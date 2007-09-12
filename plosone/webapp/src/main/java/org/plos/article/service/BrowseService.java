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
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts2.ServletActionContext;

import org.plos.util.CacheAdminHelper;
import org.plos.web.VirtualJournalContext;

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.util.TransactionHelper;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Required;

/**
 * Class to get all Articles in system and organize them by date and by category
 *
 * @author stevec
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

  private static final String ARTICLE_LOCK        = "ArticleLock-";
  private static final String ARTICLE_KEY         = "Article-";

  private final ArticlePEP pep;
  private       Session    session;
  private       Ehcache    browseCache;


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
  public SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>> getArticleDates() {
    return getArticleDates(true);
  }

  private SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>> getArticleDates(boolean load) {
    String jnlName = getCurrentJournal();
    String key     = DATE_LIST_KEY + jnlName;
    Object lock    = (DATE_LIST_LOCK + jnlName).intern();

    return
      CacheAdminHelper.getFromCache(browseCache, key, -1, lock, "article dates",
                                    load ? new CacheAdminHelper.EhcacheUpdater<SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>>>() {
        public SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>> lookup() {
          return loadArticleDates();
        }
      } : null);
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
      return getArticles(uris, pageNum, pageSize);
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
      CacheAdminHelper.getFromCache(browseCache, key, ttl, lock, "articles by date",
                                    new CacheAdminHelper.EhcacheUpdater<List<URI>>() {
        public List<URI> lookup() {
          return loadArticlesByDate(startDate, endDate);
        }
      });

    if (uris == null) {
      numArt[0] = 0;
      return null;
    } else {
      numArt[0] = uris.size();
      return getArticles(uris, pageNum, pageSize);
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

  private Object getCatInfo(String key, String desc, boolean load) {
    String jnlName = getCurrentJournal();
    key += jnlName;

    synchronized ((CAT_INFO_LOCK + jnlName).intern()) {
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
  }

  private String getCurrentJournal() {
    return ((VirtualJournalContext) ServletActionContext.getRequest().
               getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT)).getJournal();
  }

  /**
   * Notify this service that articles have been deleted from the system.
   *
   * @param uris the list of id's of the deleted articles
   */
  public void notifyArticlesDeleted(String[] uris) {
    String jnlName = getCurrentJournal();

    // update category lists
    synchronized ((CAT_INFO_LOCK + jnlName).intern()) {
      SortedMap<String, List<URI>> artByCat = (SortedMap<String, List<URI>>)
          getCatInfo(ARTBYCAT_LIST_KEY, "articles by category ", false);
      if (artByCat != null) {
        Set<URI> uriSet = new HashSet<URI>();
        for (String uri : uris)
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

    // update article cache
    for (String uri : uris)
      browseCache.remove(ARTICLE_KEY + uri);
  }

  /**
   * Notify this service that articles have been added to the system.
   *
   * @param uris the list of id's of the added articles
   */
  public void notifyArticlesAdded(String[] uris) {
    String jnlName = getCurrentJournal();

    // get some info about the new articles
    List<NewArtInfo> nais = getNewArticleInfos(uris);

    // update category lists
    synchronized ((CAT_INFO_LOCK + jnlName).intern()) {
      final SortedMap<String, List<URI>> artByCat = (SortedMap<String, List<URI>>)
                                    getCatInfo(ARTBYCAT_LIST_KEY, "articles by category ", false);
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
      SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>> dates = getArticleDates(false);
      if (dates != null) {
        for (NewArtInfo nai: nais)
          insertDate(dates, nai.date);
      }
    }

    for (Object key : browseCache.getKeysNoDuplicateCheck()) {
      if (key instanceof String && ((String) key).startsWith(ARTBYDATE_LIST_KEY + jnlName))
        browseCache.remove(key);
    }
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
              "select cat, a.id, a.dublinCore.date date from Article a " +
              "where cat := a.categories.mainCategory order by date desc;").execute();

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
    return
      TransactionHelper.doInTx(session, new TransactionHelper.Action<ArticleInfo>() {
        public ArticleInfo run(Transaction tx) {
          Results r = tx.getSession().createQuery(
              "select a.id, dc.date, dc.title, " +
              "(select dc.bibliographicCitation.authors.realName from Article aa) from Article a " +
              "where a.id = :id and dc := a.dublinCore;").
              setParameter("id", id).execute();

          r.beforeFirst();
          if (!r.next())
            return null;

          ArticleInfo ai = new ArticleInfo();
          ai.id    = id;
          ai.date  = r.getLiteralAs(1, Date.class);
          ai.title = r.getString(2);

          Results sr = r.getSubQueryResults(3);
          // XXX: preserve author order
          while (sr.next())
            ai.authors.add(sr.getString(0));

          if (log.isDebugEnabled())
            log.debug("loaded ArticleInfo: id='" + ai.id + "', date='" + ai.date + "', title='" +
                      ai.title + "', authors='" + ai.authors + "'");

          return ai;
        }
      });
  }

  private SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>> loadArticleDates() {
    SortedSet<String> dates =
      TransactionHelper.doInTx(session, new TransactionHelper.Action<SortedSet<String>>() {
        public SortedSet<String> run(Transaction tx) {
          Results r = tx.getSession().createQuery(
              "select a.dublinCore.date from Article a;").execute();

          SortedSet<String> dates = new TreeSet<String>();

          r.beforeFirst();
          while (r.next())
            dates.add(r.getString(0));

          return dates;
        }
      });

    return parseDates(dates);
  }

  private static SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>>
        parseDates(SortedSet<String> dates) {
    SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>> res =
          new TreeMap<Integer, SortedMap<Integer, SortedSet<Integer>>>();

    for (String date : dates) {
      Integer y = getYear(date);
      Integer m = getMonth(date);
      Integer d = getDay(date);
      getMonth(getYear(res, y), m).add(d);
    }

    return res;
  }

  private static SortedMap<Integer, SortedSet<Integer>>
      getYear(SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>> years, Integer year) {
    SortedMap<Integer, SortedSet<Integer>> months = years.get(year);
    if (months == null)
      years.put(year, months = new TreeMap<Integer, SortedSet<Integer>>());
    return months;
  }

  private static SortedSet<Integer> getMonth(SortedMap<Integer, SortedSet<Integer>> months,
                                             Integer mon) {
    SortedSet<Integer> days = months.get(mon);
    if (days == null)
      months.put(mon, days = new TreeSet<Integer>());
    return days;
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

  private static void insertDate(SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>> dates,
                                 Date newDate) {
    Calendar newCal = Calendar.getInstance();
    newCal.setTime(newDate);

    int y = newCal.get(Calendar.YEAR);
    int m = newCal.get(Calendar.MONTH) + 1;
    int d = newCal.get(Calendar.DATE);

    getMonth(getYear(dates, y), m).add(d);
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
            "select a.id, date from Article a where " +
            "date := a.dublinCore.date and gt(date, :sd) and lt(date, :ed) order by date desc;").
            setParameter("sd", sd).setParameter("ed", endDate).execute();

        List<URI> dates = new ArrayList<URI>();

        r.beforeFirst();
        while (r.next())
          dates.add(r.getURI(0));

        return dates;
      }
    });
  }

  private List<ArticleInfo> getArticles(List<URI> ids, int pageNum, int pageSize) {
    List<ArticleInfo> res = new ArrayList<ArticleInfo>();

    int beg = (pageSize > 0) ? pageNum * pageSize : 0;
    int end = (pageSize > 0) ? Math.min((pageNum + 1) * pageSize, ids.size()) : ids.size();
    for (int idx = beg; idx < end; idx++) {
      final URI id = ids.get(idx);

      try {
        pep.checkAccess(ArticlePEP.READ_META_DATA, id);
      } catch (SecurityException se) {
        if (log.isDebugEnabled())
          log.debug("Filtering URI " + id + " from Article list due to PEP SecurityException", se);
        continue;
      }

      ArticleInfo ai =
        CacheAdminHelper.getFromCache(browseCache, ARTICLE_KEY + id, -1, ARTICLE_LOCK + id,
                                      "article " + id,
                                      new CacheAdminHelper.EhcacheUpdater<ArticleInfo>() {
        public ArticleInfo lookup() {
          return loadArticleInfo(id);
        }
      });

      if (ai != null)
        res.add(ai);
    }

    return res;
  }

  private List<NewArtInfo> getNewArticleInfos(final String[] uris) {
    return TransactionHelper.doInTx(session, new TransactionHelper.Action<List<NewArtInfo>>() {
      public List<NewArtInfo> run(Transaction tx) {
        String query =
            "select cat, a.id, a.dublinCore.date date from Article a " +
            "where cat := a.categories.mainCategory and (";
        for (String uri : uris)
          query += "a.id = <" + uri + "> or ";
        query = query.substring(0, query.length() - 4) + ") order by date desc;";

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
   * The info about a single article that the UI needs.
   */
  public static class ArticleInfo implements Serializable {
    public URI          id;
    public Date         date;
    public String       title;
    public List<String> authors = new ArrayList<String>();

    /**
     * Get the id.
     *
     * @return the id.
     */
    public URI getId() {
      return id;
    }

    /**
     * Get the date.
     *
     * @return the date.
     */
    public Date getDate() {
      return date;
    }

    /**
     * Get the title.
     *
     * @return the title.
     */
    public String getTitle() {
      return title;
    }

    /**
     * Get the authors.
     *
     * @return the authors.
     */
    public List<String> getAuthors() {
      return authors;
    }
  }
}
