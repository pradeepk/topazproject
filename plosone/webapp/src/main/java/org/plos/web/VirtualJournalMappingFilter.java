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
import java.lang.management.ManagementFactory;
import java.util.HashMap;

import javax.management.MBeanServer;
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
import net.sf.ehcache.management.ManagementService;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;

/**
 * A Filter that maps incoming URI Requests to an appropriate virtual journal resource.
 *
 * If a virtual journal context is set, a lookup is done to see if an override for the requested
 * resource exists for the virtual journal.  If so, the virtual journal override is wrapped in a
 * Request and passed on to the FilterChain.  If not, the Request is left as is.
 */
public class VirtualJournalMappingFilter implements Filter {

  /**
   * ServletRequest attribute for the virtual journal mapping prefix.
   */
  public static final String PUB_VIRTUALJOURNAL_MAPPINGPREFIX = "pub.virtualjournal.mappingprefix";
  /**
   * Default virtual journal mapping prefix.
   */
  public static final String PUB_VIRTUALJOURNAL_DEFAULT_MAPPINGPREFIX = "";

  private static final Configuration configuration = ConfigurationStore.getInstance().getConfiguration();

  private static ServletContext servletContext = null;

  private static final Log log = LogFactory.getLog(VirtualJournalMappingFilter.class);

  private static Ehcache fileSystemCache  = null;
  static {
    try {
      CacheManager cacheManager = CacheManager.getInstance();
      fileSystemCache = cacheManager.getEhcache("VirtualJournalMappingFilter");
      MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
      ManagementService.registerMBeans(cacheManager, mBeanServer, true, true, true, true);
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

  // Cache Element value to indicate directory doesn't exist
  private static final HashMap DIRECTORY_DOES_NOT_EXIST = new HashMap();

  /*
   * @see javax.servlet.Filter#init
   */
  public void init(final FilterConfig filterConfig) throws ServletException {

    // settings & overrides are in the Configuration
    if (configuration == null) {
      // should never happen
      final String errorMessage = "No Configuration is available to set Virtual Journal mappingPrefix";
      log.error(errorMessage);
      throw new ServletException(errorMessage);
    }

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
    final String[] virtualJournalResource = lookupVirtualJournalResource((HttpServletRequest) request);

    // wrap Request if a virtual journal override should be used
    if (virtualJournalResource != null) {
      if (log.isDebugEnabled()) {
        log.debug("virtual journal override mapped to: "
          + "requestUri=\"" + virtualJournalResource[0] + "\""
          + ", requestServletPath=\"" + virtualJournalResource[1] + "\"");
      }

      filterChain.doFilter(wrapRequest((HttpServletRequest) request, virtualJournalResource[0], virtualJournalResource[1]), response);
    } else {
      // continue the Filter chain unmodified
      if (log.isDebugEnabled()) {
        log.debug("no virtual journal override for: "
          + "requestUri=\"" + ((HttpServletRequest)request).getRequestURI() + "\""
          + ", requestServletPath=\"" + ((HttpServletRequest)request).getServletPath() + "\"");
      }

      filterChain.doFilter(request, response);
    }
  }

  /**
   * Lookup a virtual journal resource.
   *
   * If resource exists within the virtual journal context, return the requestUri and requestServletPath for the resource.
   *
   * @param request <code>HttpServletRequest</code> to apply the lookup against.
   * @return String[] {requestUri, requestServletPath} or <code>null</code> if virtual journal resource doesn't exist.
   */
private String[] lookupVirtualJournalResource(final HttpServletRequest request) {

    // lookup virtual journal context
    final String virtualJournal = (String) request.getAttribute(VirtualJournalContextFilter.PUB_VIRTUALJOURNAL_JOURNAL);
    if (virtualJournal == null) {
      return null;
    }

    if (log.isDebugEnabled()) {
      log.debug("looking up mappingPrefix for virutalJournal: \"" + virtualJournal + "\"");
    }

    // lookup mapping prefix
    String mappingPrefix  = null;
    if (virtualJournal.equals(VirtualJournalContextFilter.PUB_VIRTUALJOURNAL_DEFAULT_JOURNAL)) {
      // use <default><mappingPrefix>
      mappingPrefix  = configuration.getString(VirtualJournalContextFilter.CONF_VIRTUALJOURNALS_DEFAULT + ".mappingPrefix");
    } else {
      // use <journals><${journalName}><mappingPrefix>
      mappingPrefix  = configuration.getString(VirtualJournalContextFilter.CONF_VIRTUALJOURNALS + "." + virtualJournal + ".mappingPrefix");
    }
    // put mappingPrefix in the Request for others to use
    request.setAttribute(PUB_VIRTUALJOURNAL_MAPPINGPREFIX, mappingPrefix);

    if (log.isDebugEnabled()) {
      log.debug("using mappingPrefix: \"" + mappingPrefix + "\"");
    }

    // no modification if mappingPrefix doesn't exist
    if ( mappingPrefix == null || mappingPrefix.length() == 0) {
      return null;
    }

    // what is resource name on filesystem?
    final String reqUri = mappingPrefix + request.getRequestURI();
    final String reqServletPath = request.getServletPath().startsWith("/")
      ? mappingPrefix       + request.getServletPath()
      : mappingPrefix + "/" + request.getServletPath();
    final String realPath = servletContext.getRealPath(reqUri);
    final int lastSlash = realPath.lastIndexOf("/");
    final String realDir = realPath.substring(0, lastSlash);
    final String realFile = realPath.substring(lastSlash + 1);

    if (log.isDebugEnabled()) {
      log.debug("using realDir + \"/\" + realFile: \"" + realDir + "\" + \"/\" + " + realFile + "\"");
    }

    // does resource actually exist?
    // find directory in cache
    Element cachedDirElement = fileSystemCache.get(realDir);
    if (cachedDirElement == null) {
      if (log.isDebugEnabled()) {
        log.debug("cache miss for : \"" + realDir + "\"");
      }

      // try dir on file system
      File dir = new File(realDir);
      if (!dir.exists()) {
        fileSystemCache.put(new Element(realDir, DIRECTORY_DOES_NOT_EXIST));
        return null;
      }

      // put dir contents in a HashMap for later use
      HashMap dirMap = new HashMap();
      for (File dirEntry : dir.listFiles()) {
        dirMap.put(dirEntry.getAbsolutePath(), dirEntry);
      }
      cachedDirElement = new Element(realDir, dirMap);
      fileSystemCache.put(cachedDirElement);
    }

    // cache knows if directory doesn't exist, test against static Object
    if (cachedDirElement.getObjectValue() == DIRECTORY_DOES_NOT_EXIST) {
      return null;
    }

    // look for file in dir, if specified
    if (realFile.length() != 0) {
      if (!((HashMap) cachedDirElement.getObjectValue()).containsKey(realPath)) {
        // file not in dir
        return null;
      }
    }

    // use virtual journal resource
    return new String[] {reqUri, reqServletPath};
  }

  public static HttpServletRequest wrapRequest(HttpServletRequest request, final String requestUri, final String requestServletPath) {

    return new HttpServletRequestWrapper(request) {

      public String getRequestURI() {

        return requestUri;
      }

      public String getServletPath() {

        return requestServletPath;
      }
    };
  }
}
