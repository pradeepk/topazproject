/*
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

/**
 * Simple tests for the profiles service.
 *
 * @author Ronald Tschalär
 */
public class ProfilesServiceTest extends TestCase {
  private Profiles service;

  public ProfilesServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    URL url =
        new URL("http://localhost:9997/ws-pap-webapp-0.1-SNAPSHOT/services/ProfilesServicePort");
    ProfilesServiceLocator locator = new ProfilesServiceLocator();
    service = locator.getProfilesServicePort(url);

    /*
    // ensure stuff is clean
    try {
      service.deleteProfile("muster");
    } catch (NoSuchIdException nsie) {
      // looks like it was clean
    }
    */
  }

  protected void tearDown() throws RemoteException {
    /*
    try {
      service.deleteProfile("muster");
    } catch (NoSuchIdException nsie) {
      // looks like it was clean
    }
    */
  }

  public void testBasicProfiles() throws RemoteException, IOException {
    /*
    boolean gotE = false;
    try {
      service.deleteProfile("muster");
    } catch (NoSuchIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchIdException", gotE);

    service.createProfile("muster", null);

    gotE = false;
    try {
      service.createProfile("muster", null);
    } catch (DuplicateIdException die) {
      gotE = true;
    }
    assertTrue("Failed to get expected DuplicateIdException", gotE);

    service.deleteProfile("muster");

    UserProfile prof = new UserProfile();
    prof.setDisplayName("Hans");
    prof.setEmail("hans@muster.eu");
    prof.setHomePage("http://www.muster.eu/");
    service.createProfile("muster", prof);

    prof = service.getProfile("muster");
    assertEquals("Name mismatch", prof.getDisplayName(), "Hans");
    assertEquals("Email mismatch", prof.getEmail(), "hans@muster.eu");
    assertEquals("Homepage mismatch", prof.getHomePage(), "http://www.muster.eu/");
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
    service.updateProfile("muster", prof);

    prof = service.getProfile("muster");
    assertEquals("Name mismatch;", prof.getDisplayName(), "Hans");
    assertEquals("Email mismatch;", prof.getEmail(), "hans.muster@sample.com");
    assertEquals("Weblog mismatch;", prof.getWeblog(), "http://muster.blogs.org");
    assertNull("non-null homepage, got '" + prof.getHomePage() + "'", prof.getHomePage());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null title, got '" + prof.getTitle() + "'", prof.getTitle());
    assertNull("non-null gender, got '" + prof.getGender() + "'", prof.getGender());
    assertNull("non-null pubs, got '" + prof.getPublications() + "'", prof.getPublications());
    assertNull("non-null biography, got '" + prof.getBiography() + "'", prof.getBiography());
    String[] i = prof.getInterests();
    assertEquals("Interests mismatch;", i.length, 2);
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
    service.updateProfile("muster", prof);

    prof = service.getProfile("muster");
    assertEquals("Name mismatch;", prof.getDisplayName(), "Hans");
    assertEquals("Email mismatch;", prof.getEmail(), "hans.muster@sample.com");
    assertEquals("Weblog mismatch;", prof.getWeblog(), "http://muster.blogs.org");
    assertEquals("Publications mismatch;", prof.getPublications(), "http://pubs/");
    assertEquals("Real-name mismatch;", prof.getRealName(), "rn");
    assertEquals("Title mismatch;", prof.getTitle(), "mr");
    assertEquals("Gender mismatch;", prof.getGender(), "male");
    assertEquals("Biography mismatch;", prof.getBiography(), "http://bio/");
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

    service.deleteProfile("muster");
    */
  }

  public void testProfilesPermissions() throws RemoteException, IOException {
    /*
    UserProfile prof = new UserProfile();
    prof.setDisplayName("Hans");
    prof.setEmail("hans@muster.eu");
    prof.setHomePage("http://www.muster.eu/");
    prof.setEmailReaders(new String[0]);
    service.createProfile("muster", prof);

    prof = service.getProfile("muster");
    assertEquals("Name mismatch;", prof.getDisplayName(), "Hans");
    assertEquals("Email mismatch;", prof.getEmail(), "hans@muster.eu");
    assertEquals("Homepage mismatch;", prof.getHomePage(), "http://www.muster.eu/");
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
    assertEquals("Email-readers mismatch;", r.length, 0);

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
    service.updateProfile("muster", prof);

    prof = service.getProfile("muster");
    assertEquals("Name mismatch;", prof.getDisplayName(), "Hans");
    assertEquals("Email mismatch;", prof.getEmail(), "hans@muster.eu");
    assertEquals("Homepage mismatch;", prof.getHomePage(), "http://www.muster.eu/");
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
    assertEquals("Number of Email-readers wrong;", r.length, 1);

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
    service.updateProfile("muster", prof);

    prof = service.getProfile("muster");
    assertEquals("Name mismatch;", prof.getDisplayName(), "Hans");
    assertEquals("Email mismatch;", prof.getEmail(), "hans@muster.eu");
    assertEquals("Homepage mismatch;", prof.getHomePage(), "http://www.muster.eu/");
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
    assertEquals("Number of Email-readers wrong", r.length, 2);

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

    service.deleteProfile("muster");
    */
  }
}
