/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;
import java.rmi.RemoteException;

import org.plos.article.service.ArticleWebService;

import org.topazproject.ws.article.ArticleInfo;

/**
 * @author stevec
 *
 */
public class HomePageAction extends BaseActionSupport {
  
  private static final Log log = LogFactory.getLog(HomePageAction.class);
  private ArticleWebService articleWebService;
  private ArticleInfo[] lastWeeksArticles;
  private static final long ONE_WEEK = 7 * 24 * 60 * 60 * 1000;
  private String[] categoryNames;
  private ArticleInfo[][] articlesByCategory;
  private boolean categoriesAreInitialized = false;
  
  /**
   * This execute method always returns SUCCESS
   * 
   */
  public String execute() throws Exception {
    return SUCCESS;
  }
  
  /**
   * Retrieves the most recently published articles in the last 7 days
   * 
   * @return array of ArticleInfo objects
   */
  public ArticleInfo[] getRecentArticles() {
    return getLastWeeksArticles();
  }
  
  private ArticleInfo[] getLastWeeksArticles() {
    if (lastWeeksArticles == null) {
      try {
        Date weekAgo = new Date();
        weekAgo.setTime(weekAgo.getTime() - ONE_WEEK);
        //TODO: need to change the state to 0 once admin app is in place
        lastWeeksArticles = articleWebService.getArticleInfos(weekAgo.toString(), null, null, null, new int[]{1/*0*/}, false);
      } catch (RemoteException re) {
        log.error("Could not retrive the most recent articles", re);
        lastWeeksArticles = new ArticleInfo[0];
      }
    }    
    return lastWeeksArticles;
  }
  
  /**
   * Takes the articles for the last week and sets the categoryNames and
   * articlesByCategory values.  
   * 
   *
   */
  private void getArticlesAndCategories() {
    ArticleInfo[] weeksArticles = getLastWeeksArticles();
    if (weeksArticles.length > 0){
      TreeMap<String, ArrayList<ArticleInfo>> allArticles = new TreeMap<String, ArrayList<ArticleInfo>>();
      String[] categories;
      ArrayList<ArticleInfo> theList;
      for (ArticleInfo art : weeksArticles) {
        categories = art.getCategories();
        for (String cat : categories) {
          theList = allArticles.get(cat);
          if (theList == null) {
            theList = new ArrayList<ArticleInfo>();
            allArticles.put(cat, theList);
          }
          theList.add(art);
        }
      }
      Set<Map.Entry<String, ArrayList<ArticleInfo>>> allEntries = allArticles.entrySet();  
      Iterator<Map.Entry<String, ArrayList<ArticleInfo>>> iter = allEntries.iterator();
      Map.Entry<String, ArrayList<ArticleInfo>> entry;
      categoryNames = new String[allEntries.size()];
      articlesByCategory = new ArticleInfo[allEntries.size()][];
      ArrayList<ArticleInfo> artInfoArrayList;
      for (int i = 0; iter.hasNext(); i++) {
        entry = iter.next();
        categoryNames[i] = entry.getKey();
        artInfoArrayList = entry.getValue();
        articlesByCategory[i] = new ArticleInfo[artInfoArrayList.size()];
        artInfoArrayList.toArray(articlesByCategory[i]);
      }
    }
  }
  

  /**
   * @return Returns the articleWebService.
   */
  protected ArticleWebService getArticleWebService() {
    return articleWebService;
  }

  /**
   * @param articleWebService The articleWebService to set.
   */
  public void setArticleWebService(ArticleWebService articleWebService) {
    this.articleWebService = articleWebService;
  }

  /**
   * @return Returns the articlesByCategory - a two dimensional array of ArticleInfo
   *          objects to go along with getCategoryNames.
   */
  public ArticleInfo[][] getArticlesByCategory() {
    if (!categoriesAreInitialized) {
      getArticlesAndCategories();
      categoriesAreInitialized = true;
    }
    return articlesByCategory;
  }

  /**
   * @return Returns the categoryNames for articles in the last week in a sorted array.
   */
  public String[] getCategoryNames() {
    if (!categoriesAreInitialized) {
      getArticlesAndCategories();
      categoriesAreInitialized = true;
    }
    return categoryNames;
  }
}
