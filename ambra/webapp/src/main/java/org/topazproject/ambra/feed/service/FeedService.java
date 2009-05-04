/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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
package org.topazproject.ambra.feed.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.Category;
import org.topazproject.ambra.models.DublinCore;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.UserAccount;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.MinorCorrection;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.models.RatingSummary;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.models.Retraction;
import org.topazproject.ambra.models.Annotation;
import org.topazproject.ambra.models.Trackback;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.model.article.ArticleInfo;
import org.topazproject.ambra.article.action.TOCArticleGroup;
import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.article.service.NoSuchArticleIdException;

import org.topazproject.otm.Session;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Interceptor;

/**
 * The <code>FeedService</code> supplies the API for querying and caching
 * feed request. <code>FeedService</code> is a Spring injected singleton
 * which coordinates access to the <code>annotationService, articleOtmService</code>
 * and <code>feedCache</code>.
 */
public class FeedService {
  private static final Log log = LogFactory.getLog(FeedService.class);

  private AnnotationService   annotationService;    // Annotation service Spring injected.
  private ArticleOtmService   articleOtmService;    // Article Otm service Spring injected
  private BrowseService       browseService;        // Browse Article Servcie Spring Injected
  private JournalService      journalService;       // Journal service Spring injected.
  private Cache               feedCache;            // Feed Cache Spring injected
  private Invalidator         invalidator;          // Cache invalidator
  private Session             session;

  /**
   * The feedAction data model has a types parameter which specifies
   * the feed type (currently Article or Annotation). Invalid
   * is used when the types parameter does not match any of the current
   * feed types.
   */
  public enum FEED_TYPES {
    Article               { public String rdfType() { return null;                       }
                            public Class  isClass() { return Article.class;              } },
    Annotation            { public String rdfType() {
                            return org.topazproject.ambra.models.Annotation.RDF_TYPE; }
                            public Class  isClass() { return Annotation.class;    } },
    Comment               { public String rdfType() {
                            return org.topazproject.ambra.models.Comment.RDF_TYPE;
                          }
                            public Class  isClass() { return Comment.class;              } },
    FormalCorrection      { public String rdfType() { 
                            return org.topazproject.ambra.models.FormalCorrection.RDF_TYPE;
                          }
                            public Class  isClass() { return FormalCorrection.class;     } },
    MinorCorrection       { public String rdfType() {
                            return org.topazproject.ambra.models.MinorCorrection.RDF_TYPE;
                          }
                            public Class  isClass() { return MinorCorrection.class;      } },
    Retraction            { public String rdfType() {
                            return org.topazproject.ambra.models.Retraction.RDF_TYPE;
                          }
                            public Class  isClass() { return Retraction.class;           } },
    Trackback             { public String rdfType() {
                            return org.topazproject.ambra.models.Trackback.RDF_TYPE;
                          }
                            public Class  isClass() { return Trackback.class;           } },
    Rating                { public String rdfType() {
                            return org.topazproject.ambra.models.Rating.RDF_TYPE;
                          }
                            public Class  isClass() { return Rating.class;               } },
    RatingSummary         { public String rdfType() {
                            return org.topazproject.ambra.models.RatingSummary.RDF_TYPE;
                          }
                            public Class  isClass() { return RatingSummary.class;        } },
    Reply                 { public String rdfType() {
                            return org.topazproject.ambra.models.Reply.RDF_TYPE;
                          }
                            public Class  isClass() { return Reply.class;                } },
    Issue                 { public String rdfType() { return null;                       }
                            public Class  isClass() { return null;                       } },
    // Invalid must remain last.
    Invalid               { public String rdfType() { return null;                       }
                            public Class  isClass() { return null;                       } };

    public abstract String rdfType();
    public abstract Class  isClass();
  }

  /**
   * Constructor - currently does nothing.
   */
  public FeedService(){
  }

