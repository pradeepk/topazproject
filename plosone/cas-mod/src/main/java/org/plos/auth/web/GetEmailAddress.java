/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/cas-mod/src/main/java/org#$
 * $Id: GetEmailAddress.java 649 2006-09-20 21:49:15Z viru $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.auth.web;

import org.plos.auth.AuthConstants;
import org.plos.auth.service.UserService;
import org.plos.auth.db.DatabaseException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Returns the email address given a user's GUID
 * File to change when hosting
 * D:\java\topaz-install\esup-cas-quick-start-2.0.6-1\jakarta-tomcat-5.0.28\webapps\cas\WEB-INF\web.xml

 <servlet>
   <servlet-name>Email</servlet-name>
   <servlet-class>org.plos.auth.web.GetEmailAddress</servlet-class>
 </servlet>

 <servlet-mapping>
   <servlet-name>Email</servlet-name>
   <url-pattern>/email</url-pattern>
 </servlet-mapping>
 */
public class GetEmailAddress extends HttpServlet {
  private UserService userService;

  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-Control", "no-store");
    response.setDateHeader("Expires", -1);

    final PrintWriter writer = response.getWriter();
    try {
      writer.write(userService.getEmailAddress(request.getParameter("guid")));
    } catch (DatabaseException e) {
      throw new ServletException(e);
    }
  }

  public void init(final ServletConfig servletConfig) throws ServletException {
    try {
      userService = (UserService) getServletContext().getAttribute(AuthConstants.USER_SERVICE);
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }
  }

  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }
}
