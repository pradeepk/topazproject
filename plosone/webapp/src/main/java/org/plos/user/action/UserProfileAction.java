/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.user.action;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.plos.Constants.Length;
import static org.plos.Constants.PLOS_ONE_USER_KEY;
import org.plos.user.PlosOneUser;
import org.plos.user.UserProfileGrant;

import java.util.Collection;
import java.util.Map;

/**
 * Creates a new user in Topaz and sets come Profile properties.  User must be logged in via CAS.
 * 
 * @author Stephen Cheng
 * 
 */
public class UserProfileAction extends UserActionSupport {

  private String displayName, email, realName, topazId;
  private String authId;

  private static final Log log = LogFactory.getLog(UserProfileAction.class);
  private String givennames;
  private String surnames;
  private String positionType;
  private String organizationType;
  private String organizationName;
  private String postalAddress;
  private String biographyText;
  private String interestsText;
  private String researchAreasText;
  private String city;
  private String country;
  private String[] privateFields = new String[]{""};

  /**
   * Will take the CAS ID and create a user in Topaz associated with that auth ID. If auth ID
   * already exists, it will not create another user. Email and Username are required and the
   * profile will be updated.  
   * 
   * @return status code for webwork
   */
  public String executeSaveUser() throws Exception {
    if (!validates()) return ERROR;
    final Map<String, Object> sessionMap = getSessionMap();
    authId = getUserId(sessionMap);

    topazId = getUserService().lookUpUserByAuthId(authId);
    if (topazId == null) {
      topazId = getUserService().createUser(authId);
    }
    if (log.isDebugEnabled()) {
      log.debug("Topaz ID: " + topazId + " with authID: " + authId);
    }

    final PlosOneUser newUser = createPlosOneUser();

    getUserService().setProfile(newUser, privateFields);

    sessionMap.put(PLOS_ONE_USER_KEY, newUser);
    return SUCCESS;
  }

  public String executeRetrieveUserProfile() throws Exception {
    final Map<String, Object> sessionMap = getSessionMap();
    final PlosOneUser plosOneUser = (PlosOneUser) sessionMap.get(PLOS_ONE_USER_KEY);

    authId = plosOneUser.getAuthId();
    topazId = plosOneUser.getUserId();
    email = plosOneUser.getEmail();
    displayName = plosOneUser.getDisplayName();
    realName = plosOneUser.getRealName();
    givennames = plosOneUser.getGivennames();
    surnames = plosOneUser.getSurnames();
    positionType = plosOneUser.getPositionType();
    organizationType = plosOneUser.getOrganizationType();
    organizationName = plosOneUser.getOrganizationName();
    postalAddress = plosOneUser.getPostalAddress();
    biographyText = plosOneUser.getBiographyText();
    interestsText = plosOneUser.getInterestsText();
    researchAreasText = plosOneUser.getResearchAreasText();
    city = plosOneUser.getCity();
    country = plosOneUser.getCountry();

    final Collection<UserProfileGrant> grants = getUserService().getProfileFieldsThatArePrivate(topazId);
    privateFields = new String[grants.size()];
    int i = 0;
    for (final UserProfileGrant grant : grants) {
      privateFields[i++] = grant.getFieldName();
    }

    return SUCCESS;
  }

  private PlosOneUser createPlosOneUser() {
    final PlosOneUser newUser = new PlosOneUser(this.authId);
    newUser.setUserId(this.topazId);
    newUser.setEmail(this.email);
    newUser.setDisplayName(this.displayName);
    newUser.setRealName(this.realName);
    newUser.setGivennames(this.givennames);
    newUser.setPositionType(this.positionType);
    newUser.setOrganizationType(this.organizationType);
    newUser.setPostalAddress(this.postalAddress);
    newUser.setBiographyText(this.biographyText);
    newUser.setInterestsText(this.interestsText);
    newUser.setResearchAreasText(this.researchAreasText);
    newUser.setCity(this.city);
    newUser.setCountry(this.country);
    return newUser;
  }

  public String getEmail() {
    return email;
  }

  /**
   * @param email
   *          The email to set.
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return Returns the realName.
   */
  public String getRealName() {
    return realName;
  }

  /**
   * @param realName
   *          The firstName to set.
   */
  public void setRealName(String realName) {
    this.realName = realName;
  }

  private boolean validates() {
    final int usernameLength = StringUtils.stripToEmpty(displayName).length();
    if (usernameLength < Integer.parseInt(Length.DISPLAY_NAME_MIN)
            || usernameLength > Integer.parseInt(Length.DISPLAY_NAME_MAX)) {
      addFieldError("displayName", "Username must be between " + Length.DISPLAY_NAME_MIN + " and " + Length.DISPLAY_NAME_MAX + " characters");
      return false;
    }
    return true;
  }
  /**
   * @return Returns the displayName.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName
   *          The displayName to set.
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * @return Returns the topazId.
   */
  public String getInternalId() {
    return topazId;
  }

  /**
   * @param internalId
   *          The topazId to set.
   */
  public void setInternalId(String internalId) {
    this.topazId = internalId;
  }

