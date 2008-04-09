/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.article.service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.plos.journal.JournalService;
import org.plos.model.IssueInfo;
import org.plos.model.VolumeInfo;
import org.plos.model.article.ArticleInfo;
import org.plos.model.article.ArticleType;
import org.plos.model.article.RelatedArticleInfo;
import org.plos.model.article.Years;
import org.plos.models.Article;
import org.plos.models.Issue;
import org.plos.models.Journal;
import org.plos.models.PLoS;
import org.plos.models.Volume;
import org.plos.util.CacheAdminHelper;
import org.plos.web.VirtualJournalContext;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.util.TransactionHelper;

/**
 * Class to get all Articles in system and organize them by date and by category
 *
 * @author Alex Worden, stevec
 */
public class BrowseService {
  private static final Log log = LogFactory.getLog(BrowseService.class);

  private static final String CAT_INFO_LOCK       = "CatLock-";
  private static final String CAT_INFO_KEY        = "CatInfo-";
  private static final String ARTBYCAT_LIST_KEY   = "ArtByCat-";

  private static final String DATE_LIST_LOCK      = "DateLock-";
  private static final String DATE_LIST_KEY       = "DateList-";

  private static final String ARTBYDATE_LIST_LOCK = "ArtByDateLock-";
  private static final String ARTBYDATE_LIST_KEY  = "ArtByDate-";

  public static final String ARTICLE_LOCK        = "ArticleLock-";
  public static final String ARTICLE_KEY         = "Article-";
  
  public static final String ISSUE_LOCK        = "IssueLock-";
  public static final String ISSUE_KEY         = "Issue-";

  public static final String VOL_INFOS_FOR_JOURNAL_KEY = "VolInfos_";

  private final ArticlePEP     pep;
  private       Session        session;
  private       Ehcache        browseCache;
  private       JournalService journalService;

  private CacheAdminHelper cahelper;


  /**
   * Create a new instance.
   */
  public BrowseService() throws IOException {
    pep = new ArticlePEP();
  }

  /**
   * Get the dates of all articles. The outer map is a map of years, the next inner map a map
   * of months, and finally the innermost is a list of days.
   *
   * @return the article dates.
   */
  public Years getArticleDates() {
    return getArticleDates(true, getCurrentJournal());
  }

  private Years getArticleDates(boolean load, String jnlName) {
    String key  = DATE_LIST_KEY + jnlName;
    Object lock = (DATE_LIST_LOCK + jnlName).intern();

    return
      cahelper.getFromCache(browseCache, key, -1, lock,
                            "article dates", !load ? null : new CacheAdminHelper.EhcacheUpdater<Years>() {
        public Years lookup() {
          return loadArticleDates();
        }
      });
  }

  private void updateArticleDates(Years dates, String jnlName) { 
    browseCache.put(new Element(DATE_LIST_KEY + jnlName, dates)); 
  } 

  /**
   * Get articles in the given category. One "page" of articles will be returned, i.e. articles
   * pageNum * pageSize .. (pageNum + 1) * pageSize - 1 . Note that less than a pageSize articles
   * may be returned, either because it's the end of the list or because some articles are not
   * accessible.
   *
   * @param catName  the category for which to return the articles
   * @param pageNum  the page-number for which to return articles; 0-based
   * @param pageSize the number of articles per page
   * @param numArt   (output) the total number of articles in the given category
   * @return the articles.
   */
  public List<ArticleInfo> getArticlesByCategory(final String catName, int pageNum, int pageSize,
                                                 int[] numArt) {
    List<URI> uris = ((SortedMap<String, List<URI>>)
                        getCatInfo(ARTBYCAT_LIST_KEY, "articles by category ", true)).get(catName);

    if (uris == null) {
      numArt[0] = 0;
      return null;
    } else {
      numArt[0] = uris.size();
      return loadArticles(uris, pageNum, pageSize);
    }
  }


