/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.ambra.article.action;

import java.io.Serializable;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.article.service.NoSuchArticleIdException;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Category;
import org.topazproject.ambra.models.DublinCore;
import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.web.VirtualJournalContext;
import org.topazproject.otm.Session;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Interceptor;
import com.opensymphony.xwork2.ModelDriven;

/**
 * <h4>Description</h4>
 * The <code>class ArticleFeed</code> provides an API for criteria based retrieval
 * of articles and article information. The <code>class ArticleFeed</code> implements
 * the Struts ModelDrive interface. The data model used for <code>ArticleFeed</code> is
 * <code>class Key</code>. The the field <code>ArticleFeed.cacheKey</code> is accessible
 * to Struts through the <code>ArticleFeed.getModel</code> and <code>ArticleFeed.getCacheKey</code>
 * bean getter. The ModelDriven Interceptor parses the input parameters, converts
 * them to the appropriate Java types then assigns them to fields in the data model.
 * <p>
 * The <code>ArticleFeed.cacheKey</code> serves the following purposes:
 * <ul>
 * <li> Receives and validates the parameters passed in during a Post/Get.
 * <li> Uses these parameters to compute a hashcode for cache lookups.
 * <li> Used to pass these parameters to AmbraFeedResult via the ValueStack.
 * <li> Registers a cache Invalidator as a cache listner.
 * </ul>
 * <p>
 * ArticleFeed implements the <code>ArticleFeed.exceute</code> and <code>ArticleFeed.validate
 * </code> Struts entry points. The <code>ArticleFeed.validate</code> method assigns default
 * values to fields not provided by user input and checks parameters that are provided by the
 * user. By the time Struts invokes the <code>ArticleFeed.execute</code> all model data variables
 * should be in a known and exceptable state for execution. <code>ArticleFeed.execute</code>
 * first checks the feed cache for identical queries or calls <code>ArticleFeed.getFeedData</code>
 * if there is a miss. A list of article ID's is the result. It is up to the result handler to
 * fetch the articles and serialize the output.
 * <p>
 * <ul>
 * <li>Define a hard limit of 200 articles returned in one query.
 * <li>If startDate > endDate then startDate set to endDate.
 * </ul>
 *
 * <h4>Action URI</h4>
 * http://.../article/feed
 * <h4>Parameters</h4>
 * <pre>
 * <strong>
 * Param        Format        Required     Default                    Description </strong>
 * startDate ISO yyyy/MM/dd     No         -3 months      Start Date - search for articles dated >= to sDate
 * endDate   ISO yyyy/MM/dd     No         today          End Date - search for articles dated <= to eDate
 * category    String           No         none           Article Category
 * author      String           No         none           Article Author name ex: John+Smith
 * relLinks    Boolean          No         false          If relLinks=true; internal links will be relative
 *                                                        to xmlbase
 * extended    Boolean          No         false          If extended=true; provide additional feed information
 * title       String           No         none           Sets the title of the feed
 * selfLink    String           No         none           URL of feed that is to be put in the feed data
 * maxResults  Integer          No         30             The maximun number of result to return.
 * </pre>
 *
 * @see       Key
 * @see       Invalidator
 * @see       org.topazproject.ambra.struts2.AmbraFeedResult
 *
 * @author Jeff Suttor
 * @author Eric Brown
 */
public class ArticleFeed extends BaseActionSupport implements ModelDriven {

  private static final Log log = LogFactory.getLog(ArticleFeed.class);

  //TODO: move these to BaseAction support and standardize on result dispatching.
  private static final String ATOM_RESULT = "ATOM1_0";
  private static final String JSON_RESULT = "json";

  /**
   * Cache invalidator (there can be only one) must be static.
   */
  private static Invalidator       invalidator;         // Cache listner (must be static)
  private        ArticleOtmService articleOtmService;   // OTM service Spring injected.
  private        Cache             feedCache;           // Feed Cache Spring injected
  private        JournalService    journalService;      // Journal service Spring injected.
  private        Key               cacheKey=new Key();  // The cache key and action data model
  private        List<String>      articleIDs;          // List of Article IDs; result of search