  /**
   * Creates and returns a new <code>Key</code> for clients of FeedService.
   *
   * @return Key a new cache key to be used as a data model for the FeedAction.
   */
  public ArticleFeedCacheKey newCacheKey() {
    return new ArticleFeedCacheKey();
  }

  /**
   * Querys the OtmService using the parameters set in <code>cacheKey</code>.
   * The routine first looks in the <code>feedCache</code> for the Id list,
   * or queries the data store if there is a miss.
   *
   * @param cacheKey is both the feedAction data model and cache key.
   * @return List&lt;String&gt; if article Ids.
   * @throws ApplicationException ApplicationException
   */
  public List<String> getArticleIds(final ArticleFeedCacheKey cacheKey) throws ApplicationException {
    // Create a local lookup based on the feed URI.
    Cache.Lookup<List<String>, ApplicationException> lookUp =
      new Cache.SynchronizedLookup<List<String>, ApplicationException>(cacheKey) {
        public List<String> lookup() throws ApplicationException {
          return fetchArticleIds(cacheKey);
        }
      };
    // Get articel ID's from the feed cache or add it
    return feedCache.get(cacheKey, -1, lookUp);
  }

  /**
   *
   * @param cacheKey is both the feedAction data model and cache key.
   * @param journal Current journal
   * @return List&lt;String&gt; if article Ids.
   * @throws ApplicationException ApplicationException
   * @throws URISyntaxException URISyntaxException
   */
  public List<String> getIssueArticleIds(final ArticleFeedCacheKey cacheKey, String journal) throws
      URISyntaxException, ApplicationException {
    List<String> articleList  = new ArrayList<String>();
    URI issurURI = (cacheKey.getIssueURI() != null) ? URI.create(cacheKey.getIssueURI()) : null;

    if (issurURI == null) {
      Journal curJrnl = journalService.getJournal(journal);
      issurURI = curJrnl.getCurrentIssue();
    }

    List<TOCArticleGroup> articleGroups = browseService.getArticleGrpList(issurURI);

    for(TOCArticleGroup ag : articleGroups)
      for(ArticleInfo article : ag.articles)
        articleList.add( article.id.toString());

    return articleList;
  }

  /**
   * Given a list of articleIds return a list of articles corresponding to the Ids.
   *
   * @param articleIds  List&lt;String&gt; of article Ids to fetch
   * @return <code>List&lt;Article&gt;</code> of articles
   * @throws ParseException ParseException
   */
  public List<Article> getArticles(List<String> articleIds) throws ParseException {
    return articleOtmService.getArticles(articleIds);
  }

  /**
   * Returns a list of annotation Ids based on parameters contained in
   * the cache key. If a start date is not specified then a default
   * date is used but not stored in the key.
   *
   * @param cacheKey cache key.
   * @return <code>List&lt;String&gt;</code> a list of annotation Ids
   * @throws ApplicationException   Converts all exceptions to ApplicationException
   */
  public List<String> getAnnotationIds(final AnnotationFeedCacheKey cacheKey)
      throws ApplicationException {

    // Create a local lookup based on the feed URI.
    Cache.Lookup<List<String>, ApplicationException> lookUp =
      new Cache.SynchronizedLookup<List<String>, ApplicationException>(cacheKey) {
        public List<String> lookup() throws ApplicationException {
          return fetchAnnotationIds(cacheKey);
        }
      };
    // Get articel ID's from the feed cache or add it
    return feedCache.get(cacheKey, -1, lookUp);
  }

