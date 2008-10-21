/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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

package org.topazproject.ambra.struts2;

import com.opensymphony.xwork2.config.ConfigurationManager;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.mapper.DefaultActionMapper;

import org.topazproject.ambra.configuration.ConfigurationStore;

/**
 * Custom WebWork ActionMapper.
 *
 * Map friendly URIs, e.g. "/article/feed" to WebWork actions w/o WebWorks URIs, "/article/articleFeed.action?parms"
 *
 * @author Jeff Suttor
 *
 */
public class AmbraActionMapper extends DefaultActionMapper {

  /** Pub Configuration */
  private static final org.apache.commons.configuration.Configuration PUB_CONFIG =
    ConfigurationStore.getInstance().getConfiguration();
  private final String PUB_APP_CONTEXT = PUB_CONFIG.getString("ambra.platform.appContext", "");

  private static final Log log = LogFactory.getLog(AmbraActionMapper.class);

  /**
   * @see DefaultActionMapper#getMapping(HttpServletRequest, ConfigurationManager).
   */
  public ActionMapping getMapping(HttpServletRequest request, ConfigurationManager configManager) {

    // ATOM feed hook: only care about "/article/feed"
    // will factor out with comprehensive REST URI mapping
    if (request.getRequestURI().startsWith(PUB_APP_CONTEXT + "/article/feed")) {
      return mapUriToAction();
    }

    return super.getMapping(request, configManager);
  }

  /**
   * @see DefaultActionMapper#getUriFromActionMapping(ActionMapping).
   */
  public String getUriFromActionMapping(ActionMapping mapping) {

    // only care about /article/feed
    if ("getFeed".equals(mapping.getName())
      && "/article/feed".equals(mapping.getNamespace())
      && "execute".equals(mapping.getMethod())) {
      return(PUB_APP_CONTEXT + "/article/feed");
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

   return new ActionMapping(
     "getFeed",                              // name
     "/article/feed",                        // namespace
     "execute",                              // method
     null);                                  // parms
 }

}