  /**
   * Try and find the query in the feed cache or query the Article OTM Service if nothing
   * is found. The parameters are valid by this point.
   *
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String execute() throws Exception {

    // Create a local lookup based on the feed URI.
    Cache.Lookup<List<String>, ApplicationException> lookUp =
        new Cache.SynchronizedLookup<List<String>, ApplicationException>(cacheKey) {
          public List<String> lookup() throws ApplicationException {
            return getFeedData();
          }
        };

    // Get articel ID's from the feed cache or add it 
    articleIDs = feedCache.get(cacheKey, -1, lookUp);

    return SUCCESS;
  }

  /**
   * Validate the input parameters or create defaults when they are not provided.
   * Struts calles this automagically after the parameters are parsed and
   * the proper fields are set in the data model. It is assumed that all
   * necessary fields are checked for validity and created if not specified.
   * The <code>ArticleFeed.execute</code> should be able to use them without
   * any further checks.
   *
   */
  @Override
  public void validate () {

    /* The cacheKey must have both the current Journal and start date.
     * Current Journal is set here and startDate will be set in the data
     * model validator.
     */
    cacheKey.setJournal(getCurrentJournal());
    cacheKey.validate(this);
  }

  /*
   * Use the OTM Service to build a list of article ID's that match the query.
   * Build the category and author list needed by the article ID query
   * then make the query
   *
   * @return Query results as a list of article ID's.
   */
  private List<String> getFeedData() throws ApplicationException {

    List<String> IDs;

    List<String> categoriesList = new ArrayList<String>();
    if (cacheKey.category != null && cacheKey.category.length() > 0) {
      categoriesList.add(cacheKey.category);
    }

    List<String> authorsList = new ArrayList<String>();
    if (cacheKey.author != null) {
      authorsList.add(cacheKey.author);
    }

    try {
      String start = cacheKey.sDate.toString();
      String end = cacheKey.eDate.toString();

      IDs = articleOtmService.getArticleIds(
          start,
          end,
          categoriesList.toArray(new String[categoriesList.size()]),
          authorsList.toArray(new String[authorsList.size()]),
          Article.ACTIVE_STATES,
          false,
          cacheKey.maxResults);
    } catch (ParseException ex) {
      throw new ApplicationException(ex);
    }

    return IDs;
  }

  /**
   * Set <code>articleOtmService</code> field to the article OTM service
   * singleton. This
   *
   * @param  articleOtmService  the object transaction model reference
   */
  public void setArticleOtmService(final ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * Allows Spring to set the feed cache singleton
   * with each new instantiation of the <code>ArticleFeed</code>. It also
   * initializes the static field <code>invalidator</code> with a cache
   * listener that removes cache entries when new articles are ingested or deleted.
   *
   * @param feedCache the ehcache instance
   */
  public void setFeedCache(Cache feedCache) {
    this.feedCache = feedCache;
    if (invalidator == null) {
      invalidator = new Invalidator();
      /*CacheManager is a singleton and will notify all caches
       *registered when a commit to the datastore is executed
       */
      this.feedCache.getCacheManager().registerListener(invalidator);
    }
  }

  /**
   * This is the results of the query which consist of a list
   * of article ID's.
   *
   * @return the list of article ID's returned from the query.
   */
  public List<String> getArticleIDs() {
    return articleIDs;
  }

  /**
   * Return the cache key being used by this action.
   *
   * @return  Key to the cache which is also the data model of the action
   */
  public Key getCacheKey() {
    return this.cacheKey;
  }

  /**
   * Return the a cahce key which is also the data modle for the model driven
   * interface.
   *
   * @return Key to the cache which is also the data model of the action
   */
  public Object getModel() {
    return cacheKey;
  }

  /**
   * Retreive the <code>VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT</code>
   * from the servlet container. This is set by the
   * <code>VirtualJournalCodntextFilter.doFilter</code>
   *
   * @see org.topazproject.ambra.web.VirtualJournalContextFilter
   *
   * @return String virtual journal of request
   */
  private String getCurrentJournal() {
    return ((VirtualJournalContext) ServletActionContext.getRequest().
        getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT)).getJournal();
  }

