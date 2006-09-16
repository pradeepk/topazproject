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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Returns the email address given a user's GUID
 * File to change when hosting
 * D:\java\topaz-install\esup-cas-quick-start-2.0.6-1\jakarta-tomcat-5.0.28\webapps\cas\WEB-INF\web.xml
 * 
 <context-param>
   <param-name>jdbcDriver</param-name>
   <param-value>org.postgresql.Driver</param-value>
 </context-param>
 <context-param>
   <param-name>jdbcUrl</param-name>
   <param-value>jdbc:postgresql://localhost/postgres</param-value>
 </context-param>
 <context-param>
   <param-name>usernameToGuidSql</param-name>
   <param-value>select id from plos_user where loginname=?</param-value>
 </context-param>
 <context-param>
   <param-name>guidToUsernameSql</param-name>
   <param-value>select loginname from plos_user where id=?</param-value>
 </context-param>
 <context-param>
   <param-name>adminUser</param-name>
   <param-value>postgres</param-value>
 </context-param>
 <context-param>
   <param-name>adminPassword</param-name>
   <param-value>postgres</param-value>
 </context-param>

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
    } catch (SQLException e) {
      throw new ServletException(e);
    }
  }

  public void init(final ServletConfig servletConfig) throws ServletException {
    try {
      userService = new UserService(servletConfig.getServletContext());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }
  }

  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }
}
