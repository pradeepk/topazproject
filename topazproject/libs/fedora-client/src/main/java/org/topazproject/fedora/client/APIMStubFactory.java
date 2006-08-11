/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedora.client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

/**
 * Factory class to generate FedoraAPIM web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class APIMStubFactory {
  /**
   * Creates a FedoraAPIM service client stub..
   *
   * @param service the service configuration (url and credentials)
   *
   * @return Returns a Fedora API-M client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static FedoraAPIM create(ProtectedService service)
                           throws MalformedURLException, ServiceException {
    URL                      url      = new URL(service.getServiceUri());
    String                   protocol = url.getProtocol();
    FedoraAPIMServiceLocator locator  = new FedoraAPIMServiceLocator();

    locator.setMaintainSession(true);

    FedoraAPIM apim;

    if ("http".equals(protocol))
      apim = locator.getFedoraAPIMPortSOAPHTTP(url);
    else if ("https".equals(protocol))
      apim = locator.getFedoraAPIMPortSOAPHTTPS(url);
    else
      throw new ServiceException(protocol
                                 + " not supported for service url. (must be 'http' or 'https')");

    if (service.requiresUserNamePassword()) {
      Stub stub = (Stub) apim;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }

    return apim;
  }

  /**
   * Creates a client that does not require any authentication.
   *
   * @param serviceUri the uri for Fedora API-M service
   *
   * @return Returns a Fedora API-M client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static FedoraAPIM create(String serviceUri) throws MalformedURLException, ServiceException {
    return create(ProtectedServiceFactory.createService(serviceUri, null, null, false));
  }
}
