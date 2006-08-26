/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

import org.topazproject.ws.permissions.Permissions;
import org.topazproject.ws.permissions.PermissionsServiceLocator;
import org.topazproject.ws.users.UserAccounts;
import org.topazproject.ws.users.UserAccountsServiceLocator;

/**
 * Simple tests for the profiles service.
 *
 * @author Ronald Tschalär
 */
public class ProfilesServiceTest extends TestCase {
  private Profiles     service;
  private UserAccounts userService;
  private Permissions  permsService;
  private String       userId;
  private String[]     guestIds;

  public ProfilesServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    URL url =
        new URL("http://localhost:9997/ws-pap-webapp-0.1-SNAPSHOT/services/ProfilesServicePort");
    ProfilesServiceLocator locator = new ProfilesServiceLocator();
    locator.setMaintainSession(true);
    service = locator.getProfilesServicePort(url);

    url =
      new URL("http://localhost:9997/ws-users-webapp-0.1-SNAPSHOT/services/UserAccountsServicePort");
    UserAccountsServiceLocator uaLoc = new UserAccountsServiceLocator();
    uaLoc.setMaintainSession(true);
    userService = uaLoc.getUserAccountsServicePort(url);

    url =
      new URL("http://localhost:9997/ws-permissions-webapp-0.1-SNAPSHOT/services/PermissionsServicePort");
    PermissionsServiceLocator pLoc = new PermissionsServiceLocator();
    pLoc.setMaintainSession(true);
    permsService = pLoc.getPermissionsServicePort(url);

