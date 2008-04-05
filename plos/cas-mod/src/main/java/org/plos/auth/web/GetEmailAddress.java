/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
      userService = (UserService) (servletConfig.getServletContext().getAttribute(AuthConstants.USER_SERVICE));
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }
  }

  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }
}
