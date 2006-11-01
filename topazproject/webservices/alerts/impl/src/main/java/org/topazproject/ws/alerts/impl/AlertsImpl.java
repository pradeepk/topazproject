/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.alerts.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import java.util.Date;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.text.ParseException;

import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

import org.topazproject.common.impl.SimpleTopazContext;
import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.impl.TopazContextListener;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.service.ItqlInterpreterException;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.Answer.QueryAnswer;
import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.ws.alerts.Alerts;

import org.topazproject.feed.ArticleFeed;

/** 
 * This provides the implementation of the alerts service.
 * 
 * @author Eric Brown
 */
public class AlertsImpl implements Alerts {
  private static final int    FETCH_SIZE     = 10; // TODO: Set to 100, test with 2
  private static final Log    log            = LogFactory.getLog(AlertsImpl.class);

  private static final Map    aliases;
  private static final String ALERTS_URI     = ItqlHelper.TOPAZ_URI + "alerts/";
  private static final String XSD_URI        = "http://www.w3.org/2001/XMLSchema#";

  private static final Configuration CONF    = ConfigurationStore.getInstance().getConfiguration();

  private static final String MODEL_ALERTS   = "<" + CONF.getString("topaz.models.alerts") + ">";
  private static final String ALERTS_TYPE    =
      "<" + CONF.getString("topaz.models.alerts[@type]", "http://tucana.org/tucana#Model") + ">";
  private static final String MODEL_ARTICLES = "<" + CONF.getString("topaz.models.articles") + ">";
  private static final String MODEL_PREFS    = "<" + CONF.getString("topaz.models.preferences") + ">";
  private static final String MODEL_XSD      = "<" + CONF.getString("topaz.models.xsd") + ">";
  private static final String XSD_TYPE       = "<" + CONF.getString("topaz.models.xsd[@type]") + ">";

  // Email Alert Queries
  private static final String UPDATE_TIMESTAMP_ITQL =
    "delete select $user $pred $date from ${ALERTS} where $user $pred $date and " +
    " <${userId}> <alerts:timestamp> $date from ${ALERTS};\n" +
    "insert <${userId}> <alerts:timestamp> '${stamp}'^^<xsd:date> into ${ALERTS};";
  private static final String GET_TIMESTAMP_ITQL =
    "select $date from ${ALERTS} where <${userId}> <alerts:timestamp> $date;";
  private static final String GET_USER_TIMESTAMPS_ITQL =
    "select $user $date from ${ALERTS} where <${userId}> <alerts:timestamp> $date " +
    " and $date <tucana:after> ${date} in ${XSD};";
  private static final String GET_NEXT_USERS_ITQL =
    "select $timestamp $user " +
    "  subquery( select $email from ${PREFS} where " +
    "   $user  <topaz:hasPreferences> $pref and " +
    "   $pref  <topaz:preference>     $prefn and " +
    "   $prefn <topaz:prefName>       'alertsEmailAddress' and " +
    "   $prefn <topaz:prefValue>      $email ) " +
    " from ${ALERTS} where " +
    "  $user       <alerts:timestamp> $timestamp and " +
    "  $timestamp  <tucana:before>     '${stamp}'^^<xsd:date> in ${XSD} " +
    " order by $user " +
    " limit ${limit};";
  private static final String GET_USERS_FEED_ITQL =
    "select $art $title $description $date from ${ARTICLES} where " +
    "  <${userId}> <topaz:hasPreferences> $pref  in ${PREFS} and " +
    "  $pref       <topaz:preference>     $prefn in ${PREFS} and " +
    "  $prefn      <topaz:prefName>       'alertsCategories' in ${PREFS} and " +
    "  $prefn      <topaz:prefValue>      $cat   in ${PREFS} and " +
    " $art <dc:title>       $title and " +
    " $art <dc:description> $description and " +
    " $art <dc_terms:available> $date and " +
    " $art <dc:subject>     $cat and " +
    " $date <tucana:before> '${endDate}' in ${XSD} and " +
    " $date <tucana:after>  '${startDate}' in ${XSD};";
  private static final String CLEAN_USER_ITQL =
    "delete select $user $pred $date from ${ALERTS} where $user $pred $date and " +
    " <${userId}> $pred $date from ${ALERTS};";
  private static final String CREATE_USER_ITQL =
    "insert <${userId}> <alerts:timestamp> '${stamp}'^^<xsd:date> into ${ALERTS};";

  private final AlertsPEP    pep;
  private final TopazContext ctx;

  static {
    aliases = ItqlHelper.getDefaultAliases();
    aliases.put("alerts", ALERTS_URI);
    aliases.put("xsd", XSD_URI);
  }

  /**
   * Class to stash user data while reading from Kowari.
   */
  static class UserData {
    String userId;
    String emailAddress;
    String stamp;
  }

