/*
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIM;

/** 
 * This provides the implementation of the profiles service.
 * 
 * <p>A profile is stored as a foaf:Person. Additionally a foaf:OnlineAccount node is used
 * which represents the internal topaz account. Permissions are stored hanging off a separate
 * topaz:ProfileReadPermissions node which is linked to foaf:Person via a topaz:readPermissions
 * predicate.
 *
 * <p>TODO:
 * <ul>
 *   <li>app-id support</li>
 * </ul>
 *
 * @author Ronald Tschalär
 */
public class ProfilesImpl implements Profiles {
  private static final Log    log            = LogFactory.getLog(ProfilesImpl.class);

  private static final String TOPAZ_URL      = "http://rdf.topazproject.org/RDF#";
  private static final String FOAF_URL       = "http://xmlns.com/foaf/0.1/";
  private static final String BIO_URL        = "http://purl.org/vocab/bio/0.1/";

  private static final String MODEL          = "<rmi://localhost/fedora#profiles>";
  private static final String PROF_URI_PFX   = TOPAZ_URL + "profile/";
  private static final String PERM_URI_PFX   = TOPAZ_URL + "profile-perms/";
  private static final String PROFILE_PID_NS = "profile";
  private static final String ACCT_URI_PFX   = "topaz:account/";
  private static final String ACCOUNT_PID_NS = "account";
  private static final String PUBLIC_READ    = "";

  private static final Map    aliases;

