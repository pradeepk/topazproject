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

import edu.yale.its.tp.cas.client.filter.CASFilter;
import edu.yale.its.tp.cas.client.CASReceipt;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Create a new session when the user logs in using CAS or other authentication mechanisms
 */
public class CreateSessionOnLoginFilter implements Filter {
  private static final Log log = LogFactory.getLog(CreateSessionOnLoginFilter.class);

  public void init(final FilterConfig filterConfig) throws ServletException {
  }

  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
    try {
      filterChain.doFilter(request, response);

      final HttpServletRequest servletRequest = (HttpServletRequest) request;
      final HttpSession initialSession = servletRequest.getSession();

      if (log.isDebugEnabled()) {
        log.debug("Initial attributes in session were:");
        final Enumeration attribs = initialSession.getAttributeNames();
        while (attribs.hasMoreElements()) {
          final String attribName = (String) attribs.nextElement();
          log.debug(attribName + ":" + initialSession.getAttribute(attribName));
        }
      }

      final CASReceipt casReceipt = (CASReceipt) initialSession.getAttribute(CASFilter.CAS_FILTER_RECEIPT);
      if (null == casReceipt) {
        log.debug("No CAS receipt found");
      } else {
        initialSession.invalidate();

        final HttpSession newSession = servletRequest.getSession(true);
        newSession.setAttribute(CASFilter.CAS_FILTER_RECEIPT, casReceipt);

        log.debug("Member session created with CAS ticket:" + casReceipt.getPgtIou());
      }

    } catch (IOException io) {
      log.error("IOException raised in Filter1 Filter", io);
    } catch (ServletException se) {
      log.error("ServletException raised in Filter1 Filter", se);
    }
  }

  public void destroy() {
  }
}
