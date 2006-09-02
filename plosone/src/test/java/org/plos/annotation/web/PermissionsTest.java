/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.web;

import junit.framework.TestCase;
import org.topazproject.ws.permissions.PermissionsClientFactory;
import org.topazproject.ws.permissions.Permissions;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;

public class PermissionsTest extends TestCase {
  private String      resource = "foo:bar";
  private String[]    grants   = new String[] { "annotation:visibility-private", "foo:create", "foo:get", "foo:set", "foo:delete"  };
  private String[]    revokes    = new String[] { "foo:list-all", "foo:purge" };
  private String[]    principals = new String[] { "user:viru" };
  String uri =
    "http://localhost:9080/ws-permissions-webapp-0.5-SNAPSHOT/services/PermissionsServicePort";

  private Permissions service;

  public PermissionsTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    service = PermissionsClientFactory.create(uri);
    clearAll();
  }

  public void test1Grants() throws RemoteException {
    service.grant(resource, grants, principals);

    for (String principal : principals) {
      String[] l = service.listGrants(resource, principal);
      assertEquals("grants equal", new HashSet(Arrays.asList(l)), new HashSet(Arrays.asList(grants)));
    }
  }

  /**
   * Test for cancel grants.
   *
   * @throws RemoteException on error
   */
  public void test2CancelGrants() throws RemoteException {
    service.grant(resource, grants, principals);
    service.cancelGrants(resource, grants, principals);

    for (String principal : principals) {
      String[] l = service.listGrants(resource, principal);
      assertEquals("grants empty", l.length, 0);
    }

    // and again
    service.cancelGrants(resource, grants, principals);

    for (String principal1 : principals) {
      String[] l = service.listGrants(resource, principal1);
      assertEquals("grants empty", l.length, 0);
    }
  }

  public void test3Revokes() throws RemoteException {
    service.revoke(resource, revokes, principals);

    for (String principal : principals) {
      String[] l = service.listRevokes(resource, principal);
      assertEquals("revokes equal", new HashSet(Arrays.asList(l)), new HashSet(Arrays.asList(revokes)));
    }
  }

  public void test4CancelRevokes() throws RemoteException {
    service.revoke(resource, grants, principals);
    service.cancelRevokes(resource, grants, principals);

    for (String principal : principals) {
      String[] l = service.listRevokes(resource, principal);
      assertEquals("revokes empty", l.length, 0);
    }

    // and again
    service.cancelRevokes(resource, grants, principals);

    for (String principal1 : principals) {
      String[] l = service.listRevokes(resource, principal1);
      assertEquals("revokes empty", l.length, 0);
    }
  }

  private void clearAll() throws RemoteException {
    for (final String principal : principals) {
      String[] grants = service.listGrants(resource, principal);

      if (grants.length > 0)
        service.cancelGrants(resource, grants, new String[]{principal});

      grants = service.listRevokes(resource, principal);

      if (grants.length > 0)
        service.cancelRevokes(resource, grants, new String[]{principal});
    }
  }
}