  /**
   * @param journalService the journal service to use
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * <h4>Description</h4>
   * The <code>class Key</code> serves three function:
   * <ul>
   * <li> It provides the data model used by the action.
   * <li> It is the cache key to the article ID's that reside in the feed cache
   * <li> It relays these input parameters to AmbraFeedResult.
   * </ul>
   * Since the parameters uniquely idententfy the query they are used to
   * generate the hash code for the key. Only the parameters that
   * can affect query results are used for this purpose. The cache key
   * is also made available to the AmbraFeedResult because it also
   * contains parameters that affect the output.
   *
   * <h4>Parameters</h4>
   * <pre>
   * <strong>
   * Param        Format        Required     Default               Description </strong>
   * </pre>
   *
   * @see       ArticleFeed
   * @see       Invalidator
   * @see       org.topazproject.ambra.struts2.AmbraFeedResult
   */
  public class Key implements Serializable, Comparable {

    private String journal;
    private Date   sDate;
    private Date   eDate;

    // Fields set by Struts
    private String  startDate;
    private String  endDate;
    private String  category;
    private String  author;
    private boolean relLinks = false;
    private boolean extended = false;
    private String  title;
    private String  selfLink;
    private int     maxResults;

    Set<Representation> representations;

    final SimpleDateFormat dateFrmt = new SimpleDateFormat("yyyy/MM/dd");
    private int hashCode;

    /**
     * Key Constructor - currently does nothing.
     */
    public Key() {

    }

    /**
     * Calulates a hash code based on the query parameters. Parameters that do not
     * affect the results of the query (selfLink, relLinks, title etc) should not be included
     * in the hash calculation because this will improve the probability of a cache hit.
     *
     * @return  <code>int hash code</code>
     */
    private int calculateHashKey() {
      final int ODD_PRIME_NUMBER = 37;  //Make values relatively prime
      int hash = 23;                    //Seed value

      if (this.journal != null)
        hash += ODD_PRIME_NUMBER * hash + this.journal.hashCode();
      if (this.sDate != null)
        hash += ODD_PRIME_NUMBER * hash + this.sDate.hashCode();
      if (this.eDate != null)
        hash += ODD_PRIME_NUMBER * hash + this.eDate.hashCode();
      if (this.category != null)
        hash += ODD_PRIME_NUMBER * hash + this.category.hashCode();
      if (this.author != null)
        hash += ODD_PRIME_NUMBER * hash + this.author.hashCode();

      hash += ODD_PRIME_NUMBER * hash + this.maxResults;

      return hash;
    }

    /**
     * The hash code is caculated after the validation is complete. The
     * results are stored here.
     *
     * @return  integer hash code
     */
    @Override
    public int hashCode() {
      return this.hashCode;
    }

    /**
     * Does a complete equality comparison of fields in the Key.
     * Only fields that will affect the results are used.
     *
     */
    @Override
    public boolean equals(Object o) {
      if (o == null || !(o instanceof Key)) return false;
      Key key = (Key) o;
      return (
          key.hashCode == this.hashCode
              &&
              (key.getJournal() == null && this.journal == null
                  || key.getJournal() != null && key.getJournal().equals(this.journal))
              &&
              (key.getSDate() == null && this.sDate == null
                  || key.getSDate() != null && key.getSDate().equals(this.sDate))
              &&
              (key.getEDate() == null && this.eDate == null
                  || key.getEDate() != null && key.getEDate().equals(this.eDate))
              &&
              (key.getCategory() == null && this.category == null
                  || key.getCategory() != null && key.getCategory().equals(this.category))
              &&
              (key.getAuthor() == null && this.author == null
                  || key.getAuthor() != null && key.getAuthor().equals(this.author))
              &&
              (key.getMaxResults() ==  this.maxResults)
      );
    }

    /**
     * Builds a string using the data model prarmeters.
     * Only parameters that affect the search results are used.
     *
     * @return a string representation of all the query parameters.
     */
    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();

      builder.append("journal=");
      builder.append(journal);

      if (sDate != null) {
        builder.append("; startDate=");
        builder.append(sDate);
      }
      if (eDate != null) {
        builder.append("; endDate=");
        builder.append(eDate);
      }
      if (category != null) {
        builder.append("; category=");
        builder.append(category);
      }
      if (author != null) {
        builder.append("; author=");
        builder.append(author);
      }

      builder.append("; maxResults=");
      builder.append(maxResults);

      return builder.toString();
    }

