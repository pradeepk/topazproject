/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.auth.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * Replaces the Username with the user's GUID so that the username is the GUID for any responses to clients.
 * File to change when hosting
 * D:\java\topaz-install\esup-cas-quick-start-2.0.6-1\jakarta-tomcat-5.0.28\webapps\cas\WEB-INF\web.xml

  <filter>
    <filter-name>UsernameReplacementWithGuidFilter</filter-name>
    <filter-class>org.plos.auth.web.UsernameReplacementWithGuidFilter</filter-class>
  </filter>

  <filter-mapping >
    <filter-name>UsernameReplacementWithGuidFilter</filter-name>
    <url-pattern>/login</url-pattern>
  </filter-mapping>


 Tips for using a authentication handler
 Edit the file "genericHandler.xml" located at:
 D:\java\topaz-install\esup-cas-quick-start-2.0.6-1\esup-casgeneric-2.0.6-1\webpages\WEB-INF

 Possible entry in it maybe:
  <authentication empty_password_accepted="on" debug="on">
    <handler>
   <!-- fails as it assumes a md5 hashing algorithm, so we would need to have out own handler inplementation  -->
      <classname>
        org.esupportail.cas.server.handlers.database.SearchDatabaseHandler
      </classname>
  	<config>
        <table>plos_user</table>
        <login_column>id</login_column>
        <password_column>password</password_column>
        <bind_username>postgres</bind_username>
        <bind_password>postgres</bind_password>
        <server>
          <jdbc_driver>org.postgresql.Driver</jdbc_driver>
          <jdbc_url>jdbc:postgresql://localhost/postgres</jdbc_url>
        </server>
      </config>
    </handler>
  </authentication>
*/
public class UsernameReplacementWithGuidFilter implements Filter {
  private static final Log log = LogFactory.getLog(UsernameReplacementWithGuidFilter.class);
  private UserService userService;
  public String USERNAME_PARAMETER = "username";

  public void init(final FilterConfig filterConfig) throws ServletException {
    try {
      userService = (UserService) filterConfig.getServletContext().getAttribute(AuthConstants.USER_SERVICE);
    } catch (final Exception e) {
      throw new ServletException(e);
    }
  }

  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
    try {
      HttpServletRequest httpRequest = (HttpServletRequest) request;

      if (log.isDebugEnabled()) {
        dumpRequest(httpRequest, "before chain");
      }

      final String usernameParameter = request.getParameter(USERNAME_PARAMETER);

      if (!((null == usernameParameter) || (usernameParameter.length() == 0))) {
        final String username = usernameParameter.toString();
        httpRequest = new UsernameRequestWrapper(httpRequest, username, usernameParameter);
      }

      filterChain.doFilter(httpRequest, response);

/*
      final PrintWriter out = response.getWriter();
      final CharResponseWrapper servletResponse = new CharResponseWrapper((HttpServletResponse) response);
      filterChain.doFilter(httpRequest, servletResponse);

      out.write(dumpResponse(servletResponse));
      out.close();
*/
      
    } catch (final Exception ex) {
      log.error("Exception raised in UsernameReplacementWithGuidFilter", ex);
    }
  }

  private class UsernameRequestWrapper extends HttpServletRequestWrapper {
    private final String username;
    private final String usernameParameter;

    public UsernameRequestWrapper(final HttpServletRequest httpRequest, final String username, final String usernameParameter) {
      super(httpRequest);
      this.username = username;
      this.usernameParameter = usernameParameter;
    }

    public String getParameter(final String parameterName) {
      if (USERNAME_PARAMETER.equals(parameterName)) {
        final String guid = getUserGuid(username);
        log.debug("guid:" + guid);
        return guid;
      }
      return super.getParameter(parameterName);
    }

    private String getUserGuid(final String username) {
      try {
        return userService.getGuid(username);
      } catch (final DatabaseException e) {
        log.debug("No account found for userId:" + usernameParameter, e);
        return null;
      }
    }
  }

  private String dumpResponse(final CharResponseWrapper wrapper) throws IOException {
    log.debug("Response ContentType:" + wrapper.getContentType());
    CharArrayWriter caw = new CharArrayWriter();
    caw.write(wrapper.toString());
    final String response = caw.toString();
    log.debug("Response generated:");
    log.debug(response);
    return response;
  }

  private void dumpRequest(final HttpServletRequest request, final String prefix) {
    log.debug(prefix + "----------------" + System.currentTimeMillis());
    log.debug("url:" + request.getRequestURL());
    log.debug("query string:" + request.getQueryString());
    {
      log.debug("Request Attributes:");
      final Enumeration attribs = request.getAttributeNames();
      while (attribs.hasMoreElements()) {
        final String attribName = (String) attribs.nextElement();
        log.debug(attribName + ":" + request.getAttribute(attribName).toString());
      }
    }

    {
      log.debug("Request Parameters:");
      final Enumeration params = request.getParameterNames();
      while (params.hasMoreElements()) {
        final String paramName = (String) params.nextElement();
        log.debug(paramName + ":" + request.getParameterValues(paramName).toString());
      }
    }
  }

  private void dumpSession(final HttpSession initialSession) {
    log.debug("Session Attributes:");
    final Enumeration attribs1 = initialSession.getAttributeNames();
    while (attribs1.hasMoreElements()) {
      final String attribName1 = (String) attribs1.nextElement();
      log.debug(attribName1 + ":" + initialSession.getAttribute(attribName1).toString());
    }
  }

  public void destroy() {
  }
}


class CharResponseWrapper extends HttpServletResponseWrapper {
  private CharArrayWriter output;

  public String toString() {
    return output.toString();
  }

  public CharResponseWrapper(final HttpServletResponse response) {
    super(response);
    output = new CharArrayWriter();
  }

  public PrintWriter getWriter() {
    return new PrintWriter(output);
  }
}