  private static final String ITQL_CREATE_ACCT =
      ("insert <${profId}> <rdf:type> <foaf:Person> " +
              "<${profId}> <topaz:readPermissions> <${permsId}> " +
              "<${permsId}> <rdf:type> <topaz:ProfileReadPermissions> " +
              "<${profId}> <foaf:holdsAccount> <${acctId}> " +
              "<${acctId}> <rdf:type> <foaf:OnlineAccount> " +
              "<${acctId}> <foaf:accountName> '${userId}' into ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_DELETE_ACCT =
      ("delete select $s $p $o from ${MODEL} where " +
       "( $s <tucana:is> <${profId}> or <${profId}> <foaf:holdsAccount> $s or " +
       "  <${profId}> <topaz:readPermissions> $s ) " +
       "and $s $p $o from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_ACCTID =
      "select $acctId from ${MODEL} where $acctId <foaf:accountName> '${userId}'".
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_PROFID =
      ("select $profId from ${MODEL} where " +
       "$profId <rdf:type> <foaf:Person> and " +
       "$profId <foaf:holdsAccount> $acctId and " +
       "$acctId <foaf:accountName> '${userId}'").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_CLEAR_PROF =
      ("delete select $s $p $o from ${MODEL} where " +
       "( $s <tucana:is> <${profId}> or <${profId}> <topaz:readPermissions> $s ) and $s $p $o " +
       "and exclude($s <rdf:type> $o) and exclude($s <topaz:readPermissions> $o) and " +
       "exclude($s <foaf:holdsAccount> $o) from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_PROF =
      ("select $p $o from ${MODEL} where " +
       "$profId <rdf:type> <foaf:Person> and " +
       "$profId <foaf:holdsAccount> $acctId and $acctId <foaf:accountName> '${userId}' and " +
       "( $profId $p $o or ( $profId <topaz:readPermissions> $perms and $perms $p $o ) );").
      replaceAll("\\Q${MODEL}", MODEL);

  private final ItqlHelper  itql;
  private final ProfilesPEP pep;
  private final FedoraAPIM  apim;

  private String[] newProfIds = new String[0];
  private int      newProfIdIdx;
  private String[] newAcctIds = new String[0];
  private int      newAcctIdIdx;

  static {
    aliases = ItqlHelper.getDefaultAliases();
    aliases.put("foaf", FOAF_URL);
    aliases.put("bio",  BIO_URL);
  }

  /** 
   * Create a new profiles manager instance. 
   *
   * @param mulgaraSvc the mulgara web-service
   * @param fedoraSvc  the fedora management web-service
   * @param pep        the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara or fedora services
   * @throws IOException if an error occurred talking to the mulgara or fedora services
   */
  public ProfilesImpl(ProtectedService mulgaraSvc, ProtectedService fedoraSvc, ProfilesPEP pep)
      throws IOException, ServiceException {
    this.pep = pep;

    itql = new ItqlHelper(mulgaraSvc);
    itql.setAliases(aliases);
    itql.doUpdate("create " + MODEL + ";");

    apim = APIMStubFactory.create(fedoraSvc);
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
   * @throws IOException if an error occurred talking to the mulgara or fedora services
   */
  public ProfilesImpl(URI mulgaraUri, URI fedoraUri, String username, String password,
                      ProfilesPEP pep) throws IOException, ServiceException {
    this(ProtectedServiceFactory.createService(mulgaraUri.toString(), null, null, false),
         ProtectedServiceFactory.createService(fedoraUri.toString(), username, password, true),
         pep);
  }

  public void createProfile(String userId, UserProfile profile)
      throws DuplicateIdException, RemoteException {
    pep.checkUserAccess(pep.CREATE_PROFILE, userId);

    if (log.isDebugEnabled())
      log.debug("Creating profile for '" + userId + "'");

    String txn = "create " + userId;
    try {
      itql.beginTxn(txn);

      if (getProfileId(userId) != null)
        throw new DuplicateIdException(userId);

      String acctId = null;
      try {
        StringAnswer ans =
            new StringAnswer(itql.doQuery(ITQL_GET_ACCTID.replaceAll("\\Q${userId}", userId)));
        List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
        acctId = rows.size() > 0 ? ((String[]) rows.get(0))[0] : getNewAcctId();
      } catch (AnswerException ae) {
        throw new RemoteException("Error getting account-url for user '" + userId + "'", ae);
      }

      String profId  = getNewProfId();
      String permsId = getPermsId(profId);
      itql.doUpdate(ITQL_CREATE_ACCT.replaceAll("\\Q${profId}", profId).
                                     replaceAll("\\Q${permsId}", permsId).
                                     replaceAll("\\Q${acctId}", acctId).
                                     replaceAll("\\Q${userId}", userId));

      if (profile != null)
        setProfile(profId, profile);

      itql.commitTxn(txn);
      txn = null;
    } finally {
      if (txn != null)
        itql.rollbackTxn(txn);
    }
  }

  public UserProfile getProfile(String userId) throws NoSuchIdException, RemoteException {
    if (log.isDebugEnabled())
      log.debug("Getting profile for '" + userId + "'");

    // get raw profile
    UserProfile prof = getProfileIntern(userId);
    if (prof == null)
      throw new NoSuchIdException(userId);

    // filter profile based on access-controls.
    if (prof.getDisplayName() != null &&
        !checkReadAccess(prof.getDisplayNameReaders(), userId, "displayName"))
      prof.setDisplayName(null);
    if (prof.getRealName() != null &&
        !checkReadAccess(prof.getRealNameReaders(), userId, "realName"))
      prof.setRealName(null);
    if (prof.getTitle() != null && !checkReadAccess(prof.getTitleReaders(), userId, "title"))
      prof.setTitle(null);
    if (prof.getGender() != null && !checkReadAccess(prof.getGenderReaders(), userId, "gender"))
      prof.setGender(null);
    if (prof.getBiography() != null &&
        !checkReadAccess(prof.getBiographyReaders(), userId, "biography"))
      prof.setBiography(null);
    if (prof.getEmail() != null && !checkReadAccess(prof.getEmailReaders(), userId, "email"))
      prof.setEmail(null);
    if (prof.getHomePage() != null &&
        !checkReadAccess(prof.getHomePageReaders(), userId, "homePage"))
      prof.setHomePage(null);
    if (prof.getWeblog() != null && !checkReadAccess(prof.getWeblogReaders(), userId, "weblog"))
      prof.setWeblog(null);
    if (prof.getPublications() != null &&
        !checkReadAccess(prof.getPublicationsReaders(), userId, "publications"))
      prof.setPublications(null);
    if (prof.getInterests() != null &&
        !checkReadAccess(prof.getInterestsReaders(), userId, "interests"))
      prof.setInterests(null);

    return prof;
  }

  private boolean checkReadAccess(String[] readers, String owner, String prop) {
    /* TODO: should we use the xacml policy for everything? The public-access shortcut
     * both simplifies logic and speeds up the common(?) case. And we've really already
     * codified the policy, so it's not like there's much freedom in writing the xacml.
     */
    if (readers == null)        // public access
      return true;
    try {
      pep.checkReadAccess(owner, readers);
      return true;
    } catch (SecurityException se) {
      if (log.isDebugEnabled())
        log.debug("read access to '" + prop + "' of '" + owner + "' denied", se);
      return false;
    }
  }

  public void updateProfile(String userId, UserProfile profile)
      throws NoSuchIdException, RemoteException {
    pep.checkUserAccess(pep.UPDATE_PROFILE, userId);

    if (log.isDebugEnabled())
      log.debug("Updating profile for '" + userId + "'");

    String txn = "update " + userId;
    try {
      itql.beginTxn(txn);

      String profId = getProfileId(userId);
      if (profId == null)
        throw new NoSuchIdException(userId);

      setProfile(profId, profile);

      itql.commitTxn(txn);
      txn = null;
    } finally {
      if (txn != null)
        itql.rollbackTxn(txn);
    }
  }

  public void deleteProfile(String userId) throws NoSuchIdException, RemoteException {
    pep.checkUserAccess(pep.DELETE_PROFILE, userId);

    if (log.isDebugEnabled())
      log.debug("Deleting profile for '" + userId + "'");

    String txn = "delete " + userId;
    try {
      itql.beginTxn(txn);

      String profId = getProfileId(userId);
      if (profId == null)
        throw new NoSuchIdException(userId);

      itql.doUpdate(ITQL_DELETE_ACCT.replaceAll("\\Q${profId}", profId));

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
   * @throws RemoteException if an error occurred talking to the db
   */
  protected String getProfileId(String userId) throws RemoteException {
    try {
      StringAnswer ans =
          new StringAnswer(itql.doQuery(ITQL_GET_PROFID.replaceAll("\\Q${userId}", userId)));
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      return rows.size() == 0 ? null : ((String[]) rows.get(0))[0];
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting profile-url for user '" + userId + "'", ae);
    }
  }

  /**
   * Set the profile. This does no access-checks and assumes the profile node exists.
   *
   * @param profId  the url of the profile node
   * @param profile the new profile
   * @throws RemoteException if an error occurred updating the profile
   */
  protected void setProfile(String profId, UserProfile profile) throws RemoteException {
    StringBuffer cmd = new StringBuffer(100);

    cmd.append(ITQL_CLEAR_PROF.replaceAll("\\Q${profId}", profId));

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

    String permsId = getPermsId(profId);
    addPerms(cmd, permsId, "topaz:displayNameReaders", profile.getDisplayNameReaders());
    addPerms(cmd, permsId, "topaz:realNameReaders", profile.getRealNameReaders());
    addPerms(cmd, permsId, "topaz:titleReaders", profile.getTitleReaders());
    addPerms(cmd, permsId, "topaz:genderReaders", profile.getGenderReaders());
    addPerms(cmd, permsId, "topaz:biographyReaders", profile.getBiographyReaders());
    addPerms(cmd, permsId, "topaz:emailReaders", profile.getEmailReaders());
    addPerms(cmd, permsId, "topaz:homePageReaders", profile.getHomePageReaders());
    addPerms(cmd, permsId, "topaz:weblogReaders", profile.getWeblogReaders());
    addPerms(cmd, permsId, "topaz:publicationsReaders", profile.getPublicationsReaders());
    addPerms(cmd, permsId, "topaz:interestsReaders", profile.getInterestsReaders());

    cmd.append(" into ").append(MODEL).append(";");
    itql.doUpdate(cmd.toString());
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

  private static final void addPerms(StringBuffer buf, String subj, String pred, String[] perms) {
    if (perms == null) {        // public access
      addLiteralVal(buf, subj, pred, PUBLIC_READ);
      return;
    }

    for (int idx = 0; idx < perms.length; idx++)
      addReference(buf, subj, pred, perms[idx]);
  }

  /**
   * Get the profile. This does no access-checks and assumes the profile node exists.
   *
   * @param userId  the user's id
   * @return the user's profile, or null
   * @throws RemoteException if an error occurred retrieving the profile
   */
  protected UserProfile getProfileIntern(String userId) throws RemoteException {
    StringAnswer ans;
    try {
      ans = new StringAnswer(itql.doQuery(ITQL_GET_PROF.replaceAll("\\Q${userId}", userId)));
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting profile-info for user '" + userId + "'", ae);
    }

    List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
    if (rows.size() == 0)
      return null;

    UserProfile prof = new UserProfile();
    List interests   = new ArrayList();
    Map  readers     = new HashMap();

    for (int idx = 0; idx < rows.size(); idx++) {
      String[] row = (String[]) rows.get(idx);

      if (row[0].equals(TOPAZ_URL + "displayName"))
        prof.setDisplayName(row[1]);
      else if (row[0].equals(FOAF_URL + "name"))
        prof.setRealName(row[1]);
      else if (row[0].equals(FOAF_URL + "title"))
        prof.setTitle(row[1]);
      else if (row[0].equals(FOAF_URL + "gender"))
        prof.setGender(row[1]);
      else if (row[0].equals(BIO_URL + "olb"))
        prof.setBiography(row[1]);
      else if (row[0].equals(FOAF_URL + "mbox"))
        prof.setEmail(row[1].substring(7));
      else if (row[0].equals(FOAF_URL + "homepage"))
        prof.setHomePage(row[1]);
      else if (row[0].equals(FOAF_URL + "weblog"))
        prof.setWeblog(row[1]);
      else if (row[0].equals(FOAF_URL + "publications"))
        prof.setPublications(row[1]);
      else if (row[0].equals(FOAF_URL + "interest"))
        interests.add(row[1]);
      else {
        List rl = (List) readers.get(row[0]);
        if (rl == null)
          readers.put(row[0], rl = new ArrayList());
        rl.add(row[1]);
      }
    }

    if (interests.size() > 0)
      prof.setInterests((String[]) interests.toArray(new String[interests.size()]));

    prof.setDisplayNameReaders(getReaders(readers, TOPAZ_URL + "displayNameReaders"));
    prof.setRealNameReaders(getReaders(readers, TOPAZ_URL + "realNameReaders"));
    prof.setTitleReaders(getReaders(readers, TOPAZ_URL + "titleReaders"));
    prof.setGenderReaders(getReaders(readers, TOPAZ_URL + "genderReaders"));
    prof.setBiographyReaders(getReaders(readers, TOPAZ_URL + "biographyReaders"));
    prof.setEmailReaders(getReaders(readers, TOPAZ_URL + "emailReaders"));
    prof.setHomePageReaders(getReaders(readers, TOPAZ_URL + "homePageReaders"));
    prof.setWeblogReaders(getReaders(readers, TOPAZ_URL + "weblogReaders"));
    prof.setPublicationsReaders(getReaders(readers, TOPAZ_URL + "publicationsReaders"));
    prof.setInterestsReaders(getReaders(readers, TOPAZ_URL + "interestsReaders"));

    return prof;
  }

  private static String[] getReaders(Map readers, String perm) {
    List rl = (List) readers.get(perm);
    if (rl == null)
      return new String[0];     // private access only
    if (rl.size() == 1 && ((String) rl.get(0)).equals(PUBLIC_READ))
      return null;              // public access
    return (String[]) rl.toArray(new String[rl.size()]);
  }

  /** 
   * Get an id (url) for a new profile node. 
   * 
   * @return the url
   * @throws RemoteException if an error occurred getting the new id
   */
  protected synchronized String getNewProfId() throws RemoteException {
    if (newProfIdIdx >= newProfIds.length) {
      newProfIds = apim.getNextPID(new NonNegativeInteger("20"), PROFILE_PID_NS);
      newProfIdIdx = 0;
    }

    return PROF_URI_PFX + newProfIds[newProfIdIdx++];
  }

  /** 
   * Get the id (url) for a permissions node. 
   * 
   * @param profId the id of the profile the permissions node belongs to
   * @return the url
   */
  protected String getPermsId(String profId) {
    return PERM_URI_PFX + profId.substring(PROF_URI_PFX.length());
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

    return ACCT_URI_PFX + newAcctIds[newAcctIdIdx++];
  }
}
