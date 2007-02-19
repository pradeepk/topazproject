/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.article.action;

import static org.topazproject.ws.article.Article.ST_ACTIVE;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.ArticleWebService;
import org.topazproject.ws.article.ArticleInfo;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.NeedsRefreshException;


/**
 * @author stevec
 *
 */
public class BrowseArticlesAction extends BaseActionSupport  {

  private static final Log log = LogFactory.getLog(BrowseArticlesAction.class);
  private String field;
  private int catId;
  private int startPage;
  private int pageSize;
  private int year = -1;
  private int month = -1;
  private int day = -1;
  private GeneralCacheAdministrator articleCacheAdministrator;
  private ArticleWebService articleWebService;
  private ArticleInfo[] allArticles;
  private String[] categoryNames;
  private ArrayList<ArrayList<ArticleInfo>> articlesByCategory;
  private TreeMap<String, ArrayList<ArticleInfo>> articlesByCategoryMap;
  private TreeMap<Date, ArrayList<ArticleInfo>> articlesByDateMap;  
  private ArrayList<ArrayList<ArrayList<ArrayList<ArticleInfo>>>> articlesByDate;
  private ArrayList<ArrayList<ArrayList<Date>>> articleDates;
  private ArrayList<ArticleInfo> articleList;
  
  private static final int PAGE_SIZE = 10;
  public static final String ALL_ARTICLE_CACHE_KEY = "ALL_ARTICLE_LIST";
  private static final String DATE_FIELD = "date"; 

  
  public String execute() throws Exception {
    if (DATE_FIELD.equals(getField())) { 
      return browseDate();
    } else {
      return browseCategory();
    }
  }
  
  private String browseCategory () {
    populateArticlesAndCategories();
    articleList = articlesByCategory.get(catId);
    return SUCCESS;
  }
  

  private String browseDate() {
    populateArticlesAndCategories();
    if (getYear() > -1 && getMonth() > -1 && getDay () > -1) {
      articleList = articlesByDate.get(getYear()).get(getMonth()).get(getDay());
    } else if (getYear() > -1 && getMonth() > -1) {
      articleList = new ArrayList<ArticleInfo>();
      Iterator <ArrayList<ArticleInfo>> iter = articlesByDate.get(getYear()).get(getMonth()).iterator();
      while (iter.hasNext()) {
        articleList.addAll(iter.next());
      }
    } else if (getMonth() == -2) {
      int sizeA = articlesByDate.size();
      int sizeB = articlesByDate.get(sizeA - 1).size();
      articleList = new ArrayList<ArticleInfo>();
      Iterator <ArrayList<ArticleInfo>> iter = articlesByDate.get(sizeA - 1).get(sizeB - 1).iterator();
      while (iter.hasNext()) {
        articleList.addAll(iter.next());
      }
    } else if (getMonth() == -3) {
      int sizeA = articlesByDate.size();
      int sizeB = articlesByDate.get(sizeA - 1).size();
      articleList = new ArrayList<ArticleInfo>();
      int i = 3;
      int indexA = sizeA - 1;
      int indexB = sizeB - 1;
      while (i > 0) {
        if (indexB >= 0) {
          //do nothing
        } else if (indexA > 0) {
          indexA --;
          indexB = articlesByDate.get(indexA).size() - 1;
        } else {
          break;
        }
        Iterator <ArrayList<ArticleInfo>> iter = articlesByDate.get(indexA).get(indexB).iterator();
        while (iter.hasNext()) {
          articleList.addAll(iter.next());
        }
        indexB--;
        i--;
      }
    } else {
      int sizeA = articlesByDate.size();
      int sizeB = articlesByDate.get(sizeA - 1).size();
      int sizeC = articlesByDate.get(sizeA - 1).get(sizeB - 1).size();
      articleList = articlesByDate.get(sizeA - 1).get(sizeB - 1).get(sizeC - 1);
    }
    return SUCCESS;
  }

  private ArticleInfo[] getAllArticles() {
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
          articleCacheAdministrator.putInCache(ALL_ARTICLE_CACHE_KEY, allArticles);
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
   * return a set of the articleDates broken down by year, month, and day.
   * Years are most recent first, then months and day are in chronological
   * order
   * 
   * @return Collection of dates
   */
  public Collection<ArrayList<ArrayList<Date>>> getArticleDates() {
    return articleDates;
  }
  
  /**
   * Takes the articles and sets the categoryNames and articlesByCategory values.  
   * 
   *
   */
  private void populateArticlesAndCategories() {
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
        //        artInfoArrayList = dateEntry.getValue();
        //articlesByDate[i] = new ArticleInfo[artInfoArrayList.size()];
        //artInfoArrayList.toArray(articlesByDate[i]);
      }
    } else {
      articleDates = new ArrayList<ArrayList<ArrayList<Date>>>(0);
      articlesByDate = new ArrayList<ArrayList<ArrayList<ArrayList<ArticleInfo>>>>(0);
      categoryNames = new String[0];
      articlesByCategory = new ArrayList<ArrayList<ArticleInfo>>(0);
    }
  }

  /**
   * @return Returns the field.
   */
  public String getField() {
    if (field == null) {
      field = "";
    }
    return field;
  }

  /**
   * @param field The field to set.
   */
  public void setField(String field) {
    this.field = field;
  }

  /**
   * @return Returns the start.
   */
  public int getStartPage() {
    return startPage;
  }

  /**
   * @param startPage The start to set.
   */
  public void setStartPage(int startPage) {
    this.startPage = startPage;
  }

  /**
   * @return Returns the catId.
   */
  public int getCatId() {
    return catId;
  }

  /**
   * @param catId The catId to set.
   */
  public void setCatId(int catId) {
    this.catId = catId;
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

  /**
   * @return Returns the categoryNames.
   */
  public String[] getCategoryNames() {
    return categoryNames;
  }

  /**
   * @return Returns the articlesByCategory.
   */
  public ArrayList<ArrayList<ArticleInfo>> getArticlesByCategory() {
    return articlesByCategory;
  }

  /**
   * @return Returns the day.
   */
  public int getDay() {
    return day;
  }

  /**
   * @param day The day to set.
   */
  public void setDay(int day) {
    this.day = day;
  }

  /**
   * @return Returns the month.
   */
  public int getMonth() {
    return month;
  }

  /**
   * @param month The month to set.
   */
  public void setMonth(int month) {
    this.month = month;
  }

  /**
   * @return Returns the year.
   */
  public int getYear() {
    return year;
  }

  /**
   * @param year The year to set.
   */
  public void setYear(int year) {
    this.year = year;
  }

  /**
   * @return Returns the articleList.
   */
  public Collection<ArticleInfo> getArticleList() {
    return articleList;
  }

  /**
   * @return Returns the pageSize.
   */
  public int getPageSize() {
    if (pageSize == 0) {
      pageSize = PAGE_SIZE;
    }
    return pageSize;
  }

  /**
   * @param pageSize The pageSize to set.
   */
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }
  
  
}
