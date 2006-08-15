/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.alerts;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

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

  private static final String MODEL_ALERTS   = "<rmi://localhost/fedora#alerts>";
  private static final String MODEL_ARTICLES = "<rmi://localhost/fedora#ri>";
  private static final String MODEL_PREFS    = "<rmi://localhost/fedora#preferences>";
  private static final String MODEL_XSD      = "<rmi://localhost/fedora#xsd>";
  private static final String XSD_TYPE       = "<http://tucana.org/tucana#XMLSchemaModel>";

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
  private static final String GET_USERS_CATEGORIES_ITQL = // Not Used? (combined w/FEED_ITQL below)
    "select $user $cat from ${PREFS} where " +
    " <${userId}> <topaz:hasPreferences> $pref and " +
    " $user       <topaz:hasPreferences> $pref and " +
    " $pref       <topaz:preference>     $prefn and " +
    " $prefn      <topaz:prefName>       'alertsCategories' and " +
    " $prefn      <topaz:prefValue>      $cat;";
  private static final String GET_USERS_FEED_ITQL =
    "select $doi $title $description $date from ${ARTICLES} where " +
    "  <${userId}> <topaz:hasPreferences> $pref  in ${PREFS} and " +
    "  $pref       <topaz:preference>     $prefn in ${PREFS} and " +
    "  $prefn      <topaz:prefName>       'alertsCategories' in ${PREFS} and " +
    "  $prefn      <topaz:prefValue>      $cat   in ${PREFS} and " +
    " $doi <dc:title>       $title and " +
    " $doi <dc:description> $description and " +
    " $doi <dc:date>        $date and " +
    " $doi <dc:subject>     $cat;";
  private static final String CLEAN_USER_ITQL =
    "delete select $user $pred $date from ${ALERTS} where $user $pred $date and " +
    " <${userId}> $pred $date from ${ALERTS};";
  private static final String CREATE_USER_ITQL =
    "insert <${userId}> <alerts:timestamp> '${stamp}'^^<xsd:date> into ${ALERTS};";

  // RSS (and email alerts) Queries
  private static final String FEED_ITQL =
    "select $doi $title $description $date from ${ARTICLES} where " +
    " $doi <dc:title> $title and " +
    " $doi <dc:description> $description and " +
    " $doi <dc:date> $date " +
    " ${args} " +
    " order by $date desc;";
  private static final String FIND_SUBJECTS_ITQL =
    "select '${doi}' $subject from ${ARTICLES} where " +
    " <${doi}> <dc:subject> $subject " +
    "order by $subject;";
  private static final String FIND_AUTHORS_ITQL =
    "select '${doi}' $author from ${ARTICLES} where " +
    " <${doi}> <dc:creator> $author " +
    " order by $author;";
  
  private final AlertsPEP   pep;
  private final ItqlHelper  itql;
  private final FedoraAPIM  apim;

  static {
    aliases = ItqlHelper.getDefaultAliases();
    aliases.put("alerts", ALERTS_URI);
    aliases.put("xsd", XSD_URI);
  }
  
  /**
   * Class to stash article data in while reading from Kowari.
   */
  static class ArticleData {
    String doi;
    String title;
    String description;
    String date;
    List   authors;
    List   categories;

    public String toString() {
      return "ArticleData[" + this.doi + ":" + this.date + "]";
    }
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
    this.itql = new ItqlHelper(itqlService);
    this.apim = APIMStubFactory.create(fedoraService);

    this.itql.getAliases().putAll(this.aliases);
    this.itql.doUpdate("create " + MODEL_XSD + " " + XSD_TYPE + ";");
    this.itql.doUpdate("create " + MODEL_ALERTS + ";");
    
    Configuration conf = ConfigurationStore.getInstance().getConfiguration();
    conf = conf.subset("topaz");
  }

  
  // See Alerts.java interface
  public String getFeed(String startDate, String endDate, String[] categories, String[] authors)
      throws RemoteException {
    // Build up a list of categories or authors to append to the query string
    LinkedList params = new LinkedList();
    
    if (categories != null && categories.length > 0) {
      for (int i = 0; i < categories.length; i++)
        params.add("$doi <dc:subject> '" + ItqlHelper.escapeLiteral(categories[i]) + "' ");
    } else if (authors != null && authors.length > 0) {
      for (int i = 0; i < authors.length; i++)
        params.add("$doi <dc:creator> '" + ItqlHelper.escapeLiteral(authors[i]) + "' ");
    }

    StringBuffer args = new StringBuffer();
    if (params.size() > 0) {
      args.append(" and (");
      for (Iterator i = params.iterator(); i.hasNext(); ) {
        args.append(i.next());
        if (i.hasNext())
          args.append(" or ");
      }
      args.append(")");
    }

    // TODO: Search by date (requires modification to ingestion to support datatypes)

    Map values = new HashMap();
    values.put("ARTICLES", AlertsImpl.MODEL_ARTICLES);
    values.put("args", args.toString());
    String query = ItqlHelper.bindValues(AlertsImpl.FEED_ITQL, values);

    return AlertsHelper.buildXml(this.getArticles(startDate, endDate, query));
  }

  // See Alerts.java interface
  public void startUser(String userId) throws RemoteException {
    this.startUser(userId, Calendar.getInstance());
  }

  // See Alerts.java interface
  public void startUser(String userId, Calendar date) throws RemoteException {
    Map values = new HashMap();
    values.put("ALERTS", AlertsImpl.MODEL_ALERTS);
    values.put("userId", userId);
    values.put("stamp", AlertsHelper.getTimestamp(date));
    String query = ItqlHelper.bindValues(AlertsImpl.CREATE_USER_ITQL, values);
    this.itql.doUpdate(query);
  }
  
  // See Alerts.java interface
  public void clearUser(String userId) throws RemoteException {
    Map values = new HashMap();
    values.put("ALERTS", AlertsImpl.MODEL_ALERTS);
    values.put("userId", userId);
    String query = ItqlHelper.bindValues(AlertsImpl.CLEAN_USER_ITQL, values);
    this.itql.doUpdate(query);
  }
  
  // See Alerts.java interface
  public boolean sendAlerts(Calendar endDate, int count) {
    int cnt = 0;
    try {
      for (AlertMessages msgsIt = new AlertMessages(endDate); msgsIt.hasNext() && count-- > 0; ) {
        Email msg = (Email)msgsIt.next();
        AlertsHelper.sendEmail(msg);
        cnt++;
      }
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
    Calendar c = Calendar.getInstance();
    AlertsHelper.rollCalendar(c, -1);
    return this.sendAlerts(c, 0);
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
    String response = this.itql.doQuery(query);
    Answer result = new Answer(response);
    QueryAnswer  answer = (QueryAnswer)result.getAnswers().get(0);

    // Iteratoe over returned users putting them in our data structure
    for (Iterator rowIt = answer.getRows().iterator(); rowIt.hasNext(); ) {
      Object[] row = (Object[])rowIt.next();

      UserData user = new UserData();
      user.userId = row[1].toString();
      user.stamp = row[0].toString();
      
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
    StringAnswer result = new StringAnswer(this.itql.doQuery(query));
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
    this.itql.doUpdate(query);
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
    values.put("userId", user.userId);
    // TODO: Bracket by user.stamp and endDate once articles have date properly typed
    String query = ItqlHelper.bindValues(AlertsImpl.GET_USERS_FEED_ITQL, values);
    return this.getArticles(user.stamp, endDate, query);
  }

  /**
   * Get the articles associated with a feed given a query and dates.
   *
   * The query must return $doi $title $description $date.
   *
   * @param startDate is the date to start searching from. If empty, start from begining of time
   * @param endDate is the date to search until. If empty, search until prsent date
   * @param query is the iTQL query that returns the articles we're interested in.
   */
  protected Collection getArticles(String startDate, String endDate, String query)
      throws RemoteException {
    if (log.isDebugEnabled())
      log.debug("getArticles between " + startDate + " and " + endDate + " via: " + query);

    LinkedHashMap articles = new LinkedHashMap();
    try {
      String xmlResult = this.itql.doQuery(query);
      StringAnswer result = new StringAnswer(xmlResult);
      QueryAnswer answer = (QueryAnswer)result.getAnswers().get(0);
    
      for (Iterator rowIt = answer.getRows().iterator(); rowIt.hasNext(); ) {
        String[] row = (String[])rowIt.next();

        // TODO: Remove this check once we retrofit Kowari/Fedora to search on date
        String date = row[3];
        if (!AlertsHelper.isDateInRange(date, startDate, endDate))
          continue;
        
        ArticleData article = new ArticleData();
        article.doi         = row[0];
        article.title       = row[1];
        article.description = row[2];
        article.date        = row[3];
        articles.put(article.doi, article);
      }

      this.getDetails(articles);
    } catch (AnswerException ae) {
      throw new RemoteException("Error querying RDF", ae);
    }
        
    return articles.values();
  }

  /**
   * Given a list of articles, lookup their authors and categories.
   *
   * @param articles Map of articles to work with
   */
  protected void getDetails(Map articles) throws AnswerException, RemoteException {
    StringBuffer queryBuffer = new StringBuffer();

    // Build up set of queries
    for (Iterator i = articles.values().iterator(); i.hasNext(); ) {
      ArticleData article = (ArticleData)i.next();
      if (article == null)
        continue; // should never happen
      Map values = new HashMap();
      values.put("ARTICLES", AlertsImpl.MODEL_ARTICLES);
      values.put("doi", article.doi);
      queryBuffer.append(ItqlHelper.bindValues(AlertsImpl.FIND_SUBJECTS_ITQL, values));
      queryBuffer.append(ItqlHelper.bindValues(AlertsImpl.FIND_AUTHORS_ITQL, values));
    }
    
    String results = this.itql.doQuery(queryBuffer.toString());
    List answers = (new StringAnswer(results)).getAnswers();
    
    if (log.isDebugEnabled()) {
      log.debug("Categories/Authors queries: " + queryBuffer.toString());
      log.debug("Categories/Authors XML Results: " + results);
    }

    for (Iterator answersIt = answers.iterator(); answersIt.hasNext(); ) {
      Object ans = answersIt.next();
      if (ans instanceof String) {
        // This is Ronald trying to be helpful. It isn't something we're interested in!
        continue;
      }
      
      QueryAnswer answer = (QueryAnswer)ans;
      List rows = answer.getRows();
      
      // If the query returned nothing, just move on
      if (rows.size() == 0)
        continue;

      // Column name represents type of query we did -- for categories or authors
      String column = answer.getVariables()[1];
      String doi = ((String[])rows.get(0))[0];
      ArticleData article = (ArticleData)articles.get(doi);
      if (article == null) {
        // We should never get this - means something changed underneath us
        log.warn("Didn't find article " + doi + " on " + column + " query");
        continue;
      }

      // Get the list of values we want
      List values = new LinkedList();
      for (Iterator rowIt = rows.iterator(); rowIt.hasNext(); )
        values.add(((String[])rowIt.next())[1]);
      if (column.equals("subject")) article.categories = values;
      else if (column.equals("author")) article.authors = values;
    }
  }
}
