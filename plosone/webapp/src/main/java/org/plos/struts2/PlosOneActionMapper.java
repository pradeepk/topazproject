/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.struts2;

import com.opensymphony.xwork2.config.ConfigurationManager;

import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.mapper.DefaultActionMapper;

import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.web.VirtualJournalContext;
import org.plos.web.VirtualJournalMappingFilter;

/**
 * Custom WebWork ActionMapper.
 *
 * Map friendly URIs, e.g. "/article/feed" to WebWork actions w/o WebWorks URIs, "/article/articleFeed.action?parms"
 *
 * @author Jeff Suttor
 *
 */
public class PlosOneActionMapper extends DefaultActionMapper {

  private static final Log log = LogFactory.getLog(PlosOneActionMapper.class);

  /**
   * @see DefaultActionManager#getMapping(HttpServletRequest request).
   */
  public ActionMapping getMapping(HttpServletRequest request, ConfigurationManager configManager) {

    final String origUri = request.getRequestURI();
    ActionMapping actionMapping = null;

    // do not care about "null"
    if (origUri == null) {
      return super.getMapping(request, configManager);
    }

    // is a mapping prefix defined for a virtual journal context?
    final String mappingPrefix = ((VirtualJournalContext) request.getAttribute(
      VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT)).getMappingPrefix();
    if (mappingPrefix == null
      || mappingPrefix.length() == 0) {
      // no override in effect, use default
      actionMapping = super.getMapping(request, configManager);
      if (log.isDebugEnabled()) {
        log.debug("no mappingPrefix, using default action");
      }
    } else {
      // look for an override Action in the mappingPrefix namespace
      final String reqUri = mappingPrefix + origUri;
      final String reqServletPath = request.getServletPath().startsWith("/")
        ? mappingPrefix       + request.getServletPath()
        : mappingPrefix + "/" + request.getServletPath();
      actionMapping = super.getMapping(VirtualJournalMappingFilter.wrapRequest(request, reqUri, reqServletPath), configManager);
      if (actionMapping == null) {
        // use default
        actionMapping = super.getMapping(request, configManager);
        if (log.isDebugEnabled()) {
          log.debug("no override action for mappingPrefix: " + mappingPrefix);
        }
      } else {
        if (log.isDebugEnabled()) {
          log.debug("override action for mappingPrefix: " + mappingPrefix + ", action: " + actionMapping.getName());
        }
      }
    }

    // ATOM feed hook: only care about "/article/feed"
    // will factor out with comprehensive REST URI mapping
    if (origUri.startsWith("/article/feed")) {
      return mapUriToAction();
    }

    return actionMapping;
  }

  /**
   * @see DefaultActionManager#getUriFromActionMapping(ActionMapping mapping).
   */
  public String getUriFromActionMapping(ActionMapping mapping) {

    // only care about /article/feed
    if ("getFeed".equals(mapping.getName())
      && "/article/feed".equals(mapping.getNamespace())
      && "execute".equals(mapping.getMethod())) {
      return("/article/feed");
    }

    // use default
    return super.getUriFromActionMapping(mapping);
 }

  /**
   * Map URIs that start with /article/feed to the getFeed action.
   *
   * @return ActionMapping for getFeed.
   */
 private ActionMapping mapUriToAction() {

   // placeholder for real REST URIs

   // TODO: possible to use common config?
   HashMap<String, String> parms = new HashMap();
   parms.put("feedName", "wireFeed");  // parms passed as null, for now

   return new ActionMapping(
     "getFeed",                              // name
     "/article/feed",                        // namespace
     "execute",                              // method
     null);                                  // parms
 }
}