    /**
     * Implementation of the comparable interface. TODO: doesn't comform to
     * compare interface standard.
     *
     * TODO: shouldn't this comform to compareTo standard?
     *
     * @param o   the object to compare to.
     * @return    the value 0 if the argument is a string lexicographically equal to this string;
     *            a value less than 0 if the argument is a string lexicographically greater than
     *            this string; and a value greater than 0 if the argument is a string
     *            Slexicographically less than this string.
     */
    public int compareTo(Object o) {
      if (o == null)
        return 1;
      return toString().compareTo(o.toString());
    }

    /**
     * The ArticleFeed supports the ModelDriven interface.
     * The Key class is the data model used by ArticleFeed and
     * validates user input parameters. By the time
     * ArticleFeed.excute is invoked the parameters should be
     * a usable state.
     *
     * Defined a Maximum number of result = 200 articles.
     * Both sDate and eDate will not be null by the end of validate.
     * If sDate > eDate then set sDate = eDate.
     *
     * @param action - the BaseSupportAcion allows reporting of
     *                 field errors. Pass in a reference incase
     *                 we want to report them.
     * @see ArticleFeed
     */
    public void validate(BaseActionSupport action) {

      final int defaultMaxResult = 30;
      final int MAXIMUM_RESULTS = 200;

      if (startDate != null) {
        setSDate(startDate);
      } else {
        setSDate(defaultStartDate());
      }

      // Either set the date or it is right now
      if (endDate != null)
        setEDate(endDate);     // Convert to date
      else
        setEDate(new Date());  // Use today's date as default.

      // If start > end then just use end.
      if (sDate.after(eDate)) {
        sDate = eDate;
      }

      // Need a positive non-zero number of results
      if (maxResults <= 0)
        maxResults = defaultMaxResult;
      else if (maxResults > MAXIMUM_RESULTS)   // Don't let them crash our servers.
        maxResults = MAXIMUM_RESULTS;

      hashCode = calculateHashKey();
    }

    /**
     * Create a default start date some time in the past.
     *
     * @return todays date some default time in the past.
     */
    private Date defaultStartDate() {

      final int defaultDuration = 3;
      // The startDate was not specified so create a default
      GregorianCalendar defaultDate = new GregorianCalendar();

      defaultDate.add(Calendar.MONTH, -defaultDuration);
      defaultDate.set(Calendar.HOUR_OF_DAY, 0);
      defaultDate.set(Calendar.MINUTE, 0);
      defaultDate.set(Calendar.SECOND, 0);

      return defaultDate.getTime();
    }

    public String getJournal() {
      return journal;
    }                               

    public void setJournal(String journal) {
      this.journal = journal;
    }

    public Date getSDate() {
      return sDate;
    }

    /**
     * Convert the string to a date if possible
     * else leave the startDate null.
     *
     * @param date string date to be converted to Date
     *
     */
    public void setSDate(String date) {
      try {
        this.sDate = dateFrmt.parse(date);
      } catch (ParseException e) {
        this.sDate = defaultStartDate();
      }
    }

    public void setSDate(Date date) {
      this.sDate = date;
    }

    public Date getEDate() {
      return eDate;
    }

    /**
     * Convert the string to a date if possible
     * else leave the endDate null.
     *
     * @param date string date to be converted to Date
     *
     */
    public void setEDate(String date) {
      try {
        this.eDate = dateFrmt.parse(date);
      } catch (ParseException e) {
        this.eDate = new Date();
      }
    }

    public void setEDate(Date date) {
      this.eDate = date;
    }

    public String getCategory() {
      return category;
    }

    public void setCategory(String category) {
      this.category = category;
    }
    public String getAuthor() {
      return author;
    }

    public void setAuthor(String author) {
      this.author = author;
    }

    public boolean isRelLinks() {
      return relLinks;
    }

    public boolean getRelativeLinks() {
      return relLinks;
    }

    public void setRelativeLinks(boolean relative) {
      this.relLinks = relative;
    }

    public boolean isExtended() {
      return extended;
    }

    public boolean getExtended() {
      return extended;
    }

    public void setExtended(boolean extended) {
      this.extended = extended;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getSelfLink() {
      return selfLink;
    }

    public void setSelfLink(String link) {
      this.selfLink = link;
    }

    public String getStartDate() {
      return this.startDate;
    }

    public void setStartDate(String date) {
      this.startDate = date;
    }

    public String getEndDate() {
      return endDate;
    }

    public void setEndDate(String date) {
      this.endDate = date;
    }

    public int getMaxResults() {
      return maxResults;
    }

    public void setMaxResults(int max) {
      this.maxResults = max;
    }
  }

