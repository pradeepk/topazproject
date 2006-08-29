/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service.impl;

import com.opensymphony.util.TextUtils;

/**
 * Base class for Annotation and reply.
 * For now it does not being together all the common attributes as I still prefer delegation as of now.
 * Further uses of these classes on the web layer should clarify the requirements and drive any change
 * if required.
 */
public abstract class BaseAnnotation {
  private String body;

  /**
   * @return the escaped body.
   */
  public String getBody() {
    return escapeText(body);
  }

  /**
   * Escape text so as to avoid any java scripting maliciousness when rendering it on a web page
   * @param text text
   * @return the escaped text
   */
  protected String escapeText(final String text) {
    return TextUtils.htmlEncode(text);
  }

  /** Set the text content of the annotation, not the URL
   * @param bodyText bodyText
   */
  public void setBody(final String bodyText) {
    body = bodyText;
  }
}
