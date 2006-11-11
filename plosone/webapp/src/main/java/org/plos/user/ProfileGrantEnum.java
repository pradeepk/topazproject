/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import static org.topazproject.ws.pap.Profiles.Permissions.FIND_USERS_BY_PROF;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_BIOGRAPHY;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_BIOGRAPHY_TEXT;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_CITY;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_COUNTRY;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_DISP_NAME;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_EMAIL;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_GENDER;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_GIVEN_NAMES;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_HOME_PAGE;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_INTERESTS;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_INTERESTS_TEXT;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_ORGANIZATION_NAME;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_ORGANIZATION_TYPE;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_POSITION_TYPE;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_POSTAL_ADDRESS;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_PUBLICATIONS;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_REAL_NAME;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_RESEARCH_AREAS_TEXT;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_SURNAMES;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_TITLE;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_WEBLOG;
import static org.topazproject.ws.pap.Profiles.Permissions.SET_PROFILE;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Enums for fieldnames and their corresponding grants to read the values.
 */
public enum ProfileGrantEnum {
  EMAIL ("email", GET_EMAIL),
  REAL_NAME ("realName", GET_REAL_NAME),
  DISPLAY_NAME ("displayName", GET_DISP_NAME),
  POSITION_TYPE ("positionType", GET_POSITION_TYPE),
  POSTAL_ADDRESS ("postalAddress", GET_POSTAL_ADDRESS),
  RESEARCH_AREAS_TEXT ("researchAreasText", GET_RESEARCH_AREAS_TEXT),
  GIVENNAMES ("givennames", GET_GIVEN_NAMES),
  SURNAMES ("surnames", GET_SURNAMES),
  TITLE ("title", GET_TITLE),
  GENDER ("gender", GET_GENDER),
  ORGANIZATION_NAME ("organizationName", GET_ORGANIZATION_NAME),
  OGANIZATION_TYPE ("oganizationType", GET_ORGANIZATION_TYPE),
  COUNTRY ("country", GET_COUNTRY),
  CITY ("city", GET_CITY),
  HOME_PAGE ("homePage", GET_HOME_PAGE),
  WEBLOG ("weblog", GET_WEBLOG),
  BIOGRAPHY ("biography", GET_BIOGRAPHY),
  INTERESTS ("interests", GET_INTERESTS),
  PUBLICATIONS ("publications", GET_PUBLICATIONS),
  BIOGRAPHY_TEXT ("biographyText", GET_BIOGRAPHY_TEXT),
  INTERESTS_TEXT ("interestsText", GET_INTERESTS_TEXT),
  PROFILE ("profile", SET_PROFILE),
  FIND_USERS_BY_PROF_FIELD ("findUsersByProf", FIND_USERS_BY_PROF);

  private final String fieldName; //name of the field
  private final String grant; //grant to read this field
  private static Collection<ProfileGrantEnum> sortedProfileGrantEnums;

  ProfileGrantEnum(final String fieldName, final String permission) {
    this.fieldName = fieldName;
    this.grant = permission;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getGrant() {
    return grant;
  }

  /**
   * Return the ProfileGrantEnum for a list of grants.
   * @param grants grants
   * @return collection of ProfileGrantEnum's
   */
  public static Collection<ProfileGrantEnum> getUserProfilePermissionsForGrants(final String[] grants) {
    final Predicate predicate = new Predicate() {
      public boolean evaluate(Object object) {
        final ProfileGrantEnum permEnum = (ProfileGrantEnum) object;
        return ArrayUtils.contains(grants, permEnum.getGrant());
      }
    };

    return selectProfileGrantEnums(grants, predicate);
  }

  /**
   * Return the ProfileGrantEnum for a list of fields.
   * @param fields fields
   * @return collection of ProfileGrantEnum's
   */
  public static Collection<ProfileGrantEnum> getUserProfilePermissionsForFields(final String[] fields) {
    final Predicate predicate = new Predicate() {
      public boolean evaluate(Object object) {
        final ProfileGrantEnum permEnum = (ProfileGrantEnum) object;
        return ArrayUtils.contains(fields, permEnum.getFieldName());
      }
    };

    return selectProfileGrantEnums(fields, predicate);
  }

  private static Collection<ProfileGrantEnum> selectProfileGrantEnums(final String[] values, final Predicate predicate) {
    if (null == sortedProfileGrantEnums) {
      sortedProfileGrantEnums = sortUserProfilePermissions();
    }

    Arrays.sort(values);

    return CollectionUtils.select(sortedProfileGrantEnums, predicate);
  }

  private static Collection<ProfileGrantEnum> sortUserProfilePermissions() {
    final List<ProfileGrantEnum> list = Arrays.asList(values());
    Collections.sort(list, new Comparator<ProfileGrantEnum>() {
      public int compare(final ProfileGrantEnum o1, final ProfileGrantEnum o2) {
        return o1.getGrant().compareTo(o2.getGrant());
      }
    });
    return list;
  }
}
