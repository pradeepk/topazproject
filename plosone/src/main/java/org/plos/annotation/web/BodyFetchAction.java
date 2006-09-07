/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.web;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;

/**
 * TODO: See if action chaining can help as the htmlEncoding is needing both for title and body. So we could have a FetchAction and a HtmlEncodeAction.
 * Get the content/text/body of the annotation or reply. Useful when we want to change the content before presenting it to the web layer.
 */
public class BodyFetchAction extends AnnotationActionSupport {
  private String body;
  private String bodyURL;

  private static final Log log = LogFactory.getLog(BodyFetchAction.class);

  public String execute() throws Exception {
    try {
      final String bodyContent = getAnnotationService().getBody(bodyURL);
      //htmlEncoded so that any dangerous scripting is rendered safely to viewers of the annotation.
      body =  TextUtils.htmlEncode(bodyContent);
    } catch (final ApplicationException e) {
      log.error(e, e);
      addActionError("Getting the annotation body failed with error message: " + e.getMessage());
      return ERROR;
    }

    return SUCCESS;
  }

  public String getBody() {
    return body;
  }

  /**
   * @param bodyURL the url to fetch the body.
   */
  public void setBodyUrl(final String bodyURL) {
    this.bodyURL = bodyURL;
  }

  @RequiredStringValidator(message = "Url of the body cannot be empty")
  public String getBodyURL() {
    return bodyURL;
  }
}
