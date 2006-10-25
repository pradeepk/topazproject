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
import org.plos.util.FileUtils;

import java.io.InputStream;
import java.net.URL;

public class FetchObjectAction extends BaseActionSupport {
  private ArticleWebService articleWebService;
  private String doi;
  private String representation;

  private InputStream inputStream;
  private String contentDisposition;
  private String contentType;
  private static final Log log = LogFactory.getLog(FetchObjectAction.class);

  public String execute() throws Exception {
    final String objectURL = articleWebService.getObjectURL(doi, representation);
    inputStream = new URL(objectURL).openStream();
    final String fileType = representation.toLowerCase();
    contentType = FileUtils.getContentType(fileType);
    contentDisposition = "filename=\"" + FileUtils.getFileName(doi) + "." + fileType + "\"";
    return SUCCESS;
  }

  /**
   * Set articleWebService
   * @param articleWebService articleWebService
   */
  public void setArticleWebService(final ArticleWebService articleWebService) {
    this.articleWebService = articleWebService;
  }

  /**
   * @param doi set doi
   */
  public void setDoi(final String doi) {
    this.doi = doi;
  }

  /**
   * @param representation set the representation of the article ex: XML, PDF, etc
   */
  public void setRepresentation(final String representation) {
    this.representation = representation;
  }

  /**
   * @return get the input stream
   */
  public InputStream getInputStream() {
    return inputStream;
  }

  /**
   * @return the filename that the file will be saved as of the client browser
   */
  public String getContentDisposition() {
    return contentDisposition;
  }

  public String getContentType() {
    return contentType;
  }
}
