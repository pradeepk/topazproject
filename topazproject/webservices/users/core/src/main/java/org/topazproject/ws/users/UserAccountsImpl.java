/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.users;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import javax.xml.rpc.ServiceException;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.PasswordProtectedService;
import org.topazproject.authentication.UnProtectedService;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;

/** 
 * This provides the implementation of the user-accounts service.
 * 
 * <p>A user account is stored as a foaf:OnlineAccount node. The node's URI is the interal topaz
 * user id.
 *
 * @author Ronald Tschalär
 */
public class UserAccountsImpl implements UserAccounts, UserAccountLookup {
  private static final Log    log            = LogFactory.getLog(UserAccountsImpl.class);

  private static final String FOAF_URI       = "http://xmlns.com/foaf/0.1/";

  private static final String MODEL          = "<rmi://localhost/fedora#users>";
  private static final String ACCOUNT_PID_NS = "account";

  private static final Map    aliases;

  private static final String ITQL_CREATE_ACCT =
      ("insert <${userId}> <rdf:type> <foaf:OnlineAccount> " +
              "<${userId}> <topaz:hasAuthId> '${authId}' into ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_DELETE_ACCT =
      ("delete select $s $p $o from ${MODEL} where $s $p $o and $s <tucana:is> <${userId}> " +
       "  from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_USERID =
      ("select $userId from ${MODEL} where " +
       "  $userId <rdf:type> <foaf:OnlineAccount> and $userId <topaz:hasAuthId> '${authId}';").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_TEST_USERID =
      ("select $userId from ${MODEL} where " +
       "  $userId <rdf:type> <foaf:OnlineAccount> and $userId <tucana:is> <${userId}>;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_AUTH_IDS =
      ("select $authId from ${MODEL} where " +
       "  <${userId}> <rdf:type> <foaf:OnlineAccount> and <${userId}> <topaz:hasAuthId> $authId;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_CLEAR_AUTH_IDS =
      ("delete select <${userId}> <topaz:hasAuthId> $o from ${MODEL} where " +
       "  <${userId}> <topaz:hasAuthId> $o " +
       " from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private final ItqlHelper      itql;
  private final FedoraAPIM      apim;
  private final UserAccountsPEP pep;
  private final String          baseURI;

  private String[] newAcctIds = new String[0];
  private int      newAcctIdIdx;

  static {
    aliases = ItqlHelper.getDefaultAliases();
    aliases.put("foaf", FOAF_URI);
  }

  /** 
   * Create a new user lookup instance. <em>Only use this if all you intend to use is the
   * {@link UserAccountLookup UserAccountLookup} interface.</em>
   *
   * @param itql the mulgara itql-service
   * @throws IOException if an error occurred initializing the itql service
   */
  public UserAccountsImpl(ItqlHelper itql) throws IOException {
    this.itql    = itql;
    this.apim    = null;
    this.pep     = null;
    this.baseURI = null;

    itql.getAliases().putAll(aliases);
    itql.doUpdate("create " + MODEL + ";");
  }

  /** 
   * Create a new user accounts manager instance. 
   *
   * @param itql the mulgara itql-service
   * @param apim the fedora management web-service
   * @param pep  the policy-enforcer to use for access-control
   * @throws IOException if an error occurred initializing the itql service
   * @throws ConfigurationException if any required config is missing
   */
  public UserAccountsImpl(ItqlHelper itql, FedoraAPIM apim, UserAccountsPEP pep)
      throws IOException, ConfigurationException {
    this.pep  = pep;
    this.itql = itql;
    this.apim = apim;

    itql.getAliases().putAll(aliases);
    itql.doUpdate("create " + MODEL + ";");

    Configuration conf = ConfigurationStore.getInstance().getConfiguration();
    conf = conf.subset("topaz");

    if (!conf.containsKey("objects.base-uri"))
      throw new ConfigurationException("missing key 'topaz.objects.base-uri'");
    baseURI = conf.getString("objects.base-uri");

    try {
      new URI(baseURI);
    } catch (URISyntaxException use) {
      throw new ConfigurationException("key 'topaz.objects.base-uri' does not contain a valid URI",
                                       use);
    }
  }

  /** 
   * Create a new user accounts manager instance. 
   *
   * @param mulgaraSvc the mulgara web-service
   * @param fedoraSvc  the fedora management web-service
   * @param pep        the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara or fedora services
   * @throws IOException if an error occurred talking to the mulgara or fedora services
   * @throws ConfigurationException if any required config is missing
   */
  public UserAccountsImpl(ProtectedService mulgaraSvc, ProtectedService fedoraSvc,
                          UserAccountsPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this(new ItqlHelper(mulgaraSvc), APIMStubFactory.create(fedoraSvc), pep);
  }

  /** 
   * Create a new user accounts manager instance. 
   *
   * @param mulgaraUri  the uri of the mulgara server
   * @param fedoraUri   the uri of fedora
   * @param username    the username to talk to fedora
   * @param password    the password to talk to fedora
   * @param pep         the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara or fedora services
   * @throws IOException if an error occurred talking to the mulgara or fedora services
   */
  public UserAccountsImpl(URI mulgaraUri, URI fedoraUri, String username, String password,
                          UserAccountsPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this(new UnProtectedService(mulgaraUri.toString()),
         new PasswordProtectedService(fedoraUri.toString(), username, password),
         pep);
  }

  public String createUser(String authId) throws RemoteException {
    try {
      pep.checkUserAccess(pep.CREATE_USER, baseURI + ACCOUNT_PID_NS);
    } catch (NoSuchIdException nsie) {
      throw new Error("Impossible...", nsie);   // can't happen
    }

    String txn = "create user";
    try {
      itql.beginTxn(txn);

      String userId = getNewAcctId();
      while (userExists(userId)) {
        // this shouldn't really happen...
        log.warn("Generated duplicate id '" + userId + "' - trying again...");
        userId = getNewAcctId();
      }

      itql.doUpdate(ITQL_CREATE_ACCT.replaceAll("\\Q${userId}", userId).
                                     replaceAll("\\Q${authId}", authId));

      itql.commitTxn(txn);
      txn = null;

      if (log.isDebugEnabled())
        log.debug("Created user '" + userId + "'");

      return userId;
    } finally {
      if (txn != null)
        itql.rollbackTxn(txn);
    }
  }

  public void deleteUser(String userId) throws NoSuchIdException, RemoteException {
    if (userId == null)
      throw new NullPointerException("userId may not be null");

    pep.checkUserAccess(pep.DELETE_USER, userId);

    if (log.isDebugEnabled())
      log.debug("Deleting user '" + userId + "'");

    String txn = "delete " + userId;
    try {
      itql.beginTxn(txn);

      if (!userExists(userId))
        throw new NoSuchIdException(userId);

      itql.doUpdate(ITQL_DELETE_ACCT.replaceAll("\\Q${userId}", userId));

      itql.commitTxn(txn);
      txn = null;
    } finally {
      if (txn != null)
        itql.rollbackTxn(txn);
    }
  }

  public String[] getAuthenticationIds(String userId) throws NoSuchIdException, RemoteException {
    if (userId == null)
      throw new NullPointerException("userId may not be null");

    pep.checkUserAccess(pep.GET_AUTH_IDS, userId);

    if (log.isDebugEnabled())
      log.debug("Getting auth-ids for '" + userId + "'");

    try {
      StringAnswer ans =
          new StringAnswer(itql.doQuery(ITQL_GET_AUTH_IDS.replaceAll("\\Q${userId}", userId)));
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      if (rows.size() == 0 && !userExists(userId))
        throw new NoSuchIdException(userId);

      String[] ids = new String[rows.size()];
      for (int idx = 0; idx < ids.length; idx++)
        ids[idx] = ((String[]) rows.get(idx))[0];

      return ids;
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting auth-ids for user '" + userId + "'", ae);
    }
  }

  public void setAuthenticationIds(String userId, String[] authIds)
      throws NoSuchIdException, RemoteException {
    if (userId == null)
      throw new NullPointerException("userId may not be null");

    pep.checkUserAccess(pep.SET_AUTH_IDS, userId);

    if (log.isDebugEnabled())
      log.debug("Setting auth-ids for '" + userId + "'");

    String txn = "set-auth-ids " + userId;
    try {
      itql.beginTxn(txn);

      if (!userExists(userId))
        throw new NoSuchIdException(userId);

      StringBuffer cmd = new StringBuffer(100);

      cmd.append(ITQL_CLEAR_AUTH_IDS.replaceAll("\\Q${userId}", userId));

      if (authIds != null && authIds.length > 0) {
        cmd.append("insert ");
        for (int idx = 0; idx < authIds.length; idx++) {
          if (authIds[idx] != null)
            cmd.append("<").append(userId).append("> <topaz:hasAuthId> '").
                append(authIds[idx]).append("' ");
        }
        cmd.append(" into ").append(MODEL).append(";");
      }

      itql.doUpdate(cmd.toString());

      itql.commitTxn(txn);
      txn = null;
    } finally {
      if (txn != null)
        itql.rollbackTxn(txn);
    }
  }

  public String lookUpUserByAuthId(String authId) throws RemoteException {
    try {
      pep.checkUserAccess(pep.CREATE_USER, baseURI + ACCOUNT_PID_NS);
    } catch (NoSuchIdException nsie) {
      throw new Error("Impossible...", nsie);   // can't happen
    }

    return lookUpUserByAuthIdNoAC(authId);
  }

  public String lookUpUserByAuthIdNoAC(String authId) throws RemoteException {
    if (log.isDebugEnabled())
      log.debug("Looking up user for auth-id '" + authId + "'");

    if (authId == null)
      return null;

    try {
      StringAnswer ans =
          new StringAnswer(itql.doQuery(ITQL_GET_USERID.replaceAll("\\Q${authId}", authId)));
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      return (rows.size() > 0) ? ((String[]) rows.get(0))[0] : null;
    } catch (AnswerException ae) {
      throw new RemoteException("Error looking up user for auth-id '" + authId + "'", ae);
    }
  }

  /**
   * Check if an account for the given user exists.
   *
   * @param userId the user's internal id
   * @return true if the user has an account
   * @throws RemoteException if an error occurred talking to the db
   */
  protected boolean userExists(String userId) throws RemoteException {
    try {
      StringAnswer ans =
          new StringAnswer(itql.doQuery(ITQL_TEST_USERID.replaceAll("\\Q${userId}", userId)));
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      return rows.size() > 0;
    } catch (AnswerException ae) {
      throw new RemoteException("Error testing if user '" + userId + "' exists", ae);
    }
  }

  /** 
   * Get an id (url) for a new account node. 
   * 
   * @return the url
   * @throws RemoteException if an error occurred getting the new id
   */
  protected synchronized String getNewAcctId() throws RemoteException {
    if (newAcctIdIdx >= newAcctIds.length) {
      newAcctIds = apim.getNextPID(new NonNegativeInteger("20"), ACCOUNT_PID_NS);
      newAcctIdIdx = 0;
    }

    return baseURI + newAcctIds[newAcctIdIdx++].replace(':', '/');
  }
}
