/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

import org.topazproject.ws.users.service.UserAccounts;
import org.topazproject.ws.users.service.UserAccountsServiceLocator;

/**
 * Simple tests for the profiles service.
 *
 * @author Ronald Tschalär
 */
public class ProfilesServiceTest extends TestCase {
  private Profiles     service;
  private UserAccounts userService;
  private String       userId;

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

    // create a user
    userId = userService.createUser("musterAuth");
  }

  protected void tearDown() throws RemoteException {
    try {
      service.setProfile(userId, null);
    } catch (NoSuchIdException nsie) {
      // looks like it was clean
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

    assertNull("non-null display-name readers, got '" + prof.getDisplayNameReaders() + "'",
               prof.getDisplayNameReaders());
    assertNull("non-null real-name readers, got '" + prof.getRealNameReaders() + "'",
               prof.getRealNameReaders());
    assertNull("non-null email readers, got '" + prof.getEmailReaders() + "'",
               prof.getEmailReaders());
    assertNull("non-null title readers, got '" + prof.getTitleReaders() + "'",
               prof.getTitleReaders());
    assertNull("non-null gender readers, got '" + prof.getGenderReaders() + "'",
               prof.getGenderReaders());
    assertNull("non-null weblog readers, got '" + prof.getWeblogReaders() + "'",
               prof.getWeblogReaders());
    assertNull("non-null homepage readers, got '" + prof.getHomePageReaders() + "'",
               prof.getHomePageReaders());
    assertNull("non-null pubs readers, got '" + prof.getPublicationsReaders() + "'",
               prof.getPublicationsReaders());
    assertNull("non-null biography readers, got '" + prof.getBiographyReaders() + "'",
               prof.getBiographyReaders());
    assertNull("non-null interests readers, got '" + prof.getInterestsReaders() + "'",
               prof.getInterestsReaders());

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

    assertNull("non-null display-name readers, got '" + prof.getDisplayNameReaders() + "'",
               prof.getDisplayNameReaders());
    assertNull("non-null real-name readers, got '" + prof.getRealNameReaders() + "'",
               prof.getRealNameReaders());
    assertNull("non-null email readers, got '" + prof.getEmailReaders() + "'",
               prof.getEmailReaders());
    assertNull("non-null title readers, got '" + prof.getTitleReaders() + "'",
               prof.getTitleReaders());
    assertNull("non-null gender readers, got '" + prof.getGenderReaders() + "'",
               prof.getGenderReaders());
    assertNull("non-null weblog readers, got '" + prof.getWeblogReaders() + "'",
               prof.getWeblogReaders());
    assertNull("non-null homepage readers, got '" + prof.getHomePageReaders() + "'",
               prof.getHomePageReaders());
    assertNull("non-null pubs readers, got '" + prof.getPublicationsReaders() + "'",
               prof.getPublicationsReaders());
    assertNull("non-null biography readers, got '" + prof.getBiographyReaders() + "'",
               prof.getBiographyReaders());
    assertNull("non-null interests readers, got '" + prof.getInterestsReaders() + "'",
               prof.getInterestsReaders());

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

    assertNull("non-null display-name readers, got '" + prof.getDisplayNameReaders() + "'",
               prof.getDisplayNameReaders());
    assertNull("non-null real-name readers, got '" + prof.getRealNameReaders() + "'",
               prof.getRealNameReaders());
    assertNull("non-null email readers, got '" + prof.getEmailReaders() + "'",
               prof.getEmailReaders());
    assertNull("non-null title readers, got '" + prof.getTitleReaders() + "'",
               prof.getTitleReaders());
    assertNull("non-null gender readers, got '" + prof.getGenderReaders() + "'",
               prof.getGenderReaders());
    assertNull("non-null weblog readers, got '" + prof.getWeblogReaders() + "'",
               prof.getWeblogReaders());
    assertNull("non-null homepage readers, got '" + prof.getHomePageReaders() + "'",
               prof.getHomePageReaders());
    assertNull("non-null pubs readers, got '" + prof.getPublicationsReaders() + "'",
               prof.getPublicationsReaders());
    assertNull("non-null biography readers, got '" + prof.getBiographyReaders() + "'",
               prof.getBiographyReaders());
    assertNull("non-null interests readers, got '" + prof.getInterestsReaders() + "'",
               prof.getInterestsReaders());

    service.setProfile(userId, null);
    prof = service.getProfile(userId);
    assertNull("non-null profile for user '" + userId + "'", prof);
  }

  public void testProfilesPermissions() throws RemoteException, IOException {
    UserProfile prof = new UserProfile();
    prof.setDisplayName("Hans");
    prof.setEmail("hans@muster.eu");
    prof.setHomePage("http://www.muster.eu/");
    prof.setEmailReaders(new String[0]);
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

    assertNull("non-null display-name readers, got '" + prof.getDisplayNameReaders() + "'",
               prof.getDisplayNameReaders());
    assertNull("non-null real-name readers, got '" + prof.getRealNameReaders() + "'",
               prof.getRealNameReaders());

    String[] r = prof.getEmailReaders();
    assertNotNull("Email-readers mismatch, got null instead of array", r);
    assertEquals("Email-readers mismatch;", 0, r.length);

    assertNull("non-null title readers, got '" + prof.getTitleReaders() + "'",
               prof.getTitleReaders());
    assertNull("non-null gender readers, got '" + prof.getGenderReaders() + "'",
               prof.getGenderReaders());
    assertNull("non-null weblog readers, got '" + prof.getWeblogReaders() + "'",
               prof.getWeblogReaders());
    assertNull("non-null homepage readers, got '" + prof.getHomePageReaders() + "'",
               prof.getHomePageReaders());
    assertNull("non-null pubs readers, got '" + prof.getPublicationsReaders() + "'",
               prof.getPublicationsReaders());
    assertNull("non-null biography readers, got '" + prof.getBiographyReaders() + "'",
               prof.getBiographyReaders());
    assertNull("non-null interests readers, got '" + prof.getInterestsReaders() + "'",
               prof.getInterestsReaders());

    prof.setEmailReaders(new String[] { "id:joe" });
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

    assertNull("non-null display-name readers, got '" + prof.getDisplayNameReaders() + "'",
               prof.getDisplayNameReaders());
    assertNull("non-null real-name readers, got '" + prof.getRealNameReaders() + "'",
               prof.getRealNameReaders());

    r = prof.getEmailReaders();
    assertNotNull("Email-readers mismatch, got null instead of array", r);
    assertEquals("Number of Email-readers wrong;", 1, r.length);

    assertNull("non-null title readers, got '" + prof.getTitleReaders() + "'",
               prof.getTitleReaders());
    assertNull("non-null gender readers, got '" + prof.getGenderReaders() + "'",
               prof.getGenderReaders());
    assertNull("non-null weblog readers, got '" + prof.getWeblogReaders() + "'",
               prof.getWeblogReaders());
    assertNull("non-null homepage readers, got '" + prof.getHomePageReaders() + "'",
               prof.getHomePageReaders());
    assertNull("non-null pubs readers, got '" + prof.getPublicationsReaders() + "'",
               prof.getPublicationsReaders());
    assertNull("non-null biography readers, got '" + prof.getBiographyReaders() + "'",
               prof.getBiographyReaders());
    assertNull("non-null interests readers, got '" + prof.getInterestsReaders() + "'",
               prof.getInterestsReaders());

    prof.setEmailReaders(new String[] { "id:joe", "id:bob" });
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

    assertNull("non-null display-name readers, got '" + prof.getDisplayNameReaders() + "'",
               prof.getDisplayNameReaders());
    assertNull("non-null real-name readers, got '" + prof.getRealNameReaders() + "'",
               prof.getRealNameReaders());

    r = prof.getEmailReaders();
    assertNotNull("Email-readers mismatch, got null instead of array", r);
    assertEquals("Number of Email-readers wrong", 2, r.length);

    assertNull("non-null title readers, got '" + prof.getTitleReaders() + "'",
               prof.getTitleReaders());
    assertNull("non-null gender readers, got '" + prof.getGenderReaders() + "'",
               prof.getGenderReaders());
    assertNull("non-null weblog readers, got '" + prof.getWeblogReaders() + "'",
               prof.getWeblogReaders());
    assertNull("non-null homepage readers, got '" + prof.getHomePageReaders() + "'",
               prof.getHomePageReaders());
    assertNull("non-null pubs readers, got '" + prof.getPublicationsReaders() + "'",
               prof.getPublicationsReaders());
    assertNull("non-null biography readers, got '" + prof.getBiographyReaders() + "'",
               prof.getBiographyReaders());
    assertNull("non-null interests readers, got '" + prof.getInterestsReaders() + "'",
               prof.getInterestsReaders());

    service.setProfile(userId, null);
    prof = service.getProfile(userId);
    assertNull("non-null profile for user '" + userId + "'", prof);
  }
}
