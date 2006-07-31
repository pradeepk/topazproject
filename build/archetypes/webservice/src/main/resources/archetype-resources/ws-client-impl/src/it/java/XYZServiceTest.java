#set( $service = $artifactId )
#set( $Svc = "${service.substring(0, 1).toUpperCase()}${service.substring(1)}" )
/*
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package ${package};

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 *
 */
public class ${Svc}ServiceTest extends TestCase {
  private ${Svc} service;

  public ${Svc}ServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    URL url =
        new URL("http://localhost:9997/ws-${service}-webapp-0.5-SNAPSHOT/services/${Svc}ServicePort");
    ${Svc}ServiceLocator locator = new ${Svc}ServiceLocator();
    service = locator.get${Svc}ServicePort(url);
  }

  public void testAll() throws RemoteException, IOException {
    basic${Svc}Test();
  }

  private void basic${Svc}Test() throws RemoteException, IOException {
  }
}
