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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.action.BaseActionSupport;
import org.plos.search.service.SearchResult;
import org.plos.search.service.SearchService;

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
  private Collection<SearchResult> searchResults;

  /**
   * @return return simple search result
   */
  public String executeSimpleSearch() {
    try {
      searchResults = searchService.find(query, startPage, pageSize);
    } catch (ApplicationException e) {
      addFieldError("simpleQuery", "Search failed");
      log.error("Simple search failed with error", e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Set the simple query
   * @param query query
   */
  public void setSimpleQuery(final String query) {
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
  public Collection<SearchResult> getSearchResults() {
    return searchResults;
  }
}
