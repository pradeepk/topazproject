/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.util;

import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;

/**
 * Provides some useful text manipulation functions.
 */
public class TextUtils {
  /**
   * Linkify any possible web links excepting email addresses
   * @param text text
   * @return hyperlinked text
   */
  public static String hyperlink(final String text) {
    String notNullText = (null == text)?"": text;
    notNullText =  com.opensymphony.util.TextUtils.plainTextToHtml(notNullText);
    StringBuilder retStr = new StringBuilder("<p>");
    retStr.append(notNullText);
    retStr.append("</p>");
    return (retStr.toString());
  }

  /**
   * Return the escaped html. Useful when you want to make any dangerous scripts safe to render.
   * @param bodyContent bodyContent
   * @return escaped html text
   */
  public static String escapeHtml(final String bodyContent) {
    return StringEscapeUtils.escapeHtml(bodyContent);
  }

  /**
   * @param bodyContent bodyContent
   * @return Return escaped and hyperlinked text
   */
  public static String escapeAndHyperlink(final String bodyContent) {
    return hyperlink(escapeHtml(bodyContent));
  }

  
  /**
   * Transforms an org.w3c.dom.Document into a String
   * 
   * @param doc Document to transform
   * @return String representation of doc
   * @throws TransformerException
   */
  public static String getAsXMLString(final Document doc) throws TransformerException {
    final Transformer tf = TransformerFactory.newInstance().newTransformer();
//    tf.setOutputProperty("indent", "yes");
    final StringWriter stringWriter = new StringWriter();
    tf.transform(new DOMSource(doc), new StreamResult(stringWriter));
    return stringWriter.toString();
  }


}