  /**
   * <h4>Description</h4>
   * Invalidate feedCache if new articles are ingested or
   * articles are deleted. This is accomplished via a listener
   * registered with the feed cache. This listener is notified when articles
   * are added or deleted which could potentially affect the results cached.
   * The <code>Invalidator</code> uses two
   * entry points to accomplish this.
   * <p>
   * <code>Invalidator.objectChanged</code> is called whenever an article or journal is updated.
   * <code>Invalidator.removing</code> is called whenever an article is removed.
   * <p>
   * The basic process is the same for both. Loop through all the feed cache keys and see if
   * the articles that changed could potentially affect the query results of the cache
   * entry. If it does then remove that cache entry.
   *
   * @see ArticleFeed
   */
  public class Invalidator extends AbstractObjectListener {
    /**
     * Notify the <code>Invalidator</code> that an object in the
     * cache may have changed.
     *
     * @param session  session info (unused)
     * @param cm    class metadata  (unused)
     * @param id    (unused)
     * @param object  either <code>class Article</code> or <code>class Journal<code>
     * @param updates  journal updates
     */
    @Override
    public void objectChanged(Session session, ClassMetadata cm, String id, Object object,
                              Interceptor.Updates updates) {

      /* If this is an Active Article check to see if it invalidates the feed cache
       * Else if this is a Journal change invalidate for journals.
       */
      if (object instanceof Article && ((Article) object).getState() == Article.STATE_ACTIVE) {
        invalidateFeedCacheForArticle((Article)object);
      } else if (object instanceof Journal) {
        invalidateFeedCacheForJournal((Journal)object, updates);
      }
    }

    /**
     * Notify that an article is being removed. If it matches a cache entry's
     * query criteria then remove that entry from the cache.
     *
     * @param session session info (unused)
     * @param cm    class metadata (unused)
     * @param id    (unused)
     * @param object must be <code>class Article</code>
     * @throws Exception
     */
    @Override
    public void removing(Session session, ClassMetadata cm, String id, Object object) throws Exception {
      // If this is an Active Article check to see if it invalidates the feed cache
      if (object instanceof Article && ((Article) object).getState() == Article.STATE_ACTIVE)
        invalidateFeedCacheForArticle((Article)object);
    }

    /**
     * @param journal  the journal name
     * @param updates  updates to articles
     */
    private void invalidateFeedCacheForJournal(Journal journal, Interceptor.Updates updates) {
      List<Article> articles = new ArrayList<Article>();

      for (URI articleKey : journal.getSimpleCollection()) {
        try {
          articles.add(articleOtmService.getArticle(articleKey));
        } catch (NoSuchArticleIdException e) {
          if (log.isDebugEnabled())
            log.debug("Ignoring "+articleKey);
        }
      }

      List<String> oldArticles = updates.getOldValue("simpleCollection");
      if (oldArticles != null) {
        for (String oldArticleUri : oldArticles) {
          try {
            articles.add(articleOtmService.getArticle(URI.create(oldArticleUri)));
          } catch (NoSuchArticleIdException e) {
            if (log.isDebugEnabled())
              log.debug("Ignoring old "+oldArticleUri);
          }
        }
      }

      if (!articles.isEmpty())
        invalidateFeedCacheForJournalArticle(journal, articles);
    }

    /**
     * Loop through the feed cache keys and match key parameters to
     * the article. If the article matches those found in the cache key
     * then that query is invalid and remove it.
     *
     * @param article the article which might change the cash.
     */
    private void invalidateFeedCacheForArticle(Article article) {
      for (Key key : (Set<Key>) feedCache.getKeys()) {
        if (matches(key, article, true))
          feedCache.remove(key);
      }
    }

    /**
     * Invalidate the cache entries based on a journal and a list
     * of article that are part of that journal.
     *
     * @param journal  the journal of interest
     * @param articles articles that are part of the journal
     */
    private void invalidateFeedCacheForJournalArticle(Journal journal, List<Article> articles) {
      for (Key key : (Set<Key>) feedCache.getKeys()) {
        if (key.getJournal().equals(journal.getKey())) {
          for (Article article : articles) {
            if (matches(key, article, false))
              feedCache.remove(key);
          }
        }
      }
    }