  /**
   * Create a new permission instance.
   *
   * @param pep the policy-enforcer to use for access-control
   * @param ctx the topaz context
   */
  public AlertsImpl(AlertsPEP pep, TopazContext ctx) {
    this.ctx   = ctx;
    this.pep   = pep;

    ctx.addListener(new TopazContextListener() {
        public void handleCreated(TopazContext ctx, Object handle) {
          if (handle instanceof ItqlHelper) {
            ItqlHelper itql = (ItqlHelper) handle;

            itql.getAliases().putAll(aliases);

            try {
              itql.doUpdate("create " + MODEL_XSD + " " + XSD_TYPE + ";");
              itql.doUpdate("create " + MODEL_ALERTS + " " + ALERTS_TYPE + ";");
            } catch (IOException e) {
              log.warn("failed to create alerts models", e);
            }
          }
        }
      });
  }
  /**
   * Create a new alerts service instance.
   *
   * @param itqlService   the itql web-service
   * @param fedoraService the fedora web-service
   * @param pep           the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the itql or fedora services
   * @throws IOException if an error occurred talking to the itql or fedora services
   */
  public AlertsImpl(ProtectedService itqlService, ProtectedService fedoraService, AlertsPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this.pep = pep;

    ItqlHelper itql = new ItqlHelper(itqlService);
    itql.getAliases().putAll(this.aliases);
    itql.doUpdate("create " + MODEL_XSD + " " + XSD_TYPE + ";");
    itql.doUpdate("create " + MODEL_ALERTS + " " + ALERTS_TYPE + ";");

    FedoraAPIM apim = APIMStubFactory.create(fedoraService);
    ctx = new SimpleTopazContext(itql, apim, null);
  }

  /**
   * Create a new alerts instance.
   *
   * @param mulgaraUri is the uri to kowari/mulgara.
   * @throws MalformedURLException if the URI is invalid
   * @throws ServiceException if an error occurred locating the itql service
   * @throws RemoteException
   */
  public AlertsImpl(URI mulgaraUri)
      throws MalformedURLException, ServiceException, RemoteException {
    this.pep = null; // means we are super-user

    ItqlHelper itql = new ItqlHelper(mulgaraUri);
    itql.getAliases().putAll(this.aliases);
    itql.doUpdate("create " + MODEL_XSD + " " + XSD_TYPE + ";");
    itql.doUpdate("create " + MODEL_ALERTS + " " + ALERTS_TYPE + ";");
    ctx = new SimpleTopazContext(itql, null, null);
  }


  // See Alerts.java interface
  public void startUser(String userId) throws RemoteException {
    this.startUser(userId, Calendar.getInstance().getTime().toString());
  }

  // See Alerts.java interface
  public void startUser(String userId, String date) throws RemoteException {
    checkAccess(pep.START_USER, userId);
    ItqlHelper.validateUri(userId, "user-id");

    Calendar cal = Calendar.getInstance();
    cal.setTime(ArticleFeed.parseDateParam(date));
    Map values = new HashMap();
    values.put("ALERTS", AlertsImpl.MODEL_ALERTS);
    values.put("userId", userId);
    values.put("stamp", AlertsHelper.getTimestamp(cal));
    String query = ItqlHelper.bindValues(AlertsImpl.CREATE_USER_ITQL, values);
    ctx.getItqlHelper().doUpdate(query);
  }

  // See Alerts.java interface
  public void clearUser(String userId) throws RemoteException {
    checkAccess(pep.CLEAR_USER, userId);
    ItqlHelper.validateUri(userId, "user-id");

    Map values = new HashMap();
    values.put("ALERTS", AlertsImpl.MODEL_ALERTS);
    values.put("userId", userId);
    String query = ItqlHelper.bindValues(AlertsImpl.CLEAN_USER_ITQL, values);
    ctx.getItqlHelper().doUpdate(query);
  }

  // See Alerts.java interface
  public boolean sendAlerts(String endDate, int count) {
    checkAccess(pep.SEND_ALERTS, "dummy:dummy");
    int cnt = 0;
    try {
      Calendar end = Calendar.getInstance();
      end.setTime(ArticleFeed.parseDateParam(endDate));

      for (AlertMessages msgsIt = new AlertMessages(end); msgsIt.hasNext() && count-- > 0; ) {
        Email msg = (Email)msgsIt.next();
        AlertsHelper.sendEmail(msg);
        cnt++;
      }
      log.info("Sent " + cnt + " alerts");
    } catch (Exception e) {
      log.warn("Problem sending alerts", e);
      return false;
    }

    // TODO: Update users that were never started:
    // select $user count( select $timestamp from <rmi://localhost/fedora#alerts>
    //  where $user <http://rdf.topazproject.org/RDF/alerts/timestamp> $timestamp)
    //  from <rmi://localhost/fedora#preferences> where
    //  $user <http://rdf.topazproject.org/RDF/hasPreferences> $pref and
    //  $pref <http://rdf.topazproject.org/RDF/preference> $prefm and
    //  $prefm <http://rdf.topazproject.org/RDF/prefName> 'alertsCategories'
    //  having $k0 <http://tucana.org/tucana#occurs>
    //  '1.0'^^<http://www.w3.org/2001/XMLSchema#double>;

    return true; // Everything went okay
  }

