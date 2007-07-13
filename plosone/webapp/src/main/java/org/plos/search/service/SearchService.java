/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search.service;

import org.plos.ApplicationException;
import org.plos.search.SearchResultPage;
import org.plos.user.PlosOneUser;
import org.plos.web.UserContext;
import static org.plos.Constants.PLOS_ONE_USER_KEY;
import org.topazproject.otm.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;
import org.apache.commons.configuration.Configuration;

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
  private UserContext                userContext;
  private Session                    otmSession;

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
      PlosOneUser user     = (PlosOneUser) userContext.getSessionMap().get(PLOS_ONE_USER_KEY);
      String      cacheKey = (user == null ? "anon" : user.getUserId()) + "|" + query;
      Results     results  = (Results) cache.get(cacheKey);

      if (results == null) {
        results = new Results(query, searchWebService);
        cache.put(cacheKey, results);
        if (log.isDebugEnabled())
          log.debug("Created search cache for '" + cacheKey + "' of " +
                    results.getTotalHits(otmSession));
      }

      return results.getPage(startPage, pageSize, otmSession);
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
   * Set the user's context which can be used to obtain user's session values/attributes
   * @param userContext userContext
   */
  public void setUserContext(final UserContext userContext) {
    this.userContext = userContext;
  }

  public void setOtmSession(Session otmSession) {
    this.otmSession = otmSession;
  }
}
