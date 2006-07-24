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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;

/** 
 * This provides the implementation of the user preferences service.
 * 
 * <p>Preferences are stored as follows: for each app-id there exists a node with
 * "&lt;profile-id&gt; &lt;topaz:hasPreferences&gt; &lt;pref-id&gt;" and
 * "&lt;pref-id&gt; &lt;topaz:applicationId&gt; &lt;app-id&gt;". Then, for each preference
 * associated with the app-id there's a node with predicates describing the name and values
 * as follows: "&lt;pref-id&gt; &lt;topaz:preference&gt; &lt;p-node&gt;" and
 * "&lt;p-node&gt; &lt;topaz:prefName&gt; '-name-'" and one or more
 * "&lt;p-node&gt; &lt;topaz:prefValue&gt; '-value-'".
 *
 * @author Ronald Tschalär
 */
public class PreferencesImpl implements Preferences {
  private static final Log    log            = LogFactory.getLog(PreferencesImpl.class);

  private static final String FOAF_URI       = "http://xmlns.com/foaf/0.1/";

  private static final String PREF_MODEL     = "<rmi://localhost/fedora#preferences>";
  private static final String PROF_MODEL     = "<rmi://localhost/fedora#profiles>";
  private static final String PREFS_URI_PFX  = ItqlHelper.TOPAZ_URI + "preferences/";
  private static final String PROF_URI_PFX   = ItqlHelper.TOPAZ_URI + "profile/";

  private static final Map    aliases;

  private static final String ITQL_CLEAR_PREFS =
      /* Note: there's some odd bug in Kowari. If we try to do this in one single select,
       * then you get the error
       * (TuplesException) Prefix failed to meet defined minimum prefix { defined<=L[ -1 -1 -1 * ]
       * provided<=L[ 1446 ] }
       * Whatever that means. Splitting the select into two seems to solve the problem.
       */
      ("delete select $s $p $o from ${PREF_MODEL} where " +
       "$s $p $o and ( " +
       "  <${profId}> <topaz:hasPreferences> $s and $s <topaz:applicationId> ${appId} or " +
       "  <${profId}> <topaz:hasPreferences> $y and $y <topaz:applicationId> ${appId} and " +
       "      $y <topaz:preference> $s )" +
       " from ${PREF_MODEL};" + 
       "delete select $s $p $o from ${PREF_MODEL} where " +
       "$s $p $o and " +
       "  $s <tucana:is> <${profId}> and $p <tucana:is> <topaz:hasPreferences> and " +
       "      $o <topaz:applicationId> ${appId} " +
       " from ${PREF_MODEL};").
      replaceAll("\\Q${PREF_MODEL}", PREF_MODEL);

  private static final String ITQL_GET_PROFID =
      ("select $profId from ${PROF_MODEL} where " +
       "$profId <rdf:type> <foaf:Person> and " +
       "$profId <foaf:holdsAccount> $acctId and " +
       "$acctId <foaf:accountName> '${userId}';").
      replaceAll("\\Q${PROF_MODEL}", PROF_MODEL);

  private static final String ITQL_GET_PREFS =
      ("select $p $o from ${PROF_MODEL} or ${PREF_MODEL} where " +
       "$profId <rdf:type> <foaf:Person> and " +
       "$profId <foaf:holdsAccount> $acctId and $acctId <foaf:accountName> '${userId}' and " +
       "$profId <topaz:hasPreferences> $prefs and $prefs <topaz:applicationId> ${appId} and " +
       "$prefs <topaz:preference> $pr and $pr <topaz:prefName> $p and $pr <topaz:prefValue> $o;").
      replaceAll("\\Q${PROF_MODEL}", PROF_MODEL).replaceAll("\\Q${PREF_MODEL}", PREF_MODEL);

  private final ItqlHelper     itql;
  private final PreferencesPEP pep;

  static {
    aliases = ItqlHelper.getDefaultAliases();
    aliases.put("foaf", FOAF_URI);
  }

  /** 
   * Create a new preferences manager instance. 
   *
   * @param mulgaraSvc the mulgara web-service
   * @param pep        the policy-enforcer to use for access-control
   */
  public PreferencesImpl(ProtectedService mulgaraSvc, PreferencesPEP pep)
      throws IOException, ServiceException {
    this.pep = pep;

    itql = new ItqlHelper(mulgaraSvc);
    initMulgara();
  }

