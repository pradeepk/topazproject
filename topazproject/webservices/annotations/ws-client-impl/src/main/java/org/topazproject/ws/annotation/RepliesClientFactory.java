/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.annotation;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

/**
 * Factory class to generate Replies web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class RepliesClientFactory {
  /**
   * Creates an Replies service client stub..
   *
   * @param service the service configuration (url and credentials)
   *
   * @return Returns an Replies service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static Replies create(ProtectedService service)
                           throws MalformedURLException, ServiceException {
    URL                      url     = new URL(service.getServiceUri());
    RepliesServiceLocator locator = new RepliesServiceLocator();

    locator.setMaintainSession(true);

    Replies replies = locator.getReplyServicePort(url);

    if (service.requiresUserNamePassword()) {
      Stub stub = (Stub) replies;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }

    return replies;
  }

  /**
   * Creates a client that does not require any authentication.
   *
   * @param serviceUri the uri for Replies service
   *
   * @return Returns an Replies service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static Replies create(String serviceUri)
                           throws MalformedURLException, ServiceException {
    return create(ProtectedServiceFactory.createService(serviceUri, null, null, false));
  }
}
