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

package org.plos.web;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Filter that maps incoming URI Requests to an appropriate virtual journal resources.
 *
 * If a virtual journal context is set, a lookup is done to see if an override for the requested
 * resource exists for the virtual journal.  If so, the Request is wrapped with the override values
 * and passed on to the FilterChain.  If not, the Request is wrapped with default values for the
 * resource and then passed to the FilterChain.
 */
public class VirtualJournalMappingFilter implements Filter {

  private static ServletContext servletContext = null;

  private static final Log log = LogFactory.getLog(VirtualJournalMappingFilter.class);

  private static Ehcache fileSystemCache  = null;
  static {
    try {
      CacheManager cacheManager = CacheManager.getInstance();
      fileSystemCache = cacheManager.getEhcache("VirtualJournalMappingFilter");
    } catch (CacheException ce) {
      log.error("Error getting cache-manager", ce);
    } catch (IllegalStateException ise) {
      log.error("Error getting cache", ise);
    }

    if (fileSystemCache == null) {
      log.error("No cache configuration found for VirtualJournalMappingFilter");
    } else {
      log.info("Cache configuration found for VirtualJournalMappingFilter");
    }
  }

  // Cache Element value to indicate resource exists
  private static final String RESOURCE_EXISTS = "EXISTS";
  // Cache Element value to indicate resource does not exists
  private static final String RESOURCE_DOES_NOT_EXIST = "DOES NOT EXIST";

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
    final ServletRequest virtualJournalResource = lookupVirtualJournalResource((HttpServletRequest) request);

    if (log.isDebugEnabled()) {
      log.debug("virtual journal resource Request:"
        + "  requestUri=\""  + ((HttpServletRequest) virtualJournalResource).getRequestURI() + "\""
        + ", contextPath=\"" + ((HttpServletRequest) virtualJournalResource).getContextPath() + "\""
        + ", servletPath=\"" + ((HttpServletRequest) virtualJournalResource).getServletPath() + "\""
        + ", pathInfo=\""    + ((HttpServletRequest) virtualJournalResource).getPathInfo() + "\"");
    }

    // continue the Filter chain with virtual journal resource
    filterChain.doFilter(virtualJournalResource, response);
  }

  /**
   * Lookup a virtual journal resource.
   *
   * If resource exists within the virtual journal context, return a WrappedRequest with the
   * mappingPrefix,  else return a WrappedRequest for the default resource.
   *
   * @param request <code>HttpServletRequest</code> to apply the lookup against.
   * @return WrappedRequest for the resource.
   */
private HttpServletRequest lookupVirtualJournalResource(final HttpServletRequest request)
  throws ServletException {

    // lookup virtual journal context
    final VirtualJournalContext virtualJournalContext = (VirtualJournalContext) request.getAttribute(
      VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT);
    if (virtualJournalContext == null) {
      return request;
    }
    final String virtualJournal = virtualJournalContext.getJournal();
    final String mappingPrefix  = virtualJournalContext.getMappingPrefix();
    if (virtualJournal == null || mappingPrefix == null || mappingPrefix.length() == 0) {
      return request;
    }

    if (log.isDebugEnabled()) {
      log.debug("looking up virtual journal resource for virutalJournal: \"" + virtualJournal + "\""
        + ", mappingPrefix: \"" + mappingPrefix + "\"");
    }

    // will need to examine Request Path Elements
    // get virtualized URI values
    final String[] virtualizedValues = virtualJournalContext.virtualizeUri(
      request.getContextPath(), request.getServletPath(), request.getPathInfo());
    final String virtualContextPath = virtualizedValues[0];
    final String virtualServletPath = virtualizedValues[1];
    final String virtualPathInfo    = virtualizedValues[2];
    final String virtualRequestUri  = virtualizedValues[3];
    // get defaulted URI values
    final String[] defaultedValues = virtualJournalContext.defaultUri(
      request.getContextPath(), request.getServletPath(), request.getPathInfo());
    final String defaultContextPath = defaultedValues[0];
    final String defaultServletPath = defaultedValues[1];
    final String defaultPathInfo    = defaultedValues[2];
    final String defaultRequestUri  = defaultedValues[3];

    // look in cache 1st for virtual resource
    Element cachedVirtualResourceElement = fileSystemCache.get(virtualRequestUri);
    if (cachedVirtualResourceElement == null) {
      if (log.isDebugEnabled()) {
        log.debug("cache miss for virtual resource: " + virtualRequestUri);
      }

      // can the ServletContext find the virtual resource?
      final URL virtualResourceURL;
      try {
        // path must begin with a "/" and is interpreted as relative to the current context root
        virtualResourceURL = servletContext.getResource(virtualRequestUri.substring(virtualContextPath.length()));
      } catch (MalformedURLException mre) {
        // should never happen
        log.error(mre);
        throw new ServletException("virtualRequestUri=" + virtualRequestUri, mre);
      }
      if (virtualResourceURL != null) {
        cachedVirtualResourceElement = new Element(virtualRequestUri, RESOURCE_EXISTS);
      } else {
        cachedVirtualResourceElement = new Element(virtualRequestUri, RESOURCE_DOES_NOT_EXIST);
      }
      // populate cache with existence
      fileSystemCache.put(cachedVirtualResourceElement);

      if (log.isDebugEnabled()) {
        log.debug("ServletContext.getResource(" + virtualRequestUri + "): "
          + cachedVirtualResourceElement.getObjectValue());
      }
    } else {
      if (log.isDebugEnabled()) {
        log.debug("cache hit for virtual resource: " + virtualRequestUri
          + ", value: " + cachedVirtualResourceElement.getObjectValue());
      }
    }

    // does resource override exist?
    if (cachedVirtualResourceElement.getObjectValue().equals(RESOURCE_EXISTS)) {
      // use virtual journal resource
      return wrapRequest(request,
        virtualContextPath, virtualServletPath, virtualPathInfo, virtualRequestUri);
    } else {
      // use default resource
      return wrapRequest(request,
        defaultContextPath, defaultServletPath, defaultPathInfo, defaultRequestUri);
    }
  }


  /**
   * Wrap an HttpServletRequest with arbitrary URI values.
   */
  public static HttpServletRequest wrapRequest(final HttpServletRequest request,
    final String contextPath, final String servletPath, final String pathInfo,
    final String requestUri) {

    return new HttpServletRequestWrapper(request) {

      public String getRequestURI() {
        return requestUri;
      }

      public String getContextPath() {
        return contextPath;
      }

      public String getServletPath() {
        return servletPath;
      }

      public String getPathInfo() {
        return pathInfo;
      }
    };
  }
}