  /**
   * Get articles in the given date range, from newest to olders. One "page" of articles will be
   * returned, i.e. articles pageNum * pageSize .. (pageNum + 1) * pageSize - 1 . Note that less
   * than a pageSize articles may be returned, either because it's the end of the list or because
   * some articles are not accessible.
   *
   * <p>Note: this method assumes the dates are truly just dates, i.e. no hours, minutes, etc.
   *
   * @param startDate the earliest date for which to return articles (inclusive)
   * @param endDate   the latest date for which to return articles (exclusive)
   * @param pageNum   the page-number for which to return articles; 0-based
   * @param pageSize  the number of articles per page, or -1 for all articles
   * @param numArt    (output) the total number of articles in the given category
   * @return the articles.
   */
  public List<ArticleInfo> getArticlesByDate(final Calendar startDate, final Calendar endDate,
                                             int pageNum, int pageSize, int[] numArt) {
    String jnlName = getCurrentJournal();
    String mod     = jnlName + "-" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis();
    String key     = ARTBYDATE_LIST_KEY + mod;
    Object lock    = (ARTBYDATE_LIST_LOCK + mod).intern();
    int    ttl     = getSecsTillMidnight();

    List<URI> uris =
      cahelper.getFromCache(browseCache, key, ttl, lock,
                                    "articles by date", new CacheAdminHelper.EhcacheUpdater<List<URI>>() {
        public List<URI> lookup() {
          return loadArticlesByDate(startDate, endDate);
        }
      });

    if (uris == null) {
      numArt[0] = 0;
      return null;
    } else {
      numArt[0] = uris.size();
      return loadArticles(uris, pageNum, pageSize);
    }
  }

  private static final int getSecsTillMidnight() {
    Calendar cal = Calendar.getInstance();
    long now = cal.getTimeInMillis();

    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE,      0);
    cal.set(Calendar.SECOND,      0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.DATE,        1);