  /** 
   * Create a new preferences manager instance. 
   *
   * @param mulgaraUri  the uri of the mulgara server
   * @param pep         the policy-enforcer to use for access-control
   * @throws IOException 
   * @throws ServiceException 
   */
  public PreferencesImpl(URI mulgaraUri, PreferencesPEP pep) throws IOException, ServiceException {
    this.pep = pep;

    itql = new ItqlHelper(mulgaraUri);
    initMulgara();
  }

  private void initMulgara() throws IOException {
    itql.setAliases(aliases);
    itql.doUpdate("create " + PREF_MODEL + ";");
  }

  public UserPreference[] getPreferences(String appId, String userId)
      throws NoSuchIdException, RemoteException {
    pep.checkUserAccess(pep.GET_PREFERENCES, userId);

    if (userId == null)
      throw new NullPointerException("userId may not be null");

    if (log.isDebugEnabled())
      log.debug("Getting preferences for '" + userId + "', app='" + appId + "'");

    StringAnswer ans;
    try {
      String qry = ITQL_GET_PREFS.replaceAll("\\Q${userId}", userId).
                                  replaceAll("\\Q${appId}",
                                             (appId != null) ? "'" + appId + "'" : "\\$appId");
      ans = new StringAnswer(itql.doQuery(qry));
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting preferences for user '" + userId + "'", ae);
    }

    List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
    if (rows.size() == 0) {
      if (getProfileId(userId) == null)
        throw new NoSuchIdException(userId);
      else
        return null;
    }

    Map prefs = new HashMap();

    for (int idx = 0; idx < rows.size(); idx++) {
      String[] row = (String[]) rows.get(idx);

      List vl = (List) prefs.get(row[0]);
      if (vl == null)
        prefs.put(row[0], vl = new ArrayList());
      vl.add(row[1]);
    }

    UserPreference[] res = new UserPreference[prefs.size()];
    int idx = 0;
    for (Iterator iter = prefs.entrySet().iterator(); iter.hasNext(); idx++) {
      Map.Entry pref = (Map.Entry) iter.next();

      res[idx] = new UserPreference();
      res[idx].setName((String) pref.getKey());
      res[idx].setValues((String[]) ((List) pref.getValue()).toArray(new String[0]));
    }

    return res;
  }

  public void setPreferences(String appId, String userId, UserPreference[] prefs)
      throws NoSuchIdException, RemoteException {
    pep.checkUserAccess(pep.SET_PREFERENCES, userId);

    if (userId == null)
      throw new NullPointerException("userId may not be null");

    if (appId == null && prefs != null)
      throw new IllegalArgumentException("prefs must be null if app-id is null");

    if (log.isDebugEnabled())
      log.debug("Setting preferences for '" + userId + "', app='" + appId + "'");

    String txn = "set " + userId;
    try {
      itql.beginTxn(txn);

      String profId = getProfileId(userId);
      if (profId == null)
        throw new NoSuchIdException(userId);
      String prefId = getPrefsId(profId, appId);

      StringBuffer cmd = new StringBuffer(100);

      cmd.append(ITQL_CLEAR_PREFS.replaceAll("\\Q${profId}", profId).
                                  replaceAll("\\Q${appId}",
                                             (appId != null) ? "'" + appId + "'" : "\\$appId"));

      if (prefs != null && prefs.length > 0) {
        cmd.append("insert ");

        addReference(cmd, profId, "topaz:hasPreferences", prefId);
        addLiteralVal(cmd, prefId, "topaz:applicationId", appId);

        for (int idx = 0; idx < prefs.length; idx++) {
          String[] values = prefs[idx].getValues();
          if (values == null || values.length == 0)
            continue;

          String pid = prefId + "/" + idx;
          addReference(cmd, prefId, "topaz:preference", pid);
          addLiteralVal(cmd, pid, "topaz:prefName", prefs[idx].getName());

          for (int idx2 = 0; idx2 < values.length; idx2++)
            addLiteralVal(cmd, pid, "topaz:prefValue", values[idx2]);
        }

        cmd.append(" into ").append(PREF_MODEL).append(";");
      }

      itql.doUpdate(cmd.toString());

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
   * Convert a profile-id and app-id to a preferences-id. We assume a single preferences node per
   * app-id for a given profId, so the preferences id can be computed algorithmically.
   * 
   * @param profId  the profile id
   * @param appId   the app id
   * @return the preferences id
   */
  protected String getPrefsId(String profId, String appId) {
    return PREFS_URI_PFX + profId.substring(PROF_URI_PFX.length()) + "/" + appId;
  }
}
