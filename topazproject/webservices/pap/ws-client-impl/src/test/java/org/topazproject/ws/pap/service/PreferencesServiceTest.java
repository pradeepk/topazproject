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
import java.util.Arrays;
import java.util.Comparator;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 * Simple tests for the preferences service.
 *
 * @author Ronald Tschalär
 */
public class PreferencesServiceTest extends TestCase {
  private Preferences service;
  private Profiles    profService;

  public PreferencesServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    URL url =
        new URL("http://localhost:9997/ws-pap-webapp-0.1-SNAPSHOT/services/PreferencesServicePort");
    PreferencesServiceLocator locator = new PreferencesServiceLocator();
    service = locator.getPreferencesServicePort(url);

    url = new URL("http://localhost:9997/ws-pap-webapp-0.1-SNAPSHOT/services/ProfilesServicePort");
    profService = new ProfilesServiceLocator().getProfilesServicePort(url);

    // ensure the user exists
    try {
      profService.createProfile("muster", null);
    } catch (DuplicateIdException die) {
      // oh well, already there
    }

    // ensure the settings are clean
    try {
      service.setPreferences(null, "muster", null);
    } catch (NoSuchIdException nsie) {
      nsie.printStackTrace();
    }
  }

  protected void tearDown() throws RemoteException {
    try {
      service.setPreferences(null, "muster", null);
    } catch (NoSuchIdException nsie) {
      // looks like it was clean
    }

    try {
      profService.deleteProfile("muster");
    } catch (NoSuchIdException nsie) {
      // looks like it was clean
    }
  }

  public void testBasicPreferences() throws RemoteException, IOException {
    UserPreference[] prefs, got, exp, exp2;

    // test NoSuchIdException
    boolean gotE = false;
    try {
      service.setPreferences("testApp1", "muster42", null);
    } catch (NoSuchIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchIdException", gotE);

    // test null prefs
    service.setPreferences("testApp1", "muster", null);

    prefs = service.getPreferences("testApp1", "muster");
    assertNull("non-null prefs, got " + prefs, prefs);

    prefs = service.getPreferences(null, "muster");
    assertNull("non-null prefs, got " + prefs, prefs);

    // test 0-length prefs
    service.setPreferences("testApp1", "muster", new UserPreference[0]);
    prefs = service.getPreferences(null, "muster");
    assertNull("non-null prefs, got " + prefs, prefs);

    // test simple prefs
    prefs = new UserPreference[3];
    prefs[0] = new UserPreference();
    prefs[1] = new UserPreference();
    prefs[2] = new UserPreference();

    prefs[0].setName("empty");
    prefs[0].setValues(new String[0]);
    prefs[1].setName("email");
    prefs[1].setValues(new String[] { "no" });
    prefs[2].setName("langs");
    prefs[2].setValues(new String[] { "english", "swahili" });

    service.setPreferences("testApp1", "muster", prefs);

    got = service.getPreferences("testApp1", "muster");

    exp = new UserPreference[2];
    exp[0] = prefs[1];
    exp[1] = prefs[2];
    compare(got, exp);

    // test app-ids
    got = service.getPreferences(null, "muster");
    compare(got, exp);

    prefs = new UserPreference[3];
    prefs[0] = new UserPreference();
    prefs[1] = new UserPreference();
    prefs[2] = new UserPreference();

    prefs[0].setName("foo");
    prefs[0].setValues(new String[] { "bar" });
    prefs[1].setName("email2");
    prefs[1].setValues(new String[] { "no" });
    prefs[2].setName("langs2");
    prefs[2].setValues(new String[] { "french", "korean" });
    service.setPreferences("testApp2", "muster", prefs);

    got = service.getPreferences("testApp1", "muster");
    compare(got, exp);

    got = service.getPreferences("testApp2", "muster");
    compare(got, prefs);

    got = service.getPreferences(null, "muster");
    exp2 = new UserPreference[5];
    exp2[0] = prefs[0];
    exp2[1] = prefs[1];
    exp2[2] = prefs[2];
    exp2[3] = exp[0];
    exp2[4] = exp[1];
    compare(got, exp2);

    service.setPreferences("testApp1", "muster", null);

    got = service.getPreferences("testApp2", "muster");
    compare(got, prefs);

    got = service.getPreferences(null, "muster");
    compare(got, prefs);
  }

  private static void sort(UserPreference[] prefs) {
    // first sort by name
    Arrays.sort(prefs, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((UserPreference) o1).getName().compareTo(((UserPreference) o2).getName());
      }
    });

    // now sort values
    for (int idx = 0; idx < prefs.length; idx++)
      Arrays.sort(prefs[idx].getValues());
  }

  private static void compare(UserPreference[] got, UserPreference[] exp) {
    assertNotNull("got null prefs", got);
    assertEquals("wrong nubmer of prefs,", got.length,  exp.length);

    sort(got);
    sort(exp);

    for (int idx = 0; idx < got.length; idx++) {
      assertEquals("Pref name mismatch", got[idx].getName(), exp[idx].getName());
      assertNotNull("Pref null values", got[idx].getValues());
      assertTrue("values mismatch for " + got[idx].getName(),
                 Arrays.equals(got[idx].getValues(), exp[idx].getValues()));
    }
  }
}
