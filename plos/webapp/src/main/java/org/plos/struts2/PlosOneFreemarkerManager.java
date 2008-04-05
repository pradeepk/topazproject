/* $HeadURL::                                                                            $
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

package org.plos.struts2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.views.freemarker.FreemarkerManager;
import org.apache.struts2.views.freemarker.ScopesHashModel;

import com.opensymphony.xwork2.util.ValueStack;

/**
 * Custom Freemarker Manager to load up the configuration files for css, javascript, and titles of
 * pages
 * 
 * @author Stephen Cheng
 */
public class PlosOneFreemarkerManager extends FreemarkerManager {
  private PlosOneFreemarkerConfig fmConfig;

  /**
   * Sets the custom configuration object via Spring injection
   * 
   * @param fmConfig
   */
  public PlosOneFreemarkerManager(PlosOneFreemarkerConfig fmConfig) {
    this.fmConfig = fmConfig;
  }

  /**
   * Subclass from parent to add the freemarker configuratio object globally
   * 
   * @see org.apache.struts2.views.freemarker.FreemarkerManager
   */

  protected void populateContext(ScopesHashModel model, ValueStack stack, Object action, 
                                 HttpServletRequest request, HttpServletResponse response) {
    super.populateContext(model, stack, action, request, response);
    model.put("freemarker_config", fmConfig);
  }
}
