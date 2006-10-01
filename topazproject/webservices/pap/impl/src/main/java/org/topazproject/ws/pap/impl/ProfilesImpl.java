/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.topazproject.common.impl.SimpleTopazContext;
import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.impl.TopazContextListener;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.ws.pap.Profiles;
import org.topazproject.ws.pap.UserProfile;

/** 
 * This provides the implementation of the profiles service.
 * 
 * <p>A profile is stored as a foaf:Person. Permissions can be managed via the {@link
 * org.topazproject.ws.permissions.Permissions Permissions} service, where the resource is the
 * internal user-id.
 *
 * @author Ronald Tschalär
 */
public class ProfilesImpl implements Profiles {
  private static final Log    log            = LogFactory.getLog(ProfilesImpl.class);

  private static final String FOAF_URI       = "http://xmlns.com/foaf/0.1/";
  private static final String BIO_URI        = "http://purl.org/vocab/bio/0.1/";

  private static final Configuration CONF    = ConfigurationStore.getInstance().getConfiguration();

  private static final String MODEL          = "<" + CONF.getString("topaz.models.profiles") + ">";
  private static final String MODEL_TYPE     =
      "<" + CONF.getString("topaz.models.profiles[@type]", "http://tucana.org/tucana#Model") + ">";
  private static final String USER_MODEL     = "<" + CONF.getString("topaz.models.users") + ">";
  private static final String IDS_NS         = "topaz.ids";
  private static final String PROF_PATH_PFX  = "profile";
  private static final String PUBLIC_READ    = "";

  private static final Map    aliases;

