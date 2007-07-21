/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.web;

/**
 * The virtual journal context.
 *
 * Various values relating to the virtual journal context and the current Request.
 * Designed to be put into the Request as an attribute for easy access by the web application.
 * In particular, allows templates full consistent access to the same information that is available
 * to the Java Action.
 *
 * Note that templates do not have full access to the Request, so a Request
 * attribute is used to bridge between the two.
 *
 * Application usage:
 * <pre>
 * VirtualJournalContext requestContent = ServletRequest.getAttribute(PUB_VIRTUALJOURNAL_CONTEXT);
 * String requestJournal = requestContext.getJournal();
 * </pre>
 */
public class VirtualJournalContext {

  /** ServletRequest attribute for the virtual journal context. */
  public static final String PUB_VIRTUALJOURNAL_CONTEXT = "pub.virtualjournal.context";

  /** Default virtual journal name. */
  public static final String PUB_VIRTUALJOURNAL_DEFAULT_JOURNAL = "";

  /** Default virtual journal mapping prefix. */
  public static final String PUB_VIRTUALJOURNAL_DEFAULT_MAPPINGPREFIX = "";

  private final String journal;
  private final String mappingPrefix;
  private final String requestScheme;
  private final int    requestPort;
  private final String requestServerName;
  private final String requestContext;
  private final String baseUrl;

  /**
   * Construct an immutable VirtualJournalContext.
   */
  public VirtualJournalContext(final String journal, final String mappingPrefix,
    final String requestScheme, final int requestPort, final String requestServerName,
    final String requestContext) {

    this.journal           = journal;
    this.mappingPrefix     = mappingPrefix;
    this.requestScheme     = requestScheme;
    this.requestPort       = requestPort;
    this.requestServerName = requestServerName;
    this.requestContext    = requestContext;

    // assume that we're dealing with http or https schemes for now
    StringBuilder urlBaseValue = new StringBuilder(requestScheme).append("://")
                                                  .append(requestServerName);
    if (requestScheme != null) {
      //assume that we don't want to put the default ports numbers on the URL
      if (("http".equals(requestScheme.toLowerCase()) && requestPort != 80) ||
          ("https".equals(requestScheme.toLowerCase()) && requestPort != 443)) {
        urlBaseValue.append(":").append(requestPort);
      }
    }
    urlBaseValue.append(requestContext).append("/");
    this.baseUrl = urlBaseValue.toString();
  }

  /**
   * Get the virtual journal name.
   *
   * @return Journal name, may be <code>null</code>.
   */
  public String getJournal() {
    return journal;
  }

  /**
   * Get the virtual journal mapping prefix.
   *
   * @return Mapping prefix, may be "".
   */
  public String getMappingPrefix() {
    return mappingPrefix;
  }

  /**
   * Get the virtual journal Request scheme.
   *
   * @return Request scheme.
   */
  public String getRequestScheme() {
    return requestScheme;
  }

  /**
   * Get the virtual journal Request port.
   *
   * @return Request port.
   */
  public int getRequestPort() {
    return requestPort;
  }

  /**
   * Get the virtual journal Request server name.
   *
   * @return Request server name.
   */
  public String getRequestServerName() {
    return requestServerName;
  }

  /**
   * Get the virtual journal Request context.
   *
   * @return Request context.
   */
  public String getRequestContext() {
    return requestContext;
  }

  /**
   * Get the base url of the request which consists of the scheme, server name, server port, and
   * context with an ending slash.
   *
   * @return string representing the base request URL
   */
  public String getBaseUrl () {
    return baseUrl;
  }
}