  /**
   * Here mainly for unit tests. Should not need to be used otherwise
   * 
   * @return Returns the authId.
   */
  protected String getAuthId() {
    return authId;
  }

  /**
   * Here mainly for unit tests. Should not need to be used otherwise. Action picks it up from
   * session automatically.
   * 
   * @param authId
   *          The authId to set.
   */
  protected void setAuthId(String authId) {
    this.authId = authId;
  }


  /**
   * Getter for property 'biographyText'.
   *
   * @return Value for property 'biographyText'.
   */
  public String getBiographyText() {
    return biographyText;
  }

  /**
   * Setter for property 'biographyText'.
   *
   * @param biographyText Value to set for property 'biographyText'.
   */
  public void setBiographyText(final String biographyText) {
    this.biographyText = biographyText;
  }

  /**
   * Getter for property 'city'.
   *
   * @return Value for property 'city'.
   */
  public String getCity() {
    return city;
  }

  /**
   * Setter for property 'city'.
   *
   * @param city Value to set for property 'city'.
   */
  public void setCity(final String city) {
    this.city = city;
  }

  /**
   * Getter for property 'country'.
   *
   * @return Value for property 'country'.
   */
  public String getCountry() {
    return country;
  }

  /**
   * Setter for property 'country'.
   *
   * @param country Value to set for property 'country'.
   */
  public void setCountry(final String country) {
    this.country = country;
  }

  /**
   * Getter for property 'givennames'.
   *
   * @return Value for property 'givennames'.
   */
  public String getGivennames() {
    return givennames;
  }

  /**
   * Setter for property 'givennames'.
   *
   * @param givennames Value to set for property 'givennames'.
   */
  public void setGivennames(final String givennames) {
    this.givennames = givennames;
  }

  /**
   * Getter for property 'interestsText'.
   *
   * @return Value for property 'interestsText'.
   */
  public String getInterestsText() {
    return interestsText;
  }

  /**
   * Setter for property 'interestsText'.
   *
   * @param interestsText Value to set for property 'interestsText'.
   */
  public void setInterestsText(final String interestsText) {
    this.interestsText = interestsText;
  }

  /**
   * Getter for property 'organizationType'.
   *
   * @return Value for property 'organizationType'.
   */
  public String getOrganizationType() {
    return organizationType;
  }

  /**
   * Setter for property 'organizationType'.
   *
   * @param organizationType Value to set for property 'organizationType'.
   */
  public void setOrganizationType(final String organizationType) {
    this.organizationType = organizationType;
  }

  /**
   * Getter for property 'positionType'.
   *
   * @return Value for property 'positionType'.
   */
  public String getPositionType() {
    return positionType;
  }

  /**
   * Setter for property 'positionType'.
   *
   * @param positionType Value to set for property 'positionType'.
   */
  public void setPositionType(final String positionType) {
    this.positionType = positionType;
  }

  /**
   * Getter for property 'postalAddress'.
   *
   * @return Value for property 'postalAddress'.
   */
  public String getPostalAddress() {
    return postalAddress;
  }

  /**
   * Setter for property 'postalAddress'.
   *
   * @param postalAddress Value to set for property 'postalAddress'.
   */
  public void setPostalAddress(final String postalAddress) {
    this.postalAddress = postalAddress;
  }

  /**
   * Getter for property 'researchAreasText'.
   *
   * @return Value for property 'researchAreasText'.
   */
  public String getResearchAreasText() {
    return researchAreasText;
  }

  /**
   * Setter for property 'researchAreasText'.
   *
   * @param researchAreasText Value to set for property 'researchAreasText'.
   */
  public void setResearchAreasText(final String researchAreasText) {
    this.researchAreasText = researchAreasText;
  }

  /**
   * Getter for property 'topazId'.
   *
   * @return Value for property 'topazId'.
   */
  public String getTopazId() {
    return topazId;
  }

  /**
   * Setter for property 'topazId'.
   *
   * @param topazId Value to set for property 'topazId'.
   */
  public void setTopazId(final String topazId) {
    this.topazId = topazId;
  }

  /**
   * Set the private fields
   * @param privateFields privateFields
   */
  public void setPrivateFields(final String[] privateFields) {
    this.privateFields = privateFields;
  }

  /**
   * Get the private fields
   * @return the private fields
   */
  public String[] getPrivateFields() {
    return privateFields;
  }

  /**
   * Getter for property 'organizationName'.
   * @return Value for property 'organizationName'.
   */
  public String getOrganizationName() {
    return organizationName;
  }

  /**
   * Setter for property 'organizationName'.
   * @param organizationName Value to set for property 'organizationName'.
   */
  public void setOrganizationName(final String organizationName) {
    this.organizationName = organizationName;
  }

  /**
   * Getter for property 'surnames'.
   *
   * @return Value for property 'surnames'.
   */
  public String getSurnames() {
    return surnames;
  }

  /**
   * Setter for property 'surnames'.
   *
   * @param surnames Value to set for property 'surnames'.
   */
  public void setSurnames(final String surnames) {
    this.surnames = surnames;
  }
}
