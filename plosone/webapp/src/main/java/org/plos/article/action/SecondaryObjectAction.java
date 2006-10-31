/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.ArticleWebService;
import org.plos.article.service.SecondaryObject;

/**
 * Fetch the secondary objects for a given uri
 */
public class SecondaryObjectAction extends BaseActionSupport {
  private String uri;
  private SecondaryObject[] secondaryObjects;
  private ArticleWebService articleWebService;
  private static final Log log = LogFactory.getLog(SecondaryObjectAction.class);

  public String execute() throws Exception {
    try {
      secondaryObjects = articleWebService.listSecondaryObjects(uri);
    } catch (Exception ex) {
      log.warn(ex);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Set the uri
   * @param uri uri
   */
  public void setUri(final String uri) {
    this.uri = uri;
  }

  /**
   * Get the secondary objects.
   * @return secondary objects
   */
  public SecondaryObject[] getSecondaryObjects() {
    return secondaryObjects;
  }

  /**
   * Set the secondary objects
   * @param articleWebService articleWebService
   */
  public void setArticleWebService(final ArticleWebService articleWebService) {
    this.articleWebService = articleWebService;
  }
}
