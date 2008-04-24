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

import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

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
  private Ehcache      cache;

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

      Results results;
      synchronized(cache) {
        Element e = cache.get(cacheKey);
        if (e == null) {
          cache.put(e = new Element(cacheKey, new Results(query, searchWebService, fetchArticleService)));
          if (log.isDebugEnabled())
            log.debug("Created search cache for '" + cacheKey + "'");
        }
        results = (Results)e.getObjectValue();
      }

      // Results are shared, but not concurrently.
      if (!results.getLock().tryLock(10L, TimeUnit.SECONDS)) { // XXX: tune
        log.warn("Failed to acquire lock for cache entry with '" + cacheKey 
            + "'. Creating a temporary uncached instance.");
        results = new Results(query, searchWebService, fetchArticleService);
        results.getLock().lock();
      }

      try {
        return results.getPage(startPage, pageSize, txManager);
      } finally {
        results.getLock().unlock();
      }
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

  @Required
  public void setSearchCache(Ehcache cache) {
    this.cache = cache;
  }
}
