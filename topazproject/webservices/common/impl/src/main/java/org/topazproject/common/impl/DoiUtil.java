/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.common.impl;

import java.util.BitSet;
import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

/**
 * Utility functions for converting between PID and URI representations of DOI's.
 *
 * @author Eric Brown and Ronald Tschalär (from ArticleImpl.java)
 */
public class DoiUtil {
  private static final BitSet DOI_PID_CHARS;
  private static final BitSet DOI_URI_CHARS;

  static {
    DOI_PID_CHARS = new BitSet(128);
    for (int ch = '0'; ch <= '9'; ch++)  DOI_PID_CHARS.set(ch);
    for (int ch = 'A'; ch <= 'Z'; ch++)  DOI_PID_CHARS.set(ch);
    for (int ch = 'a'; ch <= 'z'; ch++)  DOI_PID_CHARS.set(ch);
    DOI_PID_CHARS.set('-');
    DOI_PID_CHARS.set('_');
    DOI_PID_CHARS.set('.');
    DOI_PID_CHARS.set('~');

    DOI_URI_CHARS = new BitSet(128);
    for (int ch = '0'; ch <= '9'; ch++)  DOI_URI_CHARS.set(ch);
    for (int ch = 'A'; ch <= 'Z'; ch++)  DOI_URI_CHARS.set(ch);
    for (int ch = 'a'; ch <= 'z'; ch++)  DOI_URI_CHARS.set(ch);
    DOI_URI_CHARS.set('-');
    DOI_URI_CHARS.set('_');
    DOI_URI_CHARS.set('.');
    DOI_URI_CHARS.set('!');
    DOI_URI_CHARS.set('~');
    DOI_URI_CHARS.set('*');
    DOI_URI_CHARS.set('\'');
    DOI_URI_CHARS.set('(');
    DOI_URI_CHARS.set(')');
    DOI_URI_CHARS.set(';');
    DOI_URI_CHARS.set('/');
    DOI_URI_CHARS.set(':');
    DOI_URI_CHARS.set('@');
    DOI_URI_CHARS.set('&');
    DOI_URI_CHARS.set('=');
    DOI_URI_CHARS.set('+');
    DOI_URI_CHARS.set('$');
    DOI_URI_CHARS.set(',');
  }

  /**
   * Convert a topaz doi to a fedora pid.
   *
   * @param doi is the topaz doi
   * @return a fedora pid
   */
  private static String doi2PID(String doi) {
    return "doi:" + encode(doi, DOI_PID_CHARS);
  }

  /**
   * Convert a fedora pid to a topaz pid.
   *
   * @param pid is a fedora pid
   * @return a topaz doi
   */
  private static String pid2DOI(String pid) {
    return decode(pid.substring(4));
  }

  /**
   * Convert a topaz doi to a proper URI.
   *
   * @param doi is the topaz doi
   * @return a properly formatted URI
   */
  private static String doi2URI(String doi) {
    return "info:doi/" + encode(doi, DOI_URI_CHARS);
  }

  /**
   * Convert a URI to a topaz doi.
   *
   * @param uri is the uri to convert
   * @return a topaz doi
   */
  private static String uri2DOI(String uri) {
    return decode(uri.substring(9));
  }

  /**
   * Convert a DOI's PID to a URI.
   *
   * @param pid is the PID to convert
   * @return the URI
   */
  public static String pid2URI(String pid) {
    if (!pid.startsWith("doi:"))
      throw new IllegalArgumentException("Can only convert DOI-based PID's; pid='" + pid + "'");
    return doi2URI(pid2DOI(pid));
  }

  /**
   * Convert a DOI's URI to a PID.
   *
   * @param uri is the URI to convert
   * @return the PID
   */
  public static String uri2PID(String uri) {
    if (!uri.startsWith("info:doi/"))
      throw new IllegalArgumentException("Can only convert DOI-based URI's; uri='" + uri + "'");
    return doi2PID(uri2DOI(uri));
  }

  static String encode(String str, BitSet allowed) {
    try {
      return new String(URLCodec.encodeUrl(allowed, str.getBytes("UTF-8")), "ISO-8859-1");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);          // shouldn't happen
    }
  }

  static String decode(String str) {
    try {
      return new String(URLCodec.decodeUrl(str.getBytes("ISO-8859-1")), "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);          // shouldn't happen
    } catch (DecoderException de) {
      throw new RuntimeException(de);           // shouldn't happen
    }
  }
}
