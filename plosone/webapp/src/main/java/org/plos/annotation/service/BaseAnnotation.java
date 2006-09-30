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
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Base class for Annotation and reply.
 * For now it does not bring together all the common attributes as I still prefer delegation for now.
 * Further uses of these classes on the web layer should clarify the requirements and drive any changes
 * if required.
 */
public abstract class BaseAnnotation {
  /** An integer constant to indicate a unique value for the  */
  public static final int PUBLIC_MASK = 0x001;

  /**
   * @return the escaped body.
   * @throws org.plos.ApplicationException ApplicationException
   */
  public String getBody() throws ApplicationException {
    return escapeText(getOriginalBodyContent());
  }

  /**
   * @return the original content of the annotation body
   * @throws ApplicationException ApplicationException
   */
  protected abstract String getOriginalBodyContent() throws ApplicationException;

  /**
   * Escape text so as to avoid any java scripting maliciousness when rendering it on a web page
   * @param text text
   * @return the escaped text
   */
  protected String escapeText(final String text) {
    //TODO: Validate that this works ok.
    final String commonsEscaped = StringEscapeUtils.escapeHtml(text);
    final String symphonyEscaped = TextUtils.htmlEncode(text);
    assert(commonsEscaped.equals(symphonyEscaped));
    return commonsEscaped;
  }

  /**
   * @param state state
   * @return true if the annotation/reply is public, false if private
   */
  protected boolean checkIfPublic(final int state) {
    return (state & PUBLIC_MASK) == 1;
  }
}