  // See Alerts.java interface
  public boolean sendAllAlerts() {
    checkAccess(pep.SEND_ALERTS, "dummy:dummy");
    Calendar c = Calendar.getInstance();
    AlertsHelper.rollCalendar(c, -1);
    return this.sendAlerts(c.getTime().toString(), 0);
  }

  /**
   * Iterator over alerts we need to send out.
   *
   * We read in a couple of records at a time. Update the user's timestamps one at a time.
   * Send the message. Then read in a few more records.
   */
  class AlertMessages { // implements Iterator {
    Iterator usersIt; // Iterator over UserData records. Is null if no more data.
    String   endDate; // endDate for alert messages (inclusive)
    String   nextDay;
    Email    message; // Current message

    /**
     * Create AlertMessages iterator.
     *
     * @param endDate is the last day to include for alert messages (inclusive).
     */
    AlertMessages(Calendar endDate) throws RemoteException, AnswerException {
      assert endDate != null;
      this.endDate = AlertsHelper.getTimestamp(endDate);
      this.nextDay = AlertsHelper.rollTimestamp(this.endDate, 1);

      // Read first N records and set iterator
      Collection users = getNextUsers(this.endDate, FETCH_SIZE);
      if (users != null)
        this.usersIt = users.iterator();
    }

    public boolean hasNext() throws AnswerException, RemoteException,
        EmailException, AlertsGenerationException {
      while (true) {
        if (this.usersIt == null)
          return false;

        while (this.usersIt.hasNext()) {
          // Get user and update user's timestamp
          UserData user = (UserData)this.usersIt.next();
          updateTimestamp(user.userId, this.endDate);

          // Get articles we want a feed on for a specific user bounded by user.stamp and endDate
          Collection articles = getUserArticles(user, this.endDate);
          if (articles == null || articles.size() == 0)
            continue; // No articles, so no alert for this user. Skip him.

          this.message = AlertsHelper.getEmail(articles);
          this.message.setSubject("PlOS-One Alert");
          this.message.setFrom("DO-NOT-REPLY@plosone.org");
          this.message.addTo(user.emailAddress);
          this.message.addHeader("X-Topaz-Userid", user.userId);
          this.message.addHeader("X-Topaz-endDate", this.endDate);
          this.message.addHeader("X-Topaz-startDate", user.stamp);
          this.message.addHeader("X-Topaz-Articles", articles.toString());
          this.message.addHeader("X-Topaz-Categories", "N/A"); // iTQL hides these from us

          if (log.isDebugEnabled())
            log.debug("hasNext user " + user.userId + " " + user.stamp + "-" + this.endDate +
                      " msg: " + articles.toString());
          return true; // Okay, we found a user with articles
        }

        // Have reached end of iterator, read more records
        Collection users = getNextUsers(this.endDate, FETCH_SIZE);
        if (users == null || users.size() == 0) {
          this.usersIt = null;
          return false;
        }
        this.usersIt = users.iterator();
      }
    }

    public Object next() throws NoSuchElementException, RemoteException, AnswerException,
        EmailException, AlertsGenerationException {
      if (this.message == null)
        this.hasNext(); // Fetch the next message (if there is one)

      if (this.message == null)
        throw new NoSuchElementException("No more Alert messages");

      Email message = this.message;
      this.message = null; // If next() is called again, we want the next message, not this one
      return message;
    }

    public void remove() { // thorws UnsupportedOperationException {
      throw new UnsupportedOperationException("Cannot manually remove messages");
    }
  }


