/* $HeadURL::                                                                            $
 * $Id$
 *
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
 * This provides the implementation of the profiles service.
 * 
 * <p>A profile is stored as a foaf:Person. Permissions are stored hanging off a separate
 * topaz:ProfileReadPermissions node which is linked to foaf:Person via a topaz:readPermissions
 * predicate.
 *
 * @author Ronald Tschalär
 */
public class ProfilesImpl implements Profiles {
  private static final Log    log            = LogFactory.getLog(ProfilesImpl.class);

  private static final String FOAF_URI       = "http://xmlns.com/foaf/0.1/";
  private static final String BIO_URI        = "http://purl.org/vocab/bio/0.1/";

  private static final String MODEL          = "<rmi://localhost/fedora#profiles>";
  private static final String USER_MODEL     = "<rmi://localhost/fedora#users>";
  private static final String IDS_NS         = "topaz.ids";
  private static final String PROF_PATH_PFX  = "profile";
  private static final String PERM_PATH_PFX  = "profile-perms";
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
       "( $profId $p $o or ( $profId <topaz:readPermissions> $perms and $perms $p $o ) );").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_CLEAR_PROF =
      ("delete select $s $p $o from ${MODEL} where " +
       "( $s <tucana:is> <${profId}> or <${profId}> <topaz:readPermissions> $s ) and $s $p $o " +
       "from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_CREATE_PROF =
      ("insert <${profId}> <rdf:type> <foaf:Person> " +
              "<${profId}> <topaz:readPermissions> <${permsId}> " +
              "<${permsId}> <rdf:type> <topaz:ProfileReadPermissions> " +
              "<${profId}> <foaf:holdsAccount> <${userId}> " +
              "into ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_TEST_USERID =
      ("select $userId from ${USER_MODEL} where " +
       "  $userId <rdf:type> <foaf:OnlineAccount> and $userId <tucana:is> <${userId}>;").
      replaceAll("\\Q${USER_MODEL}", USER_MODEL);

  private final ItqlHelper  itql;
  private final FedoraAPIM  apim;
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

    itql = new ItqlHelper(mulgaraSvc);
    itql.getAliases().putAll(aliases);
    itql.doUpdate("create " + MODEL + ";");

    apim = APIMStubFactory.create(fedoraSvc);

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

  public UserProfile getProfile(String userId) throws NoSuchIdException, RemoteException {
    if (userId == null)
      throw new NullPointerException("userId may not be null");

    if (log.isDebugEnabled())
      log.debug("Getting profile for '" + userId + "'");

    // get raw profile
    UserProfile prof = getRawProfile(userId);
    if (prof == null)
      return null;

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

  public void setProfile(String userId, UserProfile profile)
      throws NoSuchIdException, RemoteException {
    if (userId == null)
      throw new NullPointerException("userId may not be null");

    pep.checkUserAccess(pep.SET_PROFILE, userId);

    if (log.isDebugEnabled())
      log.debug("Setting profile for '" + userId + "'");

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
   * @throws NoSuchIdException if the user does not exist
   * @throws RemoteException if an error occurred talking to the db
   */
  protected String getProfileId(String userId) throws NoSuchIdException, RemoteException {
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
      String qry = ITQL_TEST_USERID.replaceAll("\\Q${userId}", userId) +
                   ITQL_GET_PROFID.replaceAll("\\Q${userId}", userId);
      StringAnswer ans = new StringAnswer(itql.doQuery(qry));

      List user = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      if (user.size() == 0)
        throw new NoSuchIdException(userId);

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
      String permsId = getPermsId(profId);
      cmd.append(ITQL_CREATE_PROF.replaceAll("\\Q${profId}", profId).
                                  replaceAll("\\Q${permsId}", permsId).
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
    }

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
   * Get the profile. This does no access-checks.
   *
   * @param userId  the user's id
   * @return the user's profile, or null
   * @throws NoSuchIdException if the user does not exist
   * @throws RemoteException if an error occurred retrieving the profile
   */
  protected UserProfile getRawProfile(String userId) throws NoSuchIdException, RemoteException {
    StringAnswer ans;
    try {
      String qry = ITQL_TEST_USERID.replaceAll("\\Q${userId}", userId) +
                   ITQL_GET_PROF.replaceAll("\\Q${userId}", userId);
      ans = new StringAnswer(itql.doQuery(qry));
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting profile-info for user '" + userId + "'", ae);
    }

    List user = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
    if (user.size() == 0)
      throw new NoSuchIdException(userId);

    List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(1)).getRows();
    if (rows.size() == 0)
      return null;

    UserProfile prof = new UserProfile();
    List interests   = new ArrayList();
    Map  readers     = new HashMap();

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
      else {
        List rl = (List) readers.get(row[0]);
        if (rl == null)
          readers.put(row[0], rl = new ArrayList());
        rl.add(row[1]);
      }
    }

    if (interests.size() > 0)
      prof.setInterests((String[]) interests.toArray(new String[interests.size()]));

    prof.setDisplayNameReaders(getReaders(readers, ItqlHelper.TOPAZ_URI + "displayNameReaders"));
    prof.setRealNameReaders(getReaders(readers, ItqlHelper.TOPAZ_URI + "realNameReaders"));
    prof.setTitleReaders(getReaders(readers, ItqlHelper.TOPAZ_URI + "titleReaders"));
    prof.setGenderReaders(getReaders(readers, ItqlHelper.TOPAZ_URI + "genderReaders"));
    prof.setBiographyReaders(getReaders(readers, ItqlHelper.TOPAZ_URI + "biographyReaders"));
    prof.setEmailReaders(getReaders(readers, ItqlHelper.TOPAZ_URI + "emailReaders"));
    prof.setHomePageReaders(getReaders(readers, ItqlHelper.TOPAZ_URI + "homePageReaders"));
    prof.setWeblogReaders(getReaders(readers, ItqlHelper.TOPAZ_URI + "weblogReaders"));
    prof.setPublicationsReaders(getReaders(readers, ItqlHelper.TOPAZ_URI + "publicationsReaders"));
    prof.setInterestsReaders(getReaders(readers, ItqlHelper.TOPAZ_URI + "interestsReaders"));

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
      newProfIds = apim.getNextPID(new NonNegativeInteger("20"), IDS_NS);
      newProfIdIdx = 0;
    }

    return baseURI + PROF_PATH_PFX + '/' +
           newProfIds[newProfIdIdx++].substring(IDS_NS.length() + 1);
  }

  /** 
   * Get the id (url) for a permissions node. 
   * 
   * @param profId the id of the profile the permissions node belongs to
   * @return the url
   */
  protected String getPermsId(String profId) {
    return baseURI + PERM_PATH_PFX + profId.substring(baseURI.length() + PROF_PATH_PFX.length());
  }
}
