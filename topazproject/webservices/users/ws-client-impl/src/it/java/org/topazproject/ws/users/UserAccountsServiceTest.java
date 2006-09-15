/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.users;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 * Simple tests for the profiles service.
 *
 * @author Ronald Tschalär
 */
public class UserAccountsServiceTest extends TestCase {
  private UserAccounts service;
  private String       userId;

  public UserAccountsServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    String uri =
      "http://localhost:9997/ws-users-webapp-0.5-SNAPSHOT/services/UserAccountsServicePort";
    service = UserAccountsClientFactory.create(uri);
  }

  protected void tearDown() throws RemoteException {
    try {
      if (userId != null)
        service.deleteUser(userId);
    } catch (NoSuchUserIdException nsie) {
      // looks like it was clean
    }
  }

  public void testNonExistentUser() throws RemoteException, IOException {
    boolean gotE = false;
    try {
      service.deleteUser("id:muster");
    } catch (NoSuchUserIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    gotE = false;
    try {
      service.setAuthenticationIds("id:muster", null);
    } catch (NoSuchUserIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    gotE = false;
    try {
      service.getAuthenticationIds("id:muster");
    } catch (NoSuchUserIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);
  }

  public void testInvalidUser() throws RemoteException, IOException {
    boolean gotE = false;
    try {
      service.deleteUser("muster");
    } catch (NoSuchUserIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    gotE = false;
    try {
      service.setAuthenticationIds("muster", null);
    } catch (NoSuchUserIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    gotE = false;
    try {
      service.getAuthenticationIds("muster");
    } catch (NoSuchUserIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);
  }

  public void testNullUser() throws RemoteException, IOException {
    boolean gotE = false;
    try {
      service.deleteUser(null);
    } catch (RemoteException re) {
      gotE = true;
    }
    assertTrue("Failed to get expected RemoteException", gotE);

    gotE = false;
    try {
      service.setAuthenticationIds(null, null);
    } catch (RemoteException re) {
      gotE = true;
    }
    assertTrue("Failed to get expected RemoteException", gotE);

    gotE = false;
    try {
      service.getAuthenticationIds(null);
    } catch (RemoteException re) {
      gotE = true;
    }
    assertTrue("Failed to get expected RemoteException", gotE);
  }

  public void testBasicUserAccounts() throws RemoteException, IOException {
    userId = service.createUser("musterAuth");
    service.deleteUser(userId);

    userId = service.createUser("musterAuth");
    String[] authIds = service.getAuthenticationIds(userId);
    checkAuthIds(userId, authIds, new String[] { "musterAuth" });

    authIds = new String[] { "id1", "foo" };
    service.setAuthenticationIds(userId, authIds);
    String[] got = service.getAuthenticationIds(userId);
    checkAuthIds(userId, got, authIds);

    authIds = new String[0];
    service.setAuthenticationIds(userId, authIds);
    got = service.getAuthenticationIds(userId);
    checkAuthIds(userId, got, authIds);

    authIds = null;
    service.setAuthenticationIds(userId, authIds);
    got = service.getAuthenticationIds(userId);
    checkAuthIds(userId, got, new String[0]);

    authIds = new String[] { "id1", "foo" };
    service.setAuthenticationIds(userId, authIds);
    got = service.getAuthenticationIds(userId);
    checkAuthIds(userId, got, authIds);

    String user2Id = service.createUser("musterAuth");
    got = service.getAuthenticationIds(user2Id);
    checkAuthIds(userId, got, new String[] { "musterAuth" });

    String id = service.lookUpUserByAuthId("foo");
    assertEquals("user-id mismatch,", userId, id);

    id = service.lookUpUserByAuthId("musterAuth");
    assertEquals("user-id mismatch,", user2Id, id);

    id = service.lookUpUserByAuthId("id1");
    assertEquals("user-id mismatch,", userId, id);

    id = service.lookUpUserByAuthId("bar");
    assertNull("user-id mismatch", id);

    service.deleteUser(userId);
    service.deleteUser(user2Id);
  }

  public void testUserAccountState() throws RemoteException, IOException {
    userId = service.createUser("musterAuth");
    int state = service.getState(userId);
    assertEquals("state mismatch,", 0, state);

    service.setState(userId, 42);
    state = service.getState(userId);
    assertEquals("state mismatch,", 42, state);

    boolean gotE = false;
    try {
      service.setState("foo:bar", 42);
    } catch (NoSuchUserIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    gotE = false;
    try {
      service.getState("foo:bar");
    } catch (NoSuchUserIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    service.deleteUser(userId);
  }

  private void checkAuthIds(String userId, String[] got, String[] exp) {
    assertNotNull("got null auth-ids for '" + userId + "'", got);
    assertEquals("Number of auth-ids mismatch;", exp.length, got.length);

    Arrays.sort(got);
    Arrays.sort(exp);

    for (int idx = 0; idx < got.length; idx++)
      assertEquals("Auth-id mismatch, ", exp[idx], got[idx]);
  }
}
