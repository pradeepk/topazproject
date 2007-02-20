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

import static org.topazproject.ws.article.Article.ST_ACTIVE;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.article.action.BrowseArticlesAction;
import org.topazproject.ws.article.ArticleInfo;

import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

/**
 * @author stevec
 *
 */
public class BrowseService {
  private static final Log log = LogFactory.getLog(BrowseArticlesAction.class);

  
  private GeneralCacheAdministrator articleCacheAdministrator;
  private ArticleWebService articleWebService;
  private ArticleInfo[] allArticles;
  private String[] categoryNames;
  private ArrayList<ArrayList<ArticleInfo>> articlesByCategory;
  private TreeMap<String, ArrayList<ArticleInfo>> articlesByCategoryMap;
  private TreeMap<Date, ArrayList<ArticleInfo>> articlesByDateMap;  
  private ArrayList<ArrayList<ArrayList<ArrayList<ArticleInfo>>>> articlesByDate;
  private ArrayList<ArrayList<ArrayList<Date>>> articleDates;
  
  private static final String CAT_NAME_CACHE_KEY = "CAT_NAME_CACHE_KEY";
  private static final String CAT_ARTICLES_CACHE_KEY = "CAT_ARTICLES_CACHE_KEY";
  private static final String DATES_CACHE_KEY = "DATES_CACHE_KEY";
  private static final String DATES_ARTICLES_CACHE_KEY = "DATES_ARTICLES_CACHE_KEY";
  public static final String ALL_ARTICLE_CACHE_GROUP_KEY = "ALL_ARTICLE_LIST_GROUP";
  private static final String ALL_ARTICLE_CACHE_KEY = "ALL_ARTICLE_LIST";
  
  public ArticleInfo[] getAllArticles() {
    if (allArticles == null) {
      try {
        // Get from the cache
  
        allArticles= (ArticleInfo[]) 
          articleCacheAdministrator.getFromCache(ALL_ARTICLE_CACHE_KEY, CacheEntry.INDEFINITE_EXPIRY);
        if (log.isDebugEnabled()) {
          log.debug("retrieved all articles from cache");
        }
      } catch (NeedsRefreshException nre) {
        boolean updated = false;
        if (log.isDebugEnabled()){
          log.debug("retrieving all articles from TOPAZ");
        }
        try {
          //  Get the value from TOPAZ
          allArticles = articleWebService.getArticleInfos(null, null, null, null, 
                                                          new int[]{ST_ACTIVE}, false);
          
          // Store in the cache
          articleCacheAdministrator.putInCache(ALL_ARTICLE_CACHE_KEY, allArticles, 
                                               new String[]{ALL_ARTICLE_CACHE_GROUP_KEY});
          //articleCacheAdministrator.putInCache(ALL_ARTICLE_CACHE_KEY, allArticles);
          updated = true;
        } catch (RemoteException re) {
          log.error("Could not retrieve the all articles", re);
          allArticles = new ArticleInfo[0];
        } finally {
          if (!updated) {
              // It is essential that cancelUpdate is called if the
              // cached content could not be rebuilt
              articleCacheAdministrator.cancelUpdate(ALL_ARTICLE_CACHE_KEY);
          }
        }
      }
    }    
    return allArticles;
  }
  
  
  /**
   * Takes the articles and sets the categoryNames and articlesByCategory values.  
   * 
   *
   */
  private void populateArticlesAndCategories() {
    try {
      articleDates = (ArrayList<ArrayList<ArrayList<Date>>>) 
        articleCacheAdministrator.getFromCache(DATES_CACHE_KEY, CacheEntry.INDEFINITE_EXPIRY);
      articlesByDate = (ArrayList<ArrayList<ArrayList<ArrayList<ArticleInfo>>>>)
        articleCacheAdministrator.getFromCache(DATES_ARTICLES_CACHE_KEY, CacheEntry.INDEFINITE_EXPIRY);;
      categoryNames = (String[])articleCacheAdministrator.getFromCache(CAT_NAME_CACHE_KEY, CacheEntry.INDEFINITE_EXPIRY);;
      articlesByCategory = (ArrayList<ArrayList<ArticleInfo>>)articleCacheAdministrator.getFromCache(CAT_ARTICLES_CACHE_KEY, CacheEntry.INDEFINITE_EXPIRY);;;
    
    } catch (NeedsRefreshException nre) {
      boolean updated = false;
      if (log.isDebugEnabled()){
        log.debug("constructing category and date browse objects");
      }      
      try {
        createBrowseObjects();
        String[] groupKeys = new String[] {ALL_ARTICLE_CACHE_GROUP_KEY};
        articleCacheAdministrator.putInCache(DATES_CACHE_KEY, articleDates, groupKeys);
        articleCacheAdministrator.putInCache(DATES_ARTICLES_CACHE_KEY, articlesByDate, groupKeys); 
        articleCacheAdministrator.putInCache(CAT_NAME_CACHE_KEY, categoryNames, groupKeys); 
        articleCacheAdministrator.putInCache(CAT_ARTICLES_CACHE_KEY, articlesByCategory, groupKeys); 
      } finally {
        if (!updated) {
          // It is essential that cancelUpdate is called if the
          // cached content could not be rebuilt
          articleCacheAdministrator.cancelUpdate(DATES_CACHE_KEY);
          articleCacheAdministrator.cancelUpdate(DATES_ARTICLES_CACHE_KEY);
          articleCacheAdministrator.cancelUpdate(CAT_NAME_CACHE_KEY);
          articleCacheAdministrator.cancelUpdate(CAT_ARTICLES_CACHE_KEY);
        }
      }
    }
    
  }
  
