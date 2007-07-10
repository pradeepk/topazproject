/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.web;

import java.io.File;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Filter that maps incoming URI Requests to an appropriate virtual journal resource.
 *
 * If a virtual journal context is set, a lookup is done to see if an override for the requested
 * resource exists for the virtual journal.  If so, the virtual journal override is wrapped in a
 * Request and passed on to the FilterChain.  If not, the Request is left as is.
 */
public class VirtualJournalMappingFilter implements Filter {

  private static ServletContext servletContext = null;

  private static final Log log = LogFactory.getLog(VirtualJournalMappingFilter.class);

  /*
   * @see javax.servlet.Filter#init
   */
  public void init(final FilterConfig filterConfig) throws ServletException {

    // need ServletContext to get "real" path/file names
    servletContext = filterConfig.getServletContext();
  }

  /*
   * @see javax.servlet.Filter#destroy
   */
  public void destroy() {

    // nothing to do
  }

  /*
   * @see javax.servlet.Filter#doFilter
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {


    // lookup virtual journal context, mapping, & modify URI of Request if necessary
    String[] virtualJournalResource = lookupVirtualJournalResource((HttpServletRequest) request);

    // wrap Request if a virtual journal override should be used
    if (virtualJournalResource != null) {
      if (log.isDebugEnabled()) {
        log.debug("virtual journal override mapped to: "
          + "pathInfo=\"" + virtualJournalResource[0] + "\""
          + ", pathTranslated=\"" + virtualJournalResource[1] + "\"");
      }

      filterChain.doFilter(wrapRequest((HttpServletRequest) request, virtualJournalResource[0], virtualJournalResource[1]), response);
    } else {
      // continue the Filter chain unmodified
      if (log.isDebugEnabled()) {
        log.debug("no virtual journal override for: "
          + "pathInfo=\"" + ((HttpServletRequest)request).getPathInfo() + "\""
          + ", pathTranslated=\"" + ((HttpServletRequest)request).getPathTranslated() + "\"");
      }

      filterChain.doFilter(request, response);
    }
  }

  /**
   * Lookup a virtual journal resource.
   *
   * If resource exists within the virtual journal context, return the pathInfo and pathTranslated for the resource.
   *
   * @param request <code>HttpServletRequest</code> to apply the lookup against.
   * @return String[] {pathInfo, pathTranslated} or <code>null</code> if virtual journal resource doesn't exist.
   */
private String[] lookupVirtualJournalResource(final HttpServletRequest request) {

    final String mappingPrefix  = (String) request.getAttribute(VirtualJournalContextFilter.PUB_VIRTUALJOURNAL_MAPPINGPREFIX);

    // no modification if mappingPrefix doesn't exist or is == default
    if ( mappingPrefix == null
      || mappingPrefix.equals(VirtualJournalContextFilter.PUB_VIRTUALJOURNAL_DEFAULT_MAPPINGPREFIX)) {
      return null;
    }

    // what is resource name on filesystem?
    final String pathInfo       = mappingPrefix + (request.getPathInfo() == null ? "" : request.getPathInfo());
    final String pathTranslated = servletContext.getRealPath(pathInfo);
    if (pathTranslated == null) {
      if (log.isDebugEnabled()) {
        log.debug("ignoring virtual journal override: ServletContext.getRealPath(" + pathInfo + ") == null");
      }

      return null;
    }

    // does resource actually exist?
    File virtualJournalFile = new File(pathTranslated);
    if (!virtualJournalFile.exists()) {
      if (log.isDebugEnabled()) {
        log.debug("ignoring virtual journal override: File(" + pathTranslated + ").exists == false");
      }

      return null;
    }

    // use virtual journal resource
    return new String[] {pathInfo, pathTranslated};
  }

  private HttpServletRequest wrapRequest(HttpServletRequest request, final String pathInfo, final String pathTranslated) {

    return new HttpServletRequestWrapper(request) {

      public String getPathInfo() {

        return pathInfo;
      }

      public String getPathTranslated() {

        return pathTranslated;
      }
    };
  }
}
