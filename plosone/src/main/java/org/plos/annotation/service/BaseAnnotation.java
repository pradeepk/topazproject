/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import com.opensymphony.util.TextUtils;
import org.plos.ApplicationException;

/**
 * Base class for Annotation and reply.
 * For now it does not bring together all the common attributes as I still prefer delegation for now.
 * Further uses of these classes on the web layer should clarify the requirements and drive any changes
 * if required.
 */
public abstract class BaseAnnotation {
  /**
   * @return the escaped body.
   * @throws org.plos.ApplicationException
   */
  public String getBody() throws ApplicationException {
    return escapeText(getOriginalBodyContent());
  }

  protected abstract String getOriginalBodyContent() throws ApplicationException;

  public abstract boolean getVisibility() throws ApplicationException;

  /**
   * Escape text so as to avoid any java scripting maliciousness when rendering it on a web page
   * @param text text
   * @return the escaped text
   */
  protected String escapeText(final String text) {
    return TextUtils.htmlEncode(text);
  }

}