  private static final String ITQL_GET_PROFID =
      ("select $profId from ${MODEL} where " +
       "$profId <rdf:type> <foaf:Person> and " +
       "$profId <foaf:holdsAccount> <${userId}>;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_PROF =
      ("select $p $o from ${MODEL} where " +
       "$profId <rdf:type> <foaf:Person> and " +
       "$profId <foaf:holdsAccount> <${userId}> and " +
       "$profId $p $o;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_CLEAR_PROF =
      ("delete select <${profId}> $p $o from ${MODEL} where <${profId}> $p $o from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_CREATE_PROF =
      ("insert <${profId}> <rdf:type> <foaf:Person> " +
              "<${profId}> <foaf:holdsAccount> <${userId}> " +
              "into ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_TEST_USERID =
      ("select $userId from ${USER_MODEL} where " +
       "  $userId <rdf:type> <foaf:OnlineAccount> and $userId <tucana:is> <${userId}>;").
      replaceAll("\\Q${USER_MODEL}", USER_MODEL);

  private final TopazContext ctx;
  private final ProfilesPEP pep;
  private final String      baseURI;

  private String[] newProfIds = new String[0];
  private int      newProfIdIdx;

  static {
    aliases = ItqlHelper.getDefaultAliases();
    aliases.put("foaf", FOAF_URI);
    aliases.put("bio",  BIO_URI);
  }

  /**
   * Create a new profiles instance.
   *
   * @param pep the policy-enforcer to use for access-control
   * @param ctx the topaz context
   *
   */
  public ProfilesImpl(ProfilesPEP pep, TopazContext ctx) {
    this.ctx   = ctx;
    this.pep   = pep;
    this.baseURI = ctx.getObjectBaseUri().toString();

    ctx.addListener(new TopazContextListener() {
        public void handleCreated(TopazContext ctx, Object handle) {
          if (handle instanceof ItqlHelper) {
            ItqlHelper itql = (ItqlHelper) handle;
            itql.getAliases().putAll(aliases);
            try {
              itql.doUpdate("create " + MODEL + " " + MODEL_TYPE + ";");
            } catch (IOException e) {
              log.warn("failed to create model " + MODEL, e);
            }
          }
        }
      });
  }
  /** 
   * Create a new profiles manager instance. 
   *
   * @param mulgaraSvc the mulgara web-service
   * @param fedoraSvc  the fedora management web-service
   * @param pep        the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara or fedora services
   * @throws ConfigurationException if any required config is missing
   * @throws IOException if an error occurred talking to the mulgara or fedora services
   */
  public ProfilesImpl(ProtectedService mulgaraSvc, ProtectedService fedoraSvc, ProfilesPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this.pep = pep;

    ItqlHelper itql = new ItqlHelper(mulgaraSvc);
    itql.getAliases().putAll(aliases);
    itql.doUpdate("create " + MODEL + " " + MODEL_TYPE + ";");

    FedoraAPIM apim = APIMStubFactory.create(fedoraSvc);

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
    ctx = new SimpleTopazContext(itql, apim, null);
  }

  /** 
   * Create a new profiles manager instance. 
   *
   * @param mulgaraUri  the uri of the mulgara server
   * @param fedoraUri   the uri of fedora
   * @param username    the username to talk to fedora
   * @param password    the password to talk to fedora
   * @param pep         the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara or fedora services
   * @throws ConfigurationException if any required config is missing
   * @throws IOException if an error occurred talking to the mulgara or fedora services
   */
  public ProfilesImpl(URI mulgaraUri, URI fedoraUri, String username, String password,
                      ProfilesPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this(new UnProtectedService(mulgaraUri.toString()),
         new PasswordProtectedService(fedoraUri.toString(), username, password),
         pep);
  }

  public UserProfile getProfile(String userId) throws NoSuchUserIdException, RemoteException {
    if (userId == null)
      throw new NullPointerException("userId may not be null");

    if (log.isDebugEnabled())
      log.debug("Getting profile for '" + userId + "'");

    // get raw profile
    UserProfile prof = getRawProfile(userId);
    if (prof == null)
      return null;

    // filter profile based on access-controls.
    if (prof.getDisplayName() != null && !checkAccess(userId, pep.GET_DISP_NAME))
      prof.setDisplayName(null);
    if (prof.getRealName() != null && !checkAccess(userId, pep.GET_REAL_NAME))
      prof.setRealName(null);
    if (prof.getTitle() != null && !checkAccess(userId, pep.GET_TITLE))
      prof.setTitle(null);
    if (prof.getGender() != null && !checkAccess(userId, pep.GET_GENDER))
      prof.setGender(null);
    if (prof.getBiography() != null && !checkAccess(userId, pep.GET_BIOGRAPHY))
      prof.setBiography(null);
    if (prof.getEmail() != null && !checkAccess(userId, pep.GET_EMAIL))
      prof.setEmail(null);
    if (prof.getHomePage() != null && !checkAccess(userId, pep.GET_HOME_PAGE))
      prof.setHomePage(null);
    if (prof.getWeblog() != null && !checkAccess(userId, pep.GET_WEBLOG))
      prof.setWeblog(null);
    if (prof.getPublications() != null && !checkAccess(userId, pep.GET_PUBLICATIONS))
      prof.setPublications(null);
    if (prof.getInterests() != null && !checkAccess(userId, pep.GET_INTERESTS))
      prof.setInterests(null);

    return prof;
  }

  private boolean checkAccess(String owner, String perm) throws NoSuchUserIdException {
    try {
      pep.checkUserAccess(perm, owner);
      return true;
    } catch (SecurityException se) {
      if (log.isDebugEnabled())
        log.debug("access '" + perm + "' to '" + owner + "'s profile denied", se);
      return false;
    }
  }

  public void setProfile(String userId, UserProfile profile)
      throws NoSuchUserIdException, RemoteException {
    if (userId == null)
      throw new NullPointerException("userId may not be null");

    pep.checkUserAccess(pep.SET_PROFILE, userId);

    if (log.isDebugEnabled())
      log.debug("Setting profile for '" + userId + "'");

    ItqlHelper itql = ctx.getItqlHelper();
    String txn = "set-profile " + userId;
    try {
      itql.beginTxn(txn);

      String profId = getProfileId(userId);
      if (profId == null)
        profId = getNewProfId();

      setRawProfile(userId, profId, profile);

      itql.commitTxn(txn);
      txn = null;
    } finally {
      if (txn != null)
        itql.rollbackTxn(txn);
    }
  }

  /**
   * Get the id (url) of the profile node for the given internal user-id.
   *
   * @param userId the user's internal id
   * @return the profile id, or null if this user doesn't have one (doesn't exist)
   * @throws NoSuchUserIdException if the user does not exist
   * @throws RemoteException if an error occurred talking to the db
   */
  protected String getProfileId(String userId) throws NoSuchUserIdException, RemoteException {
    try {
      /* Implementation note:
       * Instead of doing two queries we could also use a subquery:
       *
       *   select $userId subquery(
       *     select $profId from <profiles-model> where $profId <foaf:holdsAccount> $userId )
       *   from <user-model> where $userId <rdf:type> <foaf:OnlineAccount> and
       *                           $userId <tucana:is> <${userId}>;
       *
       * But the answer is harder to parse, and I'm not sure it gains anything over the two-query
       * solution in this case.
       */
      ItqlHelper itql = ctx.getItqlHelper();
      String qry = ITQL_TEST_USERID.replaceAll("\\Q${userId}", userId) +
                   ITQL_GET_PROFID.replaceAll("\\Q${userId}", userId);
      StringAnswer ans = new StringAnswer(itql.doQuery(qry));

      List user = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      if (user.size() == 0)
        throw new NoSuchUserIdException(userId);

      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(1)).getRows();
      return rows.size() == 0 ? null : ((String[]) rows.get(0))[0];
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting profile-url for user '" + userId + "'", ae);
    }
  }

  /**
   * Set the profile. This does no access-checks.
   *
   * @param userId  the user's internal id
   * @param profId  the url of the profile node
   * @param profile the new profile
   * @throws RemoteException if an error occurred updating the profile
   */
  protected void setRawProfile(String userId, String profId, UserProfile profile)
      throws RemoteException {
    StringBuffer cmd = new StringBuffer(500);

    cmd.append(ITQL_CLEAR_PROF.replaceAll("\\Q${profId}", profId));

    if (profile != null) {
      cmd.append(ITQL_CREATE_PROF.replaceAll("\\Q${profId}", profId).
                                  replaceAll("\\Q${userId}", userId));

      cmd.append("insert ");

      addLiteralVal(cmd, profId, "topaz:displayName", profile.getDisplayName());
      addLiteralVal(cmd, profId, "foaf:name", profile.getRealName());
      addLiteralVal(cmd, profId, "foaf:title", profile.getTitle());
      addLiteralVal(cmd, profId, "foaf:gender", profile.getGender());
      addLiteralVal(cmd, profId, "bio:olb", profile.getBiography());

      String email = profile.getEmail();
      if (email != null)
        addReference(cmd, profId, "foaf:mbox", "mailto:" + email);

      addReference(cmd, profId, "foaf:homepage", profile.getHomePage());
      addReference(cmd, profId, "foaf:weblog", profile.getWeblog());
      addReference(cmd, profId, "foaf:publications", profile.getPublications());

      String[] interests = profile.getInterests();
      for (int idx = 0; interests != null && idx < interests.length; idx++)
        addReference(cmd, profId, "foaf:interest", interests[idx]);

      cmd.append(" into ").append(MODEL).append(";");
    }

    ctx.getItqlHelper().doUpdate(cmd.toString());
  }

  private static final void addLiteralVal(StringBuffer buf, String subj, String pred, String lit) {
    if (lit == null)
      return;

    buf.append("<").append(subj).append("> <").append(pred).append("> '").append(lit).append("' ");
  }

  private static final void addReference(StringBuffer buf, String subj, String pred, String url) {
    if (url == null)
      return;

    buf.append("<").append(subj).append("> <").append(pred).append("> <").append(url).append("> ");
  }

  /**
   * Get the profile. This does no access-checks.
   *
   * @param userId  the user's id
   * @return the user's profile, or null
   * @throws NoSuchUserIdException if the user does not exist
   * @throws RemoteException if an error occurred retrieving the profile
   */
  protected UserProfile getRawProfile(String userId) throws NoSuchUserIdException, RemoteException {
    StringAnswer ans;
    try {
      String qry = ITQL_TEST_USERID.replaceAll("\\Q${userId}", userId) +
                   ITQL_GET_PROF.replaceAll("\\Q${userId}", userId);
      ans = new StringAnswer(ctx.getItqlHelper().doQuery(qry));
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting profile-info for user '" + userId + "'", ae);
    }

    List user = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
    if (user.size() == 0)
      throw new NoSuchUserIdException(userId);

    List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(1)).getRows();
    if (rows.size() == 0)
      return null;

    UserProfile prof = new UserProfile();
    List interests   = new ArrayList();

    for (int idx = 0; idx < rows.size(); idx++) {
      String[] row = (String[]) rows.get(idx);

      if (row[0].equals(ItqlHelper.TOPAZ_URI + "displayName"))
        prof.setDisplayName(row[1]);
      else if (row[0].equals(FOAF_URI + "name"))
        prof.setRealName(row[1]);
      else if (row[0].equals(FOAF_URI + "title"))
        prof.setTitle(row[1]);
      else if (row[0].equals(FOAF_URI + "gender"))
        prof.setGender(row[1]);
      else if (row[0].equals(BIO_URI + "olb"))
        prof.setBiography(row[1]);
      else if (row[0].equals(FOAF_URI + "mbox"))
        prof.setEmail(row[1].substring(7));
      else if (row[0].equals(FOAF_URI + "homepage"))
        prof.setHomePage(row[1]);
      else if (row[0].equals(FOAF_URI + "weblog"))
        prof.setWeblog(row[1]);
      else if (row[0].equals(FOAF_URI + "publications"))
        prof.setPublications(row[1]);
      else if (row[0].equals(FOAF_URI + "interest"))
        interests.add(row[1]);
    }

    if (interests.size() > 0)
      prof.setInterests((String[]) interests.toArray(new String[interests.size()]));

    return prof;
  }

  /** 
   * Get an id (url) for a new profile node. 
   * 
   * @return the url
   * @throws RemoteException if an error occurred getting the new id
   */
  protected synchronized String getNewProfId() throws RemoteException {
    if (newProfIdIdx >= newProfIds.length) {
      newProfIds = ctx.getFedoraAPIM().getNextPID(new NonNegativeInteger("20"), IDS_NS);
      newProfIdIdx = 0;
    }

    return baseURI + PROF_PATH_PFX + '/' +
           newProfIds[newProfIdIdx++].substring(IDS_NS.length() + 1);
  }
}
