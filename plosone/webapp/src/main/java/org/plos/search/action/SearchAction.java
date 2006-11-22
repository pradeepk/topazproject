/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search.action;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.action.BaseActionSupport;
import org.plos.search.service.SearchHit;
import org.plos.search.service.SearchService;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Search Action class to search for simple or advanced search.
 */
public class SearchAction extends BaseActionSupport {
  private String query;
  private int startPage = 1;
  private int pageSize = 10;
  private SearchService searchService;

  private static final Log log = LogFactory.getLog(SearchAction.class);
  private Collection<SearchHit> searchResults;
  private String title;
  private String text;

  /**
   * @return return simple search result
   */
  public String executeSimpleSearch() {
    return executeSearch(query);
  }

  /**
   * @return return simple search result
   */
  public String executeAdvancedSearch() {
    query = getAdvancedQuery();
    return executeSearch(query);
  }

  private String executeSearch(final String queryString) {
    try {
      if (StringUtils.isBlank(queryString)) {
        addActionError("Please enter a query string.");
        return ERROR;
      }
      searchResults = searchService.find(queryString, startPage, pageSize);
    } catch (ApplicationException e) {
      addActionError("Search failed");
      log.error("Search failed with error", e);
      return ERROR;
    }
    return SUCCESS;
  }

  private String getAdvancedQuery() {
    final Collection<String> fields = new ArrayList<String>();

    if (StringUtils.isNotBlank(title)) fields.add("title:" + title);
    if (StringUtils.isNotBlank(text)) fields.add("text:" + text);

    return StringUtils.join(fields.iterator(), " AND ");
  }

  /**
   * Set the simple query
   * @param query query
   */
  public void setQuery(final String query) {
    this.query = query;
  }

  /**
   * Set the startPage
   * @param startPage startPage
   */
  public void setStartPage(final int startPage) {
    this.startPage = startPage;
  }

  /**
   * Set the pageSize
   * @param pageSize pageSize
   */
  public void setPageSize(final int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * Set the searchService
   * @param searchService searchService
   */
  public void setSearchService(final SearchService searchService) {
    this.searchService = searchService;
  }

  /**
   * @return the search results.
   */
  public Collection<SearchHit> getSearchResults() {
    return searchResults;
  }


  /**
   * Getter for property 'pageSize'.
   * @return Value for property 'pageSize'.
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Getter for property 'query'.
   * @return Value for property 'query'.
   */
  public String getQuery() {
    return query;
  }

  /**
   * Getter for property 'startPage'.
   * @return Value for property 'startPage'.
   */
  public int getStartPage() {
    return startPage;
  }

  /**
   * Getter for property 'text'.
   * @return Value for property 'text'.
   */
  public String getText() {
    return text;
  }

  /**
   * Setter for property 'text'.
   * @param text Value to set for property 'text'.
   */
  public void setText(final String text) {
    this.text = text;
  }

  /**
   * Getter for property 'title'.
   * @return Value for property 'title'.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Setter for property 'title'.
   * @param title Value to set for property 'title'.
   */
  public void setTitle(final String title) {
    this.title = title;
  }
}
