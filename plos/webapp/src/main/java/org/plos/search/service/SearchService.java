/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.plos.search.service;

import org.plos.ApplicationException;
import org.plos.search.SearchResultPage;
import org.plos.user.PlosOneUser;
import org.topazproject.otm.Session;
import org.topazproject.otm.spring.OtmTransactionManager;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;
import org.plos.article.service.FetchArticleService;
import org.plos.configuration.ConfigurationStore;
import org.plos.search.SearchResultPage;
import org.plos.user.PlosOneUser;
import org.springframework.beans.factory.annotation.Required;

/**
 * Service to provide search capabilities for the application
 *
 * @author Viru
 * @author Eric Brown
 */
public class SearchService {
  private static final Log           log   = LogFactory.getLog(SearchService.class);
  private static final Configuration CONF  = ConfigurationStore.getInstance().getConfiguration();
  private static TemporaryCache      cache = new TemporaryCache(
                                              CONF.getLong("pub.search.cacheDuration", 600000L));

  private SearchWebService           searchWebService;
  private FetchArticleService        fetchArticleService;
  private OtmTransactionManager      txManager;

  /**
   * Find the results for a given query.
   *
   * @param query     The query string the user suplied
   * @param startPage The page number of the search results the user wants
   * @param pageSize  The number of results per page
   * @return A SearchResultPage representing the search results page to be rendered
   * @throws ApplicationException that wraps some underlying exception
   */
  public SearchResultPage find(final String query, final int startPage, final int pageSize)
      throws ApplicationException {
    try {
      PlosOneUser   user     = PlosOneUser.getCurrentUser();
      final String  cacheKey = (user == null ? "anon" : user.getUserId()) + "|" + query;

      Results results  = (Results) cache.get(cacheKey);
      if (results == null) {
        results = new Results(query, searchWebService, fetchArticleService);
        cache.put(cacheKey, results);
        if (log.isDebugEnabled())
          log.debug("Created search cache for '" + cacheKey + "' of " +
                    results.getTotalHits(txManager));
      }

      return results.getPage(startPage, pageSize, txManager);
    } catch (Exception e) {
      throw new ApplicationException("Search failed with exception:", e);
    }
  }

  /**
   * Setter for property 'searchWebService'.
   * @param searchWebService Value to set for property 'searchWebService'.
   */
  public void setSearchWebService(final SearchWebService searchWebService) {
    this.searchWebService = searchWebService;
  }

  /**
   * Set FetchArticleService.  Enable Spring autowiring.
   *
   * @param fetchArticleService to use.
   */
  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  @Required
  public void setTxManager(OtmTransactionManager txManager) {
    this.txManager = txManager;
  }
}