    /**
     * This is the linchpin to the entire process. Basically, query results
     * are currently affected by 5 parameters:
     * <ul>
     * <li>startDate/endDate
     * <li>author
     * <li>category
     * <li>maxResult
     * </ul>
     *
     * If the article affects the results of any one of these parameters then
     * then the query could be different and hence needs to be ejected from
     * the cache.
     *
     * Match criteria:
     *
     * If the journal doesn't match - don't remove
     * If the date of the article isn't between start and end - don't remove
     *
     * The Category and Author have to follow the following truth table.
     *                                                                      desired result
     * matchesCat       matchesAuthor     (!matchesCat and !matchesAuthor)   removeEntry
     *     F                  F                         T                       F
     *     F                  T                         F                       T
     *     T                  F                         F                       T
     *     T                  T                         F                       T
     *
     * If both do not match then don't remove the key
     * @param key the cache key and input parameters
     * @param article article that has caused the change
     * @param checkJournal include journal as part of match if true.
     * @return boolean true if we need to remove this entry from the cache
     *
     */
    private boolean matches(ArticleFeed.Key key, Article article, boolean checkJournal) {

      if (checkJournal && !matchesJournal(key, article))
        return false;

      DublinCore dc = article.getDublinCore();

      if (!matchesDates(key, dc))
        return false;

      if (!matchesCategory(key, article) && !matchesAuthor(key, dc))
        return false;

      return true;
    }

    /**
     * Compares the author in the cache key to the
     * creators specified in Dublin core.
     *
     * If key.author = null return match
     * If key.author = one of the creators return match
     * Else return no match.
     *
     * @param key the cache key
     * @param dc Dublin core field from Article
     * @return  boolean true if there is a match
     */
    private boolean matchesAuthor(Key key, DublinCore dc) {
      boolean matches = false;

      if (key.getAuthor() != null) {
        for (String author : dc.getCreators()) {
          if (key.getAuthor().equalsIgnoreCase(author)) {
            matches = true;
            break;
          }
        }
      } else
        matches = true;

      return matches;
    }

    /**
     * Compare key.categery to the article.category
     * If the key.category = null then wildcard match
     * Else If the key.category = article.category then match
     * Else return no match .
     *
     * @param key a cache key and actiopn data model
     * @param article  the article
     * @return boolean true if the category matches (key.category = null is wildcard)
     */
    private boolean matchesCategory(Key key, Article article) {
      boolean matches = false;

      if (key.getCategory() != null) {
        for (Category category : article.getCategories()) {
          if (category.getMainCategory().equals(key.getCategory())) {
            matches = true;
            break;
          }
        }
      } else
        matches = true;

      return matches;
    }

    /**
     * Check to see if the article date is between the start and
     * end date specified in the key. If it is then return true
     * and the entry for this key should be removed. If the user
     * did not specify the end date then any article after the
     * start date invalidates the cache key.
     *
     * @param key cache key
     * @param dc  Dublincore field from the article
     * @return boolean true if the article date falls between the start and end date
     */
    private boolean matchesDates(Key key, DublinCore dc) {
      Date articleDate = dc.getDate();
      boolean matches = false;

      if (articleDate != null) {
        if ((key.getEndDate() == null) && articleDate.after(key.getSDate())) {
          matches = true;
        } else if (articleDate.after(key.getSDate()) && articleDate.before(key.getEDate())) {
          matches = true;
        }
      }
      return matches;
    }

    /**
     * Loop thorugh the Journals to see if the key.journal matches
     * one of the journals the list of journals the article belongs to.
     *
     * @param key a cache key and actiopn data model
     * @param article  the article
     * @return boolean true if key.journal matches one of the journals returned
     *         by the journal service.
     */
    private boolean matchesJournal(Key key, Article article) {
      boolean matches = false;

      if (key.getJournal() != null) {
        for (Journal journal : journalService.getJournalsForObject(article.getId())) {
          if (journal.getKey().equals(key.getJournal())) {
            matches = true;
            break;
          }
        }
      } else
        matches = true;

      return matches;
    }
  }
}