    return (int) ((cal.getTimeInMillis() - now) / 1000);
  }

  /**
   * Get a list of article-counts for each category.
   *
   * @return the category infos.
   */
  public SortedMap<String, Integer> getCategoryInfos() {
    return (SortedMap<String, Integer>) getCatInfo(CAT_INFO_KEY, "category infos", true);
  }

  /**
	 * Get Issue information.
	 * 
	 * @param issue
	 *          DOI of Issue.
	 * @return the Issue information.
	 */
	public IssueInfo getIssueInfo(final URI doi) {
	  return cahelper.getFromCache(browseCache, ISSUE_KEY + doi, -1,
          ISSUE_LOCK + doi,
          "issue " + doi, new CacheAdminHelper.EhcacheUpdater<IssueInfo>() {
            public IssueInfo lookup() {
              return getIssueInfo2(doi);
            }
          });
    } 

  /**
   * Get Issue information - creates a transaction then calls getIssueInfoInTx. 
   * FYI - I've left this in here to make the merge back to head easier and make the
   * readability of getIssueInfo() easier. 
   *
   * @param issue DOI of Issue.
   * @param eagerFetchArticles causes the ArticleInfos to be fetched
   * @return the Issue information.
   */
  private IssueInfo getIssueInfo2(final URI doi) {

    // XXX look up IssueInfo in Cache

    // OTM usage wants to be in a Transaction
    return TransactionHelper.doInTx(session,
      new TransactionHelper.Action<IssueInfo>() {

        // TODO should all of this be in a tx???

        public IssueInfo run(Transaction tx) {

          return getIssueInfoInTx(doi);
        }
      });
  }
	
  /**
	 * Get Issue information inside of a Transaction.
	 * 
	 * @param issue
	 *          DOI of Issue.
	 * @return the Issue information.
	 */
  private IssueInfo getIssueInfoInTx(final URI issueDOI) {

    // XXX look up IssueInfo in Cache

    // get the Issue
    final Issue issue = session.get(Issue.class, issueDOI.toString());
    if (issue == null) {
      log.error("Faiiled to retrieve Issue for doi='"+issueDOI.toString()+"'");
      return null; 
    }

    // get the image Article
    URI imageArticle = null;
    String description = null;
    if (issue.getImage() != null
        && issue.getImage().toString().length() != 0) {
      final Article article = session.get(Article.class, issue.getImage().toString());
      if (article != null) {
        imageArticle = issue.getImage();
        description = article.getDublinCore().getDescription();
      }
    }
    
    // derive prev/next Issue, "parent" Volume
    URI prevIssueURI = null;
    URI nextIssueURI = null;
    Volume parentVolume = null;
    
    // String oqlQuery = "select v from Volume v where v.issueList = <" + issueDOI + "> ;";
    // Results results = session.createQuery(oqlQuery).execute();
    
    Results results = session.createQuery("select v from Volume v where v.issueList = :doi ;")
    .setParameter("doi", issueDOI).execute();
    
    results.beforeFirst();
    if (results.next()) {
      parentVolume = (Volume)results.get("v"); 
    }
    if (parentVolume != null) {
      final List<URI> issues = parentVolume.getIssueList();
      final int issuePos = issues.indexOf(issueDOI);
      prevIssueURI = (issuePos == 0) ? null : issues.get(issuePos - 1);
      nextIssueURI = (issuePos == issues.size() - 1) ? null : issues.get(issuePos + 1);
    } else {
      log.warn("Issue: " + issue.getId() + ", not contained in any Volumes");
    }

    IssueInfo issueInfo = new IssueInfo(issue.getId(), issue.getDisplayName(), prevIssueURI, nextIssueURI,
                                        imageArticle, description, parentVolume.getId());
    issueInfo.setArticleUriList(issue.getSimpleCollection());
    return issueInfo;
  }
  
  /**
   * Returns the list of ArticleInfos contained in this Issue. The list will contain only ArticleInfos for 
   * Articles that the current user has permission to view. 
   * 
   * @param issueDOI
   * @return
   */
  public List<ArticleInfo> getArticleInfosForIssue(final URI issueDOI) {
    
    IssueInfo iInfo = getIssueInfo(issueDOI);
    List<ArticleInfo> aInfos = new ArrayList<ArticleInfo>();
    
    for (URI articleDoi : iInfo.getArticleUriList()) {
      ArticleInfo ai = getArticleInfo(articleDoi);
      if (ai == null) {
        log.warn("Article " + articleDoi + " missing; member of Issue " + issueDOI);
        continue;
      }
      aInfos.add(ai);
    }
    return aInfos;
  }

  /**
   * Get a VolumeInfo for the given id. This only works if the volume is in the current journal.
   * 
   * @param id
   * @return
   */
  public VolumeInfo getVolumeInfo(URI id) {
    // Attempt to get the volume infos from the cached journal list... 
    List<VolumeInfo> volumes = getVolumeInfosForJournal(journalService.getCurrentJournal(session));
    for (VolumeInfo vol : volumes) {
      if (id.equals(vol.getId())) {
        return vol;
      }
    }
    
    // If we have no luck with the cached journal list, attempt to load the volume re-using loadVolumeInfos();
    List<URI> l = new ArrayList<URI>();
    l.add(id);
    List<VolumeInfo> vols = loadVolumeInfos(l);
    if (vols != null && vols.size() > 0) {
      return vols.get(0);
    }
    return null;
  }
  
  /**
   * Returns a list of VolumeInfos for the given Journal.
   * VolumeInfos are sorted in reverse order to reflect most common usage.
   * Uses the CacheAdminHelper pull-through cache. 
   * 
   * @param journal To find VolumeInfos for.
   * @return VolumeInfos for journal in reverse order.
   */
  public List<VolumeInfo> getVolumeInfosForJournal(final Journal journal) {
    
    String key = (VOL_INFOS_FOR_JOURNAL_KEY + (journal.getKey())).intern();
    
    return cahelper.getFromCache(browseCache, key, -1, key,
                                         "List of volumes for journal" + journal.getId(), new CacheAdminHelper.EhcacheUpdater<List<VolumeInfo>>() {
       public List<VolumeInfo> lookup() {
         final List<URI> volumeDois = journal.getVolumes();
         List<VolumeInfo> volumeInfos = loadVolumeInfos(volumeDois);
         Collections.reverse(volumeInfos);
         return volumeInfos;
       }});
  }
  
  /**
   * Get VolumeInfos. Note that the IssueInfos contained in the volumes have not been instantiated
   * with the ArticleInfos. 
   *
   * @param volumeDois to look up.
   * @return volumeInfos.
   */
  public List<VolumeInfo> loadVolumeInfos(final List<URI> volumeDois) {

    // XXX look up VolumeInfos in Cache

    // OTM usage wants to be in a Transaction
    return TransactionHelper.doInTx(session,
      new TransactionHelper.Action<List<VolumeInfo>>() {

        // TODO should all of this be in a tx???
        public List<VolumeInfo> run(Transaction tx) {

          List<VolumeInfo> volumeInfos = new ArrayList<VolumeInfo>();
          // get the Volumes
          for (int onVolumeDoi = 0; onVolumeDoi < volumeDois.size(); onVolumeDoi++) {
            final URI volumeDoi = volumeDois.get(onVolumeDoi);
            final Volume volume  = session.get(Volume.class, volumeDoi.toString());
            if (volume == null) {
              log.error("unable to load Volume: " + volumeDoi);
              continue;
            }
            // get the image Article, may be null
            URI imageArticle = null;
            String description = null;
            if (volume.getImage() != null) {
              final Article article = session.get(Article.class, volume.getImage().toString());
              if (article != null) {
                imageArticle = volume.getImage();
                description = article.getDublinCore().getDescription();
              }
            }

            List<IssueInfo> issueInfos = new ArrayList<IssueInfo>();
            for (final URI issueDoi : volume.getIssueList()) {
              issueInfos.add(getIssueInfo(issueDoi));
            }

            // calculate prev/next
            final URI prevVolumeDoi = (onVolumeDoi == 0) ? null : volumeDois.get(onVolumeDoi - 1);
            final URI nextVolumeDoi = (onVolumeDoi == volumeDois.size() - 1) ? null
                                                      : volumeDois.get(onVolumeDoi + 1);
            final VolumeInfo volumeInfo = new VolumeInfo(volume.getId(), volume.getDisplayName(),
                    prevVolumeDoi, nextVolumeDoi, imageArticle, description, issueInfos);
            volumeInfos.add(volumeInfo);
          }
          
          return volumeInfos;
        }
      });
  }
  
  private Object getCatInfo(String key, String desc, boolean load) {
    return getCatInfo(key, desc, load, getCurrentJournal());
  }

  private Object getCatInfo(String key, String desc, boolean load, String jnlName) {
    key += jnlName;
    
    if (session != null && session.getTransaction() != null) {
      log.warn("WARNING: getCatInfo is in an active OTM transaction and was about to enter a synchronized " +
      		"block for the entire journal that in turn calls OTM. This could have resulted in a deadlock" +
      		"so it was removed!");
    }
    
    Element e = browseCache.get(key);
    
    if (e == null) {
      if (!load)
        return null;
      
      if (log.isDebugEnabled())
        log.debug("retrieving " + desc + " from db");
      
      loadCategoryInfos(jnlName);
      e = browseCache.get(key);
    } else if (log.isDebugEnabled()) {
      log.debug("retrieved " + desc + " from cache");
    }
    
    return e.getValue();
  }

  private String getCurrentJournal() {
    return ((VirtualJournalContext) ServletActionContext.getRequest().
               getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT)).getJournal();
  }

  /**
   * Notify this service that articles have been deleted from the system. This should be invoked
   * before the journal-service is updated.
   *
   * @param uris the list of id's of the deleted articles
   */
  public void notifyArticlesDeleted(String[] uris) {
    // create list of articles for each journal
    Map<String, Set<String>> artMap = getArticlesByJournal(uris);

    // process each journal
    for (String jnlName : artMap.keySet()) {
      // update category lists
      synchronized ((CAT_INFO_LOCK + jnlName).intern()) {
        SortedMap<String, List<URI>> artByCat = (SortedMap<String, List<URI>>)
            getCatInfo(ARTBYCAT_LIST_KEY, "articles by category ", false, jnlName);
        if (artByCat != null) {
          Set<URI> uriSet = new HashSet<URI>();
          for (String uri : artMap.get(jnlName))
            uriSet.add(URI.create(uri));

          cat: for (Iterator<List<URI>> catIter = artByCat.values().iterator(); catIter.hasNext(); ) {
            List<URI> articles = catIter.next();
            for (Iterator<URI> artIter = articles.iterator(); artIter.hasNext(); ) {
              URI art = artIter.next();
              if (uriSet.remove(art)) {
                artIter.remove();

                if (articles.isEmpty())
                  catIter.remove();

                if (uriSet.isEmpty())
                  break cat;
              }
            }
          }

          updateCategoryCaches(artByCat, jnlName);
        }
      }

      // update date lists
      browseCache.remove(DATE_LIST_KEY + jnlName);

      for (Object key : browseCache.getKeysNoDuplicateCheck()) {
        if (key instanceof String && ((String) key).startsWith(ARTBYDATE_LIST_KEY + jnlName))
          browseCache.remove(key);
      }
    }

    // update article cache
    for (String uri : uris)
      browseCache.remove(ARTICLE_KEY + uri);
  }

  /**
   * Notify this service that articles have been added to the system. This should be invoked
   * after the journal-service is updated.
   *
   * @param uris the list of id's of the added articles
   */
  public void notifyArticlesAdded(String[] uris) {
    // create list of articles for each journal
    Map<String, Set<String>> artMap = getArticlesByJournal(uris);

    // process each journal
    for (String jnlName : artMap.keySet()) {
      // get some info about the new articles
      List<NewArtInfo> nais = getNewArticleInfos(artMap.get(jnlName));

      // update category lists
      synchronized ((CAT_INFO_LOCK + jnlName).intern()) {
        final SortedMap<String, List<URI>> artByCat = (SortedMap<String, List<URI>>)
                            getCatInfo(ARTBYCAT_LIST_KEY, "articles by category ", false, jnlName);
        if (artByCat != null) {
          for (NewArtInfo nai: nais) {
            List<URI> arts = artByCat.get(nai.category);
            if (arts == null)
              artByCat.put(nai.category, arts = new ArrayList<URI>());
            arts.add(0, nai.id);
          }

          updateCategoryCaches(artByCat, jnlName);
        }
      }

      // update date lists
      synchronized ((DATE_LIST_LOCK + jnlName).intern()) {
        Years dates = getArticleDates(false, jnlName);
        if (dates != null) {
          for (NewArtInfo nai: nais)
            insertDate(dates, nai.date);
        }
        updateArticleDates(dates, jnlName);
      }

      for (Object key : browseCache.getKeysNoDuplicateCheck()) {
        if (key instanceof String && ((String) key).startsWith(ARTBYDATE_LIST_KEY + jnlName))
          browseCache.remove(key);
      }
    }
  }

  /**
   * Build list of articles for each journal.
   */
  private Map<String, Set<String>> getArticlesByJournal(String[] artUris) {
    Map<String, Set<String>> artMap = new HashMap<String, Set<String>>();

    for (String uri : artUris) {
      for (Journal j : journalService.getJournalsForObject(URI.create(uri))) {
        Set<String> artList = artMap.get(j.getKey());
        if (artList == null)
          artMap.put(j.getKey(), artList = new HashSet<String>());
        artList.add(uri);
      }
    }

    return artMap;
  }

  /**
   * Notify this service that a journal definition has been modified. Should only be called when
   * the rules determining articles belonging to this journal have been changed.
   *
   * @param jnlName the key of the journal that was modified
   */
  public void notifyJournalModified(final String jnlName) {
    synchronized ((CAT_INFO_LOCK + jnlName).intern()) {
      browseCache.remove(CAT_INFO_KEY + jnlName);
      browseCache.remove(ARTBYCAT_LIST_KEY + jnlName);
      browseCache.remove(BrowseService.VOL_INFOS_FOR_JOURNAL_KEY + jnlName);
    }

    synchronized ((DATE_LIST_LOCK + jnlName).intern()) {
      browseCache.remove(DATE_LIST_KEY + jnlName);
    }

    for (Object key : browseCache.getKeysNoDuplicateCheck()) {
      if (key instanceof String && ((String) key).startsWith(ARTBYDATE_LIST_KEY + jnlName))
        browseCache.remove(key);
    }
  }

  /**
   * Load all (cat, art.id) from the db and stick the stuff in the cache.
   *
   * Loading (cat, art-id) from the db and doing the counting ourselves is faster than loading
   * (cat, count(art-id)) (at least 10 times as fast with 50'000 articles in 10'000 categories).
   */
  private void loadCategoryInfos(String jnlName) {
    // get all article-ids in all categories
    SortedMap<String, List<URI>> artByCat =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<SortedMap<String, List<URI>>>() {
        public SortedMap<String, List<URI>> run(Transaction tx) {
          Results r = tx.getSession().createQuery(
              "select cat, a.id articleId, a.dublinCore.date date from Article a " +
              "where cat := a.categories.mainCategory order by date desc, articleId;").execute();

          SortedMap<String, List<URI>> artByCat = new TreeMap<String, List<URI>>();
          r.beforeFirst();
          while (r.next()) {
            String cat = r.getString(0);
            List<URI> ids = artByCat.get(cat);
            if (ids == null)
              artByCat.put(cat, ids = new ArrayList<URI>());
            ids.add(r.getURI(1));
          }

          return artByCat;
        }
      });

    updateCategoryCaches(artByCat, jnlName);
  }

  private void updateCategoryCaches(SortedMap<String, List<URI>> artByCat, String jnlName) {
    // calculate number of articles in each category
    SortedMap<String, Integer> catSizes = new TreeMap<String, Integer>();
    for (Map.Entry<String, List<URI>> e : artByCat.entrySet())
      catSizes.put(e.getKey(), e.getValue().size());

    // save in cache
    browseCache.put(new Element(ARTBYCAT_LIST_KEY + jnlName, artByCat));
    browseCache.put(new Element(CAT_INFO_KEY + jnlName, catSizes));
  }

  private ArticleInfo loadArticleInfo(final URI id) {
    return TransactionHelper.doInTx(session, new TransactionHelper.Action<ArticleInfo>() {
      public ArticleInfo run(Transaction tx) {
        Results r = tx.getSession()
            .createQuery(
                         "select a.id, dc.date, dc.title, ci, " + "(select a.articleType from Article aa), "
                             + "(select aa2.id, aa2.dublinCore.title from Article aa2 "
                             + "   where aa2 = a.relatedArticles.article) "
                             + "from Article a, BrowseService$CitationInfo ci "
                             + "where a.id = :id and dc := a.dublinCore and ci.id = dc.bibliographicCitation.id;")
            .setParameter("id", id).execute();
        
        r.beforeFirst();
        if (!r.next())
          return null;
        
        ArticleInfo ai = new ArticleInfo();
        ai.setId(id);
        ai.setDate(r.getLiteralAs(1, Date.class));
        ai.setTitle(r.getString(2));
        
        for (UserProfileInfo upi : ((CitationInfo) r.get(3)).authors) {
          upi.hashCode(); // force load
          ai.addAuthor(upi.realName);
        }
        
        Results sr = r.getSubQueryResults(4);
        while (sr.next()) {
          ai.articleTypes.add(ArticleType.getArticleTypeForURI(sr.getURI(0), true));
        }
        
        sr = r.getSubQueryResults(5);
        while (sr.next()) {
          ai.addRelatedArticle(new RelatedArticleInfo(sr.getURI(0), sr.getString(1)));
        }
        
        if (log.isDebugEnabled())
          log.debug("loaded ArticleInfo: id='" + ai.getId() + 
                    "', articleTypes='" + ai.getArticleTypes() + 
                    "', date='" + ai.getDate() +
                    "', title='" + ai.getTitle() + 
                    "', authors='" + ai.getAuthors() + 
                    "', related-articles='" + ai.getRelatedArticles() + "'");
        
        return ai;
      }
    });
  }

  private Years loadArticleDates() {
    return TransactionHelper.doInTx(session, new TransactionHelper.Action<Years>() {
      public Years run(Transaction tx) {
        Results r = tx.getSession().createQuery(
            "select a.dublinCore.date from Article a;").execute();

        Years dates = new Years();

        r.beforeFirst();
        while (r.next()) {
          String  date = r.getString(0);
          Integer y    = getYear(date);
          Integer m    = getMonth(date);
          Integer d    = getDay(date);
          dates.getMonths(y).getDays(m).add(d);
        }

        return dates;
      }
    });
  }

  private static final Integer getYear(String date) {
    return Integer.valueOf(date.substring(0, 4));
  }

  private static final Integer getMonth(String date) {
    return Integer.valueOf(date.substring(5, 7));
  }

  private static final Integer getDay(String date) {
    return Integer.valueOf(date.substring(8, 10));
  }

  private static void insertDate(Years dates, Date newDate) {
    Calendar newCal = Calendar.getInstance();
    newCal.setTime(newDate);

    int y = newCal.get(Calendar.YEAR);
    int m = newCal.get(Calendar.MONTH) + 1;
    int d = newCal.get(Calendar.DATE);

    dates.getMonths(y).getDays(m).add(d);
  }

  private List<URI> loadArticlesByDate(final Calendar startDate, final Calendar endDate) {
    // XsdDateTimeSerializer formats dates in UTC, so make sure that doesn't change the date
    final Calendar sd = (Calendar) startDate.clone();
    sd.add(Calendar.MILLISECOND, sd.get(Calendar.ZONE_OFFSET) + sd.get(Calendar.DST_OFFSET));
    // ge(date, date) is currently broken, so tweak the start-date instead
    sd.add(Calendar.MILLISECOND, -1);

    return TransactionHelper.doInTx(session, new TransactionHelper.Action<List<URI>>() {
      public List<URI> run(Transaction tx) {
        Results r = tx.getSession().createQuery(
            "select a.id articleId, date from Article a where " +
            "date := a.dublinCore.date and gt(date, :sd) and lt(date, :ed) order by date desc, articleId;").
            setParameter("sd", sd).setParameter("ed", endDate).execute();

        List<URI> dates = new ArrayList<URI>();

        r.beforeFirst();
        while (r.next())
          dates.add(r.getURI(0));

        return dates;
      }
    });
  }

  private List<ArticleInfo> loadArticles(final List<URI> ids, int pageNum, int pageSize) {
    final int beg = (pageSize > 0) ? pageNum * pageSize : 0;
    final int end = (pageSize > 0) ? Math.min((pageNum + 1) * pageSize, ids.size()) : ids.size();

    return TransactionHelper.doInTx(session, new TransactionHelper.Action<List<ArticleInfo>>() {
      public List<ArticleInfo> run(Transaction tx) {
        List<ArticleInfo> res = new ArrayList<ArticleInfo>();

        for (int idx = beg; idx < end; idx++) {
          URI id = ids.get(idx);

          ArticleInfo ai = getArticleInfo(id);
          if (ai != null)
            res.add(ai);
        }

        return res;
      }
    });
  }

  public ArticleInfo getArticleInfo(final URI id) {
    try {
      pep.checkAccess(ArticlePEP.READ_META_DATA, id);
    } catch (SecurityException se) {
      if (log.isDebugEnabled())
        log.debug("Filtering URI " + id + " from Article list due to PEP SecurityException", se);
      return null;
    }

    return
      cahelper.getFromCache(browseCache, ARTICLE_KEY + id, -1,
                                    ARTICLE_LOCK + id,
                                    "article " + id, new CacheAdminHelper.EhcacheUpdater<ArticleInfo>() {
        public ArticleInfo lookup() {
          return loadArticleInfo(id);
        }
      });
  }

  private List<NewArtInfo> getNewArticleInfos(final Set<String> uris) {
    return TransactionHelper.doInTx(session, new TransactionHelper.Action<List<NewArtInfo>>() {
      public List<NewArtInfo> run(Transaction tx) {
        String query =
            "select cat, a.id articleId, a.dublinCore.date date from Article a " +
            "where cat := a.categories.mainCategory and (";
        for (String uri : uris)
          query += "a.id = <" + uri + "> or ";
        query = query.substring(0, query.length() - 4) + ") order by date desc, articleId;";

        Results r = tx.getSession().createQuery(query).execute();

        List<NewArtInfo> res = new ArrayList<NewArtInfo>();
        r.beforeFirst();
        while (r.next()) {
          NewArtInfo nai = new NewArtInfo();
          nai.category = r.getString(0);
          nai.id       = r.getURI(1);
          nai.date     = r.getLiteralAs(2, Date.class);
          res.add(nai);
        }

        return res;
      }
    });
  }

  private static class NewArtInfo {
    public URI          id;
    public Date         date;
    public String       category;
  }

  /**
   * @param journalService The journal-service to use.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * @param browseCache The browse-cache to use.
   */
  @Required
  public void setBrowseCache(Ehcache browseCache) {
    this.browseCache = browseCache;
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session The otm session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Just the list of authors.
   */
  @Entity(type = PLoS.bibtex + "Entry", model = "ri")
  public static class CitationInfo {
    @Id
    public URI id;

    @Predicate(uri = PLoS.plos + "hasAuthorList", storeAs = Predicate.StoreAs.rdfSeq)
    public List<UserProfileInfo> authors = new ArrayList<UserProfileInfo>();
  }

  /**
   * Just the full name.
   */
  @Entity(type = Rdf.foaf + "Person", model = "profiles")
  public static class UserProfileInfo {
    @Id
    public URI id;

    @Predicate(uri = Rdf.foaf + "name")
    public String realName;
  }
  
  /**
   * Spring injected method to set the CacheAdminHelper. 
   * 
   * @param cah - the Spring injected CacheAdminHelper
   */
  @Required
  public void setCacheAdminHelper(CacheAdminHelper cah) {
    this.cahelper = cah;
  }
}