    // create the users
    userId   = userService.createUser("musterAuth");
    guestIds = new String[] { userService.createUser("guestAuth") };
  }

  protected void tearDown() throws RemoteException {
    try {
      service.setProfile(userId, null);
    } catch (NoSuchIdException nsie) {
      // looks like it was clean
    }

    for (int idx = 0; idx < guestIds.length; idx++) {
      String[] l = permsService.listGrants(userId, guestIds[idx]);
      if (l.length > 0)
        permsService.cancelGrants(userId, l, new String[] { guestIds[idx] });

      l = permsService.listRevokes(userId, guestIds[idx]);
      if (l.length > 0)
      permsService.cancelRevokes(userId, l, new String[] { guestIds[idx] });

      try {
        userService.deleteUser(guestIds[idx]);
      } catch (NoSuchIdException nsie) {
        // looks like it was clean
      }
    }

    try {
      userService.deleteUser(userId);
    } catch (NoSuchIdException nsie) {
      // looks like it was clean
    }
  }

  public void testBasicProfiles() throws RemoteException, IOException {
    // test non-existent user
    boolean gotE = false;
    try {
      service.setProfile("id:muster42", null);
    } catch (NoSuchIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchIdException", gotE);

    gotE = false;
    try {
      service.getProfile("id:muster42");
    } catch (NoSuchIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchIdException", gotE);

    // test empty profile
    UserProfile prof = service.getProfile(userId);
    assertNull("non-null profile for user '" + userId + "'", prof);

    // test profile
    prof = new UserProfile();
    prof.setDisplayName("Hans");
    prof.setEmail("hans@muster.eu");
    prof.setHomePage("http://www.muster.eu/");
    service.setProfile(userId, prof);

    prof = service.getProfile(userId);
    assertEquals("Name mismatch", "Hans", prof.getDisplayName());
    assertEquals("Email mismatch", "hans@muster.eu", prof.getEmail());
    assertEquals("Homepage mismatch", "http://www.muster.eu/", prof.getHomePage());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null title, got '" + prof.getTitle() + "'", prof.getTitle());
    assertNull("non-null gender, got '" + prof.getGender() + "'", prof.getGender());
    assertNull("non-null weblog, got '" + prof.getWeblog() + "'", prof.getWeblog());
    assertNull("non-null pubs, got '" + prof.getPublications() + "'", prof.getPublications());
    assertNull("non-null biography, got '" + prof.getBiography() + "'", prof.getBiography());
    assertNull("non-null interests, got '" + prof.getInterests() + "'", prof.getInterests());

    prof.setHomePage(null);
    prof.setEmail("hans.muster@sample.com");
    prof.setWeblog("http://muster.blogs.org");
    prof.setInterests(new String[] { "http://i1.org/", "http://i2.org/" });
    service.setProfile(userId, prof);

    prof = service.getProfile(userId);
    assertEquals("Name mismatch;", "Hans", prof.getDisplayName());
    assertEquals("Email mismatch;", "hans.muster@sample.com", prof.getEmail());
    assertEquals("Weblog mismatch;", "http://muster.blogs.org", prof.getWeblog());
    assertNull("non-null homepage, got '" + prof.getHomePage() + "'", prof.getHomePage());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null title, got '" + prof.getTitle() + "'", prof.getTitle());
    assertNull("non-null gender, got '" + prof.getGender() + "'", prof.getGender());
    assertNull("non-null pubs, got '" + prof.getPublications() + "'", prof.getPublications());
    assertNull("non-null biography, got '" + prof.getBiography() + "'", prof.getBiography());
    String[] i = prof.getInterests();
    assertEquals("Interests mismatch;", 2, i.length);
    if (!(i[0].equals("http://i1.org/") && i[1].equals("http://i2.org/") ||
          i[1].equals("http://i1.org/") && i[0].equals("http://i2.org/")))
      fail("Interests mismatch; i0='" + i[0] + "', i1='" + i[1] + "'");

    prof.setRealName("rn");
    prof.setTitle("mr");
    prof.setGender("male");
    prof.setBiography("http://bio/");
    prof.setPublications("http://pubs/");
    prof.setInterests(null);
    service.setProfile(userId, prof);

    prof = service.getProfile(userId);
    assertEquals("Name mismatch;", "Hans", prof.getDisplayName());
    assertEquals("Email mismatch;", "hans.muster@sample.com", prof.getEmail());
    assertEquals("Weblog mismatch;", "http://muster.blogs.org", prof.getWeblog());
    assertEquals("Publications mismatch;", "http://pubs/", prof.getPublications());
    assertEquals("Real-name mismatch;", "rn", prof.getRealName());
    assertEquals("Title mismatch;", "mr", prof.getTitle());
    assertEquals("Gender mismatch;", "male", prof.getGender());
    assertEquals("Biography mismatch;", "http://bio/", prof.getBiography());
    assertNull("non-null interests, got '" + prof.getInterests() + "'", prof.getInterests());

    service.setProfile(userId, null);
    prof = service.getProfile(userId);
    assertNull("non-null profile for user '" + userId + "'", prof);
  }

  /* We need to be able to log in in order to run these tests.
  public void testProfilesPermissions() throws RemoteException, IOException {
    UserProfile prof = new UserProfile();
    prof.setDisplayName("Hans");
    prof.setEmail("hans@muster.eu");
    prof.setHomePage("http://www.muster.eu/");
    service.setProfile(userId, prof);

    prof = service.getProfile(userId);
    assertEquals("Name mismatch;", "Hans", prof.getDisplayName());
    assertEquals("Email mismatch;", "hans@muster.eu", prof.getEmail());
    assertEquals("Homepage mismatch;", "http://www.muster.eu/", prof.getHomePage());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null title, got '" + prof.getTitle() + "'", prof.getTitle());
    assertNull("non-null gender, got '" + prof.getGender() + "'", prof.getGender());
    assertNull("non-null weblog, got '" + prof.getWeblog() + "'", prof.getWeblog());
    assertNull("non-null pubs, got '" + prof.getPublications() + "'", prof.getPublications());
    assertNull("non-null biography, got '" + prof.getBiography() + "'", prof.getBiography());
    assertNull("non-null interests, got '" + prof.getInterests() + "'", prof.getInterests());

    doAsUser(guestIds[0], prof = service.getProfile(userId));
    assertNull("non-null name, got '" + prof.getDisplayName() + "'", prof.getDisplayName());
    assertNull("non-null email, got '" + prof.getEmail() + "'", prof.getEmail());
    assertNull("non-null homepage, got '" + prof.getHomePage() + "'", prof.getHomePage());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null title, got '" + prof.getTitle() + "'", prof.getTitle());
    assertNull("non-null gender, got '" + prof.getGender() + "'", prof.getGender());
    assertNull("non-null weblog, got '" + prof.getWeblog() + "'", prof.getWeblog());
    assertNull("non-null pubs, got '" + prof.getPublications() + "'", prof.getPublications());
    assertNull("non-null biography, got '" + prof.getBiography() + "'", prof.getBiography());
    assertNull("non-null interests, got '" + prof.getInterests() + "'", prof.getInterests());

    permsService.grant(userId, new String[] { "profiles:getEmail" }, guestIds);

    doAsUser(guestIds[0], prof = service.getProfile(userId));
    assertNull("non-null name, got '" + prof.getDisplayName() + "'", prof.getDisplayName());
    assertEquals("Email mismatch;", "hans@muster.eu", prof.getEmail());
    assertNull("non-null homepage, got '" + prof.getHomePage() + "'", prof.getHomePage());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null title, got '" + prof.getTitle() + "'", prof.getTitle());
    assertNull("non-null gender, got '" + prof.getGender() + "'", prof.getGender());
    assertNull("non-null weblog, got '" + prof.getWeblog() + "'", prof.getWeblog());
    assertNull("non-null pubs, got '" + prof.getPublications() + "'", prof.getPublications());
    assertNull("non-null biography, got '" + prof.getBiography() + "'", prof.getBiography());
    assertNull("non-null interests, got '" + prof.getInterests() + "'", prof.getInterests());

    permsService.revoke(userId, new String[] { "profiles:getEmail" }, guestIds);
    permsService.grant(userId, new String[] { "profiles:getHomePage", "profiles:getDisplayName" },
                       guestIds);

    doAsUser(guestIds[0], prof = service.getProfile(userId));

    assertEquals("Name mismatch;", "Hans", prof.getDisplayName());
    assertNull("non-null email, got '" + prof.getEmail() + "'", prof.getEmail());
    assertEquals("Homepage mismatch;", "http://www.muster.eu/", prof.getHomePage());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null title, got '" + prof.getTitle() + "'", prof.getTitle());
    assertNull("non-null gender, got '" + prof.getGender() + "'", prof.getGender());
    assertNull("non-null weblog, got '" + prof.getWeblog() + "'", prof.getWeblog());
    assertNull("non-null pubs, got '" + prof.getPublications() + "'", prof.getPublications());
    assertNull("non-null biography, got '" + prof.getBiography() + "'", prof.getBiography());
    assertNull("non-null interests, got '" + prof.getInterests() + "'", prof.getInterests());

    service.setProfile(userId, null);
    doAsUser(guestIds[0], prof = service.getProfile(userId));
    assertNull("non-null profile for user '" + userId + "'", prof);
  }
  */
}
