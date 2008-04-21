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

package org.plos.admin.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.article.util.ArticleDeleteException;
import org.plos.article.util.ArticleUtil;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.otm.Session;

public class DeleteArticleAction extends BaseAdminActionSupport {

  private static final Log log = LogFactory.getLog(DeleteArticleAction.class);
  private Session session;
  private String article;

  public String execute() throws Exception {
    boolean error = false;

    try {
      ArticleUtil.delete(article, session);
    } catch (ArticleDeleteException ade) {
      addActionError("Failed to successfully delete article: "+article+". <br>"+ade.toString());
      log.error("Failed to successfully delete article: "+article, ade);
      error = true;
    }

    if (!error) {
      addActionMessage("Successfully deleted article: "+article);
    }

    return base();
  }

  public void setArticle(String a) {
    article = a;
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session The OTM session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }
}
