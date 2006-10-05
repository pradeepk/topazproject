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

import edu.yale.its.tp.cas.client.CASReceipt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.user.Constants;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Create a new session when the user logs in using CAS or other authentication mechanisms
 */
public class CreateSessionOnLoginFilter implements Filter {
  private static final Log log = LogFactory.getLog(CreateSessionOnLoginFilter.class);
  private final String MEMBER_SESSION_CREATED_FLAG = "MemberSessionCreatedFlag";

  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
    try {
      final HttpServletRequest servletRequest = (HttpServletRequest) request;
      final HttpSession initialSession = servletRequest.getSession();

//    if member session was already recreated let the call pass through
      if (null != initialSession.getAttribute(MEMBER_SESSION_CREATED_FLAG)) {
        filterChain.doFilter(request, response);
      } else {

        if (log.isTraceEnabled()) {
          log.debug("Initial attributes in session were:");
          final Enumeration attribs = initialSession.getAttributeNames();
          while (attribs.hasMoreElements()) {
            final String attribName = (String) attribs.nextElement();
            log.debug(attribName + ":" + initialSession.getAttribute(attribName).toString());
          }
        }

        final String casUser = (String) initialSession.getAttribute(Constants.SINGLE_SIGNON_USER_KEY);
        if (null == casUser) {
          log.debug("No CAS receipt found");
        } else {
          final CASReceipt casReceipt = (CASReceipt) initialSession.getAttribute(Constants.SINGLE_SIGNON_RECEIPT);
          initialSession.invalidate();

          final HttpSession newSession = servletRequest.getSession(true);
          newSession.setAttribute(Constants.SINGLE_SIGNON_RECEIPT, casReceipt);
          newSession.setAttribute(Constants.SINGLE_SIGNON_USER_KEY, casUser);

          newSession.setAttribute(MEMBER_SESSION_CREATED_FLAG, "true");
          filterChain.doFilter(request, response);

          log.debug("Member session created with CAS ticket:" + casReceipt.getPgtIou());
        }
      }
    } catch (IOException io) {
      log.error("IOException raised in Filter1 Filter", io);
    } catch (ServletException se) {
      log.error("ServletException raised in Filter1 Filter", se);
    }
  }

  public void init(final FilterConfig filterConfig) throws ServletException {}
  public void destroy() {}
}