  /**
   * Returns a list of reply Ids based on parameters contained in
   * the cache key. If a start date is not specified then a default
   * date is used but not stored in the key.
   *
   * @param cacheKey cache key
   * @return <code>List&lt;String&gt;</code> a list of reply Ids
   * @throws ApplicationException   Converts all exceptions to ApplicationException
   */
  public List<String> getReplyIds(final AnnotationFeedCacheKey cacheKey)
      throws ApplicationException {

    // Create a local lookup based on the feed URI.
    Cache.Lookup<List<String>, ApplicationException> lookUp =
      new Cache.SynchronizedLookup<List<String>, ApplicationException>(cacheKey) {
        public List<String> lookup() throws ApplicationException {
          return fetchReplyIds(cacheKey);
        }
      };
    // Get articel ID's from the feed cache or add it
    return feedCache.get(cacheKey, -1, lookUp);
  }

  private List<String> fetchAnnotationIds(final AnnotationFeedCacheKey cacheKey)
      throws ApplicationException {
    
    List<String> annotIds;
    try {
      annotIds = annotationService.getFeedAnnotationIds(
                 cacheKey.getStartDate(), cacheKey.getEndDate(), cacheKey.getAnnotationTypes(),
                 cacheKey.getMaxResults());

    } catch (Exception ex) {
      throw new ApplicationException(ex);
    }
    return  annotIds;
  }

  private List<String> fetchReplyIds(final AnnotationFeedCacheKey cacheKey)
      throws ApplicationException {

    List<String> replyIds;
    try {
      replyIds = annotationService.getReplyIds(
                 cacheKey.getStartDate(), cacheKey.getEndDate(), cacheKey.getAnnotationTypes(),
                 cacheKey.getMaxResults());

    } catch (Exception ex) {
      throw new ApplicationException(ex);
    }
    return  replyIds;
  }

   /**
   * Use the OTM Service to build a list of article ID's that match the query.  Build the category
   * and author list needed by the article ID query then make the query
   *
   * @param cacheKey is both the feedAction data model and cache key.
   * @return  <code>List&lt;String&gt;</code> a list of article ID's.
   * @throws  ApplicationException Converts all exceptions to ApplicationException
   */
  private List<String> fetchArticleIds(final ArticleFeedCacheKey cacheKey) throws ApplicationException {
    List<String> categoriesList = new ArrayList<String>();
    if (cacheKey.getCategory() != null && cacheKey.getCategory().length() > 0) {
      categoriesList.add(cacheKey.getCategory());
    }

    List<String> authorsList = new ArrayList<String>();
    if (cacheKey.getAuthor() != null) {
      authorsList.add(cacheKey.getAuthor());
    }

    try {
      String startDate = null;
      String endDate = null;

      if (cacheKey.getSDate() != null)
        startDate = cacheKey.getSDate().toString();

      if (cacheKey.getEDate() != null)
        endDate = cacheKey.getEDate().toString();

      return articleOtmService.getArticleIds(
              startDate, endDate,
              categoriesList.toArray(new String[categoriesList.size()]),
              authorsList.toArray(new String[authorsList.size()]),
              Article.ACTIVE_STATES, false, cacheKey.getMaxResults());
    } catch (ParseException ex) {
      throw new ApplicationException(ex);
    }
  }

  /**
   * @param journalService   Journal Service
   */
  @SuppressWarnings("synthetic-access")
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * @param articleOtmService   ArticleOtm Service
   */
  @SuppressWarnings("synthetic-access")
  @Required
  public void setArticleOtmService(ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * @param annotationService   Annotation Service
   */
  @SuppressWarnings("synthetic-access")
  @Required
  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }


  /**
   * @param browseService   Browse Service
   */
  @SuppressWarnings("synthetic-access")
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }


  /**
   * @param feedCache  Feed Cache
   */
  @SuppressWarnings("synthetic-access")
  @Required
  public void setFeedCache(Cache feedCache) {
    this.feedCache = feedCache;
    // We are order dependent on what the journal service does. So register the listener there.
    if (invalidator == null)
      feedCache.getCacheManager().registerListener(invalidator = new Invalidator());
  }

  /**
   * @param session    Otm Session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Get the UserAccount info using the Id.
   * 
   * @param id  user id
   * @return  user account
   */
  public UserAccount getUserAcctFrmID(String id) {
    UserAccount ua = session.get(UserAccount.class, id);
    if(ua == null) {
      log.error("Unable to look up UserAccount for " + id);
    }
    return ua;
  }

  /**
   * Invalidate feedCache if new articles are ingested or articles are deleted. This is accomplished
   * via a listener registered with the feed cache. This listener is notified when articles are
   * added or deleted which could potentially affect the results cached.
   *
   * The <code>Invalidator</code> uses two entry points to accomplish this.
   * <p>
   * <code>Invalidator.objectChanged</code> is called whenever an article or journal is updated.
   * <code>Invalidator.removing</code> is called whenever an article is removed.
   * <p>
   *
   * The basic process is the same for both. Loop through all the feed cache keys and see if the
   * articles that changed could potentially affect the query results of the cache entry. If it does
   * then remove that cache entry.
   *
   * @see FeedService
   */
  public class Invalidator extends AbstractObjectListener {
    /**
     * Notify the <code>Invalidator</code> that an object in the cache may have changed.
     *
     * @param session  session info (unused)
     * @param cm       class metadata  (unused)
     * @param id       (unused)
     * @param object   either <code>class Article</code> or <code>class Journal<code>
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
      } else if (object instanceof ArticleAnnotation) {
        invalidateFeedCacheForAnnotation((ArticleAnnotation)object);
      } else if (object instanceof Rating) {
        invalidateFeedCacheForAnnotation((Rating)object);
      } else if (object instanceof Trackback) {
        invalidateFeedCacheForAnnotation((Trackback)object);
      } else if (object instanceof Reply) {
        invalidateFeedCacheForReply((Reply)object);
      }
    }

    /**
     * Notify that an article is being removed. If it matches a cache entry's query criteria then
     * remove that entry from the cache.
     *
     * @param session session info (unused)
     * @param cm      class metadata (unused)
     * @param id      (unused)
     * @param object  must be <code>class Article</code>
     * @throws Exception
     */
    @Override
    public void removing(Session session, ClassMetadata cm, String id, Object object)
        throws Exception {
      // If this is an Active Article check to see if it invalidates the feed cache
      if (object instanceof Article && ((Article) object).getState() == Article.STATE_ACTIVE) {
        invalidateFeedCacheForArticle((Article) object);
      } else if (object instanceof ArticleAnnotation) {
        invalidateFeedCacheForAnnotation((ArticleAnnotation) object);
      } else if (object instanceof Rating) {
        invalidateFeedCacheForAnnotation((Rating)object);
      } else if (object instanceof Trackback) {
        invalidateFeedCacheForAnnotation((Trackback)object);
      } else if (object instanceof Reply) {
        invalidateFeedCacheForReply((Reply) object);
      }
    }

    /**
     * @param journal  the journal name
     * @param updates  updates to articles
     */
    private void invalidateFeedCacheForJournal(Journal journal, Interceptor.Updates updates) {

      /* If the simple collect changed size then assume
       * the change was an addition or deletion and
       * flush cache entries for this journal.
       */
      List<String> oldArticles = updates.getOldValue("simpleCollection");
      List<URI> curArticles = journal.getSimpleCollection();

      if ((oldArticles == null) || (curArticles==null))
        return;

      if ( oldArticles.size() != curArticles.size() )
        invalidateFeedCacheForJournalArticle(journal);
    }

    /**
     * Loop through the feed cache keys and match key parameters to the article. If the article
     * matches those found in the cache key then that query is invalid and remove it.
     *
     * @param article the article which might change the cash.
     */
    private void invalidateFeedCacheForArticle(Article article) {

      for (Object key : feedCache.getKeys()) {
        if (key instanceof ArticleFeedCacheKey) {
          if (matchesArticle((ArticleFeedCacheKey) key, article))
            feedCache.remove(key);
        } else if (key instanceof AnnotationFeedCacheKey) {
          if(matchesJournal(article, ((AnnotationFeedCacheKey)key).getJournal()))
            feedCache.remove(key);
        }
      }
    }

    /**
     * Invalidate the cache entries based on a journal and a list of article that are part of that
     * journal.
     *
     * @param journal  the journal of interest
     */
    private void invalidateFeedCacheForJournalArticle(Journal journal) {
      for (Object key : feedCache.getKeys()) {
        if (key instanceof ArticleFeedCacheKey) {
          if (((ArticleFeedCacheKey) key).getJournal().equals(journal.getKey()))
            feedCache.remove(key);
        } else if (key instanceof AnnotationFeedCacheKey) {
          if (((AnnotationFeedCacheKey)key).getJournal().equals(journal.getKey()))
            feedCache.remove(key);
        }
      }
    }

    /**
     * Invalidate the cache entries based on a annotation.
     *
     * @param annotation The annotation.
     */
    private void invalidateFeedCacheForAnnotation(Annotation annotation) {
      for (Object key : feedCache.getKeys()) {
        if (key instanceof AnnotationFeedCacheKey) {
          if (matchesAnnotation((AnnotationFeedCacheKey)key, annotation))
            feedCache.remove(key);
        }
      }
    }


    /**
     * Invalidate the cache entries based on a annotation reply.
     *
     * @param reply The reply.
     */
    private void invalidateFeedCacheForReply(Reply reply) {
      for (Object key : feedCache.getKeys()) {
        if (key instanceof AnnotationFeedCacheKey) {
          AnnotationFeedCacheKey annotationKey = (AnnotationFeedCacheKey) key;
          if (matchesReply(annotationKey, reply))
            feedCache.remove(key);
        }
      }
    }




    /**
     * This is the linchpin to the entire process. Basically, query results are currently affected
     * by 5 parameters:
     *
     * <ul>
     * <li>startDate/endDate
     * <li>author
     * <li>category
     * <li>maxResult
     * </ul>
     *
     * If the article affects the results of any one of these parameters then then the query could
     * be different and hence needs to be ejected from the cache.
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
     * @param key          the cache key and input parameters
     * @param article      article that has caused the change
     * @return boolean true if we need to remove this entry from the cache
     */
    private boolean matchesArticle(ArticleFeedCacheKey key, Article article) {
      if (!matchesJournal(article, key.getJournal()))
        return false;

      DublinCore dc = article.getDublinCore();

      return matchesDates(dc.getDate(), key.getSDate(), key.getEDate()) &&
             matchesCategory(key, article) &&
             matchesAuthor(key, dc);

    }

    private boolean matchesAnnotation(AnnotationFeedCacheKey key, Annotation annotation) {
      try {
        Article article = articleOtmService.getArticle(annotation.getAnnotates());
        if (!matchesJournal(article, key.getJournal()))
          return false;

      } catch (NoSuchArticleIdException e) {
        log.error("Failed trying to invalidate FeedCache for annotation " + annotation.getId() +
            " and key " + key.toString(), e);
        return true; // remove this cache entry
      }

      return matchesDates(annotation.getCreated(), key.getStartDate(), key.getEndDate()) &&
             matchesAnnotationType(key, annotation);

    }


    private boolean matchesReply(AnnotationFeedCacheKey key, Reply reply) {

      if (key.getType() != AnnotationFeedCacheKey.Type.REPLIES)
        return false;

      Annotation annotation = annotationService.getArticleAnnotation(reply.getRoot());
      if (annotation != null) {
        try {
          Article article = articleOtmService.getArticle(annotation.getAnnotates());
          if (article == null || !matchesJournal(article, key.getJournal()))
            return false;
        } catch (NoSuchArticleIdException e) {
          log.error("Failed trying to invalidate FeedCache for reply " + reply.getId() +
              " and key " + key.toString(), e);
          return true; // remove this cache entry
        }

        if (!matchesAnnotationType(key, annotation))
          return false;

      } else {
        log.error("Root annotation not found for reply " + reply.getId());
        return true;  // remove this cache entry
      }

      return matchesDates(reply.getCreated(), key.getStartDate(), key.getEndDate());

    }


    /**
     * Compares the author in the cache key to the creators specified in Dublin core.
     * If key.author = null return match
     * If key.author = one of the creators return match
     * Else return no match.
     *
     * @param key the cache key
     * @param dc  Dublin core field from Article
     * @return  boolean true if there is a match
     */
    private boolean matchesAuthor(ArticleFeedCacheKey key, DublinCore dc) {
      boolean matches = false;

      if (key.getAuthor() != null) {
        for (String author : dc.getCreators()) {
          if (key.getAuthor().equalsIgnoreCase(author)) {
            matches = true;
            break;
          }
        }
      } else {
        matches = true;
      }
      return matches;
    }

    /**
     * Compare key.categery to the article.category
     * If the key.category = null then wildcard match
     * Else If the key.category = article.category then match
     * Else return no match .
     *
     * @param key      a cache key and actiopn data model
     * @param article  the article
     *
     * @return boolean true if the category matches (key.category = null is wildcard)
     */
    private boolean matchesCategory(ArticleFeedCacheKey key, Article article) {
      boolean matches = false;

      if (key.getCategory() != null) {
        for (Category category : article.getCategories()) {
          if (category.getMainCategory().equalsIgnoreCase(key.getCategory())) {
            matches = true;
            break;
          }
        }
      } else
        matches = true;

      return matches;
    }

    private boolean matchesAnnotationType(AnnotationFeedCacheKey key, Annotation annotation) {

      return key.getAnnotationTypes() == null ||
             key.getAnnotationTypes().size() == 0 ||
             key.getAnnotationTypes().contains(FEED_TYPES.Annotation.rdfType()) ||
             annotation instanceof FormalCorrection &&
                key.getAnnotationTypes().contains(FEED_TYPES.FormalCorrection.rdfType()) ||
             annotation instanceof MinorCorrection &&
                key.getAnnotationTypes().contains(FEED_TYPES.MinorCorrection.rdfType()) ||
             annotation instanceof Comment &&
                key.getAnnotationTypes().contains(FEED_TYPES.Comment.rdfType()) ||
             annotation instanceof Retraction &&
                key.getAnnotationTypes().contains(FEED_TYPES.Retraction.rdfType());

    }


    /**
     * Check to see if the article date is between the start and end date specified in the key. If
     * it is then return true and the entry for this key should be removed.
     *
     * @param createDate date object is created
     * @param startDate Start date, can be null.
     * @param endDate End date can be null.
     * @return boolean true if the article date falls between the start and end date
     */
    private boolean matchesDates(Date createDate, Date startDate, Date endDate) {

      boolean matches = false;

      // If start and end are null then it doesn't matter what the article date is.
      if ((endDate == null) && (startDate == null)) {
        matches = true;
      } else if (createDate != null) {
        if ((endDate == null) && createDate.after(startDate)) {
          matches = true;
        } else if ((startDate == null) && createDate.before(endDate)) {
          matches = true;
        } else if (createDate.after(startDate) && createDate.before(endDate)) {
          matches = true;
        }
      }
      return matches;
    }

    /**
     * Loop thorugh the Journals to see if the key.journal matches one of the journals the list of
     * journals the article belongs to.
     *
     * @param article  the article
     * @param journalKey Journal
     * @return boolean true if key.journal matches one of the journals returned
     *         by the journal service.
     */
    private boolean matchesJournal(Article article, String journalKey) {
      boolean matches = false;

      if (journalKey != null) {
        for (Journal journal : journalService.getJournalsForObject(article.getId())) {
          if (journal.getKey().equals(journalKey)) {
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
