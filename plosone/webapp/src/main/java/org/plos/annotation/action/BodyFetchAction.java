/* $HeadURL::                                                                            $
 * $Id:BodyFetchAction.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.action;

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;

/**
 * Get the content/text/body of the annotation or reply.
 * Useful when we want to change the content before presenting it to the web layer.
 */
public class BodyFetchAction extends AnnotationActionSupport {
  private String body;
  private String bodyURL;

  private static final Log log = LogFactory.getLog(BodyFetchAction.class);

  public String execute() throws Exception {
    try {
      final String bodyContent = getAnnotationService().getBody(bodyURL);
      //htmlEncoded so that any dangerous scripting is rendered safely to viewers of the annotation.
      final String linkedContent = StringEscapeUtils.escapeHtml(bodyContent);
      body = TextUtils.hyperlink(linkedContent);
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