  private void createBrowseObjects() {
    ArticleInfo[] allArticleList = getAllArticles();
    
    if (allArticleList.length > 0){
      articlesByCategoryMap = new TreeMap<String, ArrayList<ArticleInfo>>();
      articlesByDateMap = new TreeMap<Date, ArrayList<ArticleInfo>>();
      String[] categories;
      ArrayList<ArticleInfo> theList;
      Date theDate;
      for (ArticleInfo art : allArticleList) {
        categories = art.getCategories();
        theDate = art.getArticleDate();
        theList = articlesByDateMap.get(theDate);
        if (theList == null) {
          theList = new ArrayList<ArticleInfo>();
          articlesByDateMap.put(theDate, theList);
        }
        theList.add(art);
        for (String cat : categories) {
          theList = articlesByCategoryMap.get(cat);
          if (theList == null) {
            theList = new ArrayList<ArticleInfo>();
            articlesByCategoryMap.put(cat, theList);
          }
          theList.add(art);
        }
      }
      Set<Map.Entry<String, ArrayList<ArticleInfo>>> allEntries = articlesByCategoryMap.entrySet();  
      Iterator<Map.Entry<String, ArrayList<ArticleInfo>>> iter = allEntries.iterator();
      Map.Entry<String, ArrayList<ArticleInfo>> entry;
      categoryNames = new String[allEntries.size()];
      articlesByCategory = new ArrayList<ArrayList<ArticleInfo>>(allEntries.size());
      ArrayList<ArticleInfo> artInfoArrayList;
      for (int i = 0; iter.hasNext(); i++) {
        entry = iter.next();
        categoryNames[i] = entry.getKey();
        artInfoArrayList = entry.getValue();
        articlesByCategory.add(i, artInfoArrayList);
        //artInfoArrayList.toArray(articlesByCategory[i]);
      }
      
      Set<Map.Entry<Date, ArrayList<ArticleInfo>>> allDateEntries = articlesByDateMap.entrySet();  
      Iterator<Map.Entry<Date, ArrayList<ArticleInfo>>> dateIter = allDateEntries.iterator();
      Map.Entry<Date, ArrayList<ArticleInfo>> dateEntry;
      articleDates = new ArrayList<ArrayList<ArrayList<Date>>>(2);
      articlesByDate = new ArrayList<ArrayList<ArrayList<ArrayList<ArticleInfo>>>>(2);
      //ArrayList<ArticleInfo> artInfoArrayList;
      int j = -1;
      int currentMonth = -1;
      int currentYear = -1;
      int k = -1;
      Calendar oneDate;
      for (int i = 0; dateIter.hasNext(); i++) {
        dateEntry = dateIter.next();
        oneDate = Calendar.getInstance();
        oneDate.setTime(dateEntry.getKey());
        if (currentYear != oneDate.get(Calendar.YEAR)) {
          articleDates.add(++k, new ArrayList<ArrayList<Date>>(12));
          articlesByDate.add(k, new ArrayList<ArrayList<ArrayList<ArticleInfo>>>(12));
          currentYear = oneDate.get(Calendar.YEAR);
          j = -1;
        }
        
        if (currentMonth != oneDate.get(Calendar.MONTH)) {
          //flaw here is if you have two consecutive entries with the same month but different year
          articleDates.get(k).add(++j,new ArrayList<Date>());
          articlesByDate.get(k).add(j,new ArrayList<ArrayList<ArticleInfo>>());
          currentMonth = oneDate.get(Calendar.MONTH);
        }
        articleDates.get(k).get(j).add(dateEntry.getKey());
        articlesByDate.get(k).get(j).add(dateEntry.getValue());
      }
    } else {
      articleDates = new ArrayList<ArrayList<ArrayList<Date>>>(0);
      articlesByDate = new ArrayList<ArrayList<ArrayList<ArrayList<ArticleInfo>>>>(0);
      categoryNames = new String[0];
      articlesByCategory = new ArrayList<ArrayList<ArticleInfo>>(0);
    }
  }


  /**
   * @return Returns the articleDates.
   */
  public ArrayList<ArrayList<ArrayList<Date>>> getArticleDates() {
    if (articleDates == null) {
      populateArticlesAndCategories();
    }
    return articleDates;
  }


  /**
   * @return Returns the articlesByCategory.
   */
  public ArrayList<ArrayList<ArticleInfo>> getArticlesByCategory() {
    if (articlesByCategory == null){
      populateArticlesAndCategories();      
    }
    return articlesByCategory;
  }


  /**
   * @return Returns the articlesByDate.
   */
  public ArrayList<ArrayList<ArrayList<ArrayList<ArticleInfo>>>> getArticlesByDate() {
    if (articlesByDate== null){
      populateArticlesAndCategories();      
    }
    
    return articlesByDate;
  }


  /**
   * @return Returns the categoryNames.
   */
  public String[] getCategoryNames() {
    if (categoryNames == null){
      populateArticlesAndCategories();      
    }
    return categoryNames;
  }


  /**
   * @param articleCacheAdministrator The articleCacheAdministrator to set.
   */
  public void setArticleCacheAdministrator(GeneralCacheAdministrator articleCacheAdministrator) {
    this.articleCacheAdministrator = articleCacheAdministrator;
  }


  /**
   * @param articleWebService The articleWebService to set.
   */
  public void setArticleWebService(ArticleWebService articleWebService) {
    this.articleWebService = articleWebService;
  }

}
