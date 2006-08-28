/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.permissions;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import java.util.Arrays;
import java.util.HashSet;

import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 * Simple tests for the permissions service
 *
 * @author Pradeep Krishnan
 */
public class PermissionsServiceTest extends TestCase {
  private Permissions service;
  private String      resource = "foo:bar";
  private String[]    grants   = new String[] { "foo:create", "foo:get", "foo:set", "foo:delete" };
  private String[]    revokes    = new String[] { "foo:list-all", "foo:purge" };
  private String[]    principals =
    new String[] { "user:joe", "group:joe-friends", "group:joe-family" };

  /**
   * Creates a new PermissionsServiceTest object.
   *
   * @param testName DOCUMENT ME!
   */
  public PermissionsServiceTest(String testName) {
    super(testName);
  }

  /*
   * Test setup
   *
   */
  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    String uri =
      "http://localhost:9997/ws-permissions-webapp-0.5-SNAPSHOT/services/PermissionsServicePort";
    service = PermissionsClientFactory.create(uri);

    //
    clearAll();
  }

  /**
   * Test for grants.
   *
   * @throws RemoteException on error
   */
  public void test1Grants() throws RemoteException {
    service.grant(resource, grants, principals);

    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listGrants(resource, principals[i]);
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

    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listGrants(resource, principals[i]);
      assertEquals("grants empty", l.length, 0);
    }

    // and again
    service.cancelGrants(resource, grants, principals);

    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listGrants(resource, principals[i]);
      assertEquals("grants empty", l.length, 0);
    }
  }

  /**
   * Test for revokes.
   *
   * @throws RemoteException on error
   */
  public void test3Revokes() throws RemoteException {
    service.revoke(resource, revokes, principals);

    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listRevokes(resource, principals[i]);
      assertEquals("revokes equal", new HashSet(Arrays.asList(l)),
                   new HashSet(Arrays.asList(revokes)));
    }
  }

  /**
   * Test for cancel revokes.
   *
   * @throws RemoteException on error
   */
  public void test4CancelRevokes() throws RemoteException {
    service.revoke(resource, grants, principals);
    service.cancelRevokes(resource, grants, principals);

    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listRevokes(resource, principals[i]);
      assertEquals("revokes empty", l.length, 0);
    }

    // and again
    service.cancelRevokes(resource, grants, principals);

    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listRevokes(resource, principals[i]);
      assertEquals("revokes empty", l.length, 0);
    }
  }

  private void clearAll() throws RemoteException {
    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listGrants(resource, principals[i]);

      if (l.length > 0)
        service.cancelGrants(resource, l, new String[] { principals[i] });

      l = service.listRevokes(resource, principals[i]);

      if (l.length > 0)
        service.cancelRevokes(resource, l, new String[] { principals[i] });
    }
  }
}
