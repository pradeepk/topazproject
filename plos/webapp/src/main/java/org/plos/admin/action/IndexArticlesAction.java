/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
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
package org.plos.admin.action;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;

/**
 * Index all Articles.
 * 
 * @author jsuttor
 */
public class IndexArticlesAction extends BaseAdminActionSupport {

  private static final Log log = LogFactory.getLog(IndexArticlesAction.class);
  
  @Override
  public String execute() throws Exception {
    
    Map<String, String> results = new HashMap();
    try {
      results = getDocumentManagementService().indexArticles();
    } catch (ApplicationException ae) {
      final String errorMsg = "Exception during indexing";
      addActionError(errorMsg + ": " + ae);
      log.error(errorMsg, ae);
    }
    
    // pass results on to console
    addActionMessage("indexed " + results.size() + " Articles in current Journal");
    for (String key : results.keySet()) {
      addActionMessage(key + " : " + results.get(key));
    }
        
    return SUCCESS;
  }
}