  /**
   * Get the next N users that have alerts from their timestamp until endDate (usually set
   * to yesterday).
   *
   * @param endDate The date to get alerts until
   * @param count The number of users to get
   * @return a list of users
   */
  private Collection getNextUsers(String endDate, int count)
      throws RemoteException, AnswerException {
    LinkedHashMap users = new LinkedHashMap();

    Map values = new HashMap();
    values.put("PREFS", MODEL_PREFS);
    values.put("ALERTS", MODEL_ALERTS);
    values.put("XSD", MODEL_XSD);
    values.put("limit", "" + count);
    values.put("stamp", endDate);
    // TODO: This query should include count(*) from ARTICLES delimited by dates
    String query = ItqlHelper.bindValues(AlertsImpl.GET_NEXT_USERS_ITQL, values);
    String response = ctx.getItqlHelper().doQuery(query);
    Answer result = new Answer(response);
    QueryAnswer  answer = (QueryAnswer)result.getAnswers().get(0);

    // Iteratoe over returned users putting them in our data structure
    for (Iterator rowIt = answer.getRows().iterator(); rowIt.hasNext(); ) {
      Object[] row = (Object[])rowIt.next();

      UserData user = new UserData();
      user.userId = ((URIReference)row[1]).getURI().toString();
      user.stamp = ((Literal)row[0]).getLexicalForm();

      QueryAnswer subAnswer = (QueryAnswer)row[2]; // from sub-query
      Object[] subRow = (Object[])subAnswer.getRows().get(0);
      user.emailAddress = subRow[0].toString();

      if (log.isDebugEnabled())
        log.debug("Found user " + user.userId + " " + user.emailAddress + " " + user.stamp);
      users.put(user.userId, user);
    }

    return users.values();
  }


  /**
   * Get timestamp user last received update. (Unused?)
   */
  private String getUserTimestamp(String userId) throws RemoteException, AnswerException {
    Map values = new HashMap();
    values.put("ALERTS", MODEL_ALERTS);
    values.put("userId", userId);
    String query = ItqlHelper.bindValues(AlertsImpl.GET_TIMESTAMP_ITQL, values);
    StringAnswer result = new StringAnswer(ctx.getItqlHelper().doQuery(query));
    QueryAnswer  answer = (QueryAnswer)result.getAnswers().get(0);

    // If user has no timestamp yet, then return today
    if (answer.getRows().size() == 0)
      return AlertsHelper.getTimestamp(Calendar.getInstance());

    // Return the timestamp we found in the ALERTS Kowari Model
    return ((String[])answer.getRows().get(0))[0];
  }

  /**
   * Update a users timestamp in the database.
   */
  private void updateTimestamp(String userId, String stamp) throws RemoteException {
    // TODO: Make this transactional AND recover if something fails...
    Map values = new HashMap();
    values.put("ALERTS", MODEL_ALERTS);
    values.put("userId", userId);
    values.put("stamp", stamp);
    String query = ItqlHelper.bindValues(AlertsImpl.UPDATE_TIMESTAMP_ITQL, values);
    ctx.getItqlHelper().doUpdate(query);
  }


  /**
   * Get the xml for a feed for a specific user's alerts preferences.
   *
   * @param user is the userId and timestamp associated with a user.
   * @param endDate is the YYYY-MM-DD that we want to go until.
   * @return the articles for this user
   */
  private Collection getUserArticles(UserData user, String endDate) throws RemoteException {
    if (endDate == null)
      endDate = AlertsHelper.getTimestamp(Calendar.getInstance()); // today

    HashMap values = new HashMap();
    values.put("ARTICLES", MODEL_ARTICLES);
    values.put("PREFS", MODEL_PREFS);
    values.put("XSD", MODEL_XSD);
    values.put("userId", user.userId);
    values.put("endDate", endDate);
    values.put("startDate", user.stamp);
    // TODO: Bracket by user.stamp and endDate once articles have date properly typed
    String query = ItqlHelper.bindValues(AlertsImpl.GET_USERS_FEED_ITQL, values);
    return this.getArticles(query);
  }

  /**
   * Get the articles associated with a feed given a query and dates.
   *
   * The query must return $art $title $description $date.
   *
   * @param startDate is the date to start searching from. If empty, start from begining of time
   * @param endDate is the date to search until. If empty, search until prsent date
   * @param query is the iTQL query that returns the articles we're interested in.
   */
  protected Collection getArticles(String query)
      throws RemoteException {
    try {
      StringAnswer articlesAnswer = new StringAnswer(ctx.getItqlHelper().doQuery(query));
      Map articles = ArticleFeed.getArticlesSummary(articlesAnswer);

      for (Iterator it = articles.keySet().iterator(); it.hasNext(); ) {
        String art = (String)it.next();
        try {
          checkAccess(pep.READ_META_DATA, art);
        } catch (SecurityException se) {
          articles.remove(art);
          if (log.isDebugEnabled())
            log.debug(art, se);
        }
      }

      String detailsQuery = ArticleFeed.getDetailsQuery(articles.values());
      StringAnswer detailsAnswer = new StringAnswer(ctx.getItqlHelper().doQuery(detailsQuery));
      ArticleFeed.addArticlesDetails(articles, detailsAnswer);

      return articles.values();
    } catch (AnswerException ae) {
      throw new RemoteException("Error querying RDF", ae);
    }
  }

  protected void checkAccess(String action, String uri) {
    pep.checkAccess(action, URI.create(uri));
  }
}
