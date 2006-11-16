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

import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.plos.Constants.Length;
import static org.plos.Constants.PLOS_ONE_USER_KEY;
import org.plos.user.PlosOneUser;

import java.util.Map;

/**
 * Creates a new user in Topaz and sets come Profile properties.  User must be logged in via CAS.
 * 
 * @author Stephen Cheng
 * 
 */
public class CreateUserAction extends UserActionSupport {

  private String username, email, realName, topazId;

  private String authId;

  private static final Log log = LogFactory.getLog(CreateUserAction.class);
  private String givennames;
  private String positionType;
  private String organizationType;
  private String postalAddress;
  private String biographyText;
  private String interestsText;
  private String researchAreasText;
  private String city;
  private String country;
  private String[] privateFields;

  /**
   * Will take the CAS ID and create a user in Topaz associated with that auth ID. If auth ID
   * already exists, it will not create another user. Email and Username are required and the
   * profile will be updated.  
   * 
   * @return status code for webwork
   */
  public String execute() throws Exception {
    final Map<String, Object> sessionMap = getSessionMap();
    authId = getUserId(sessionMap);

    topazId = getUserService().lookUpUserByAuthId(authId);
    if (topazId == null) {
      topazId = getUserService().createUser(authId, privateFields);
    }
    if (log.isDebugEnabled()) {
      log.debug("Topaz ID: " + topazId + " with authID: " + authId);
    }

    final PlosOneUser newUser = createPlosOneUser();

    getUserService().setProfile(newUser);

    sessionMap.put(PLOS_ONE_USER_KEY, newUser);
    return SUCCESS;
  }

  private PlosOneUser createPlosOneUser() {
    final PlosOneUser newUser = new PlosOneUser(this.authId);
    newUser.setUserId(this.topazId);
    newUser.setEmail(this.email);
    newUser.setDisplayName(this.username);
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

  /**
   * Email is required and length must be less than 256 characters.
   * 
   * @return Returns the email.
   */
  @EmailValidator(type = ValidatorType.SIMPLE, fieldName = "email", 
                  message = "You must enter a valid email", shortCircuit = true)
  @RequiredStringValidator(message = "You must enter an email address", shortCircuit = true)
  @StringLengthFieldValidator(maxLength = Length.EMAIL,
                              message = "Email must be less than " + Length.EMAIL, shortCircuit = true)
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

  /**
   * @return Returns the username.
   */
  @RequiredStringValidator(message = "You must enter a username", shortCircuit = true)
  @StringLengthFieldValidator(fieldName = "username",
                              minLength = Length.DISPLAY_NAME_MIN,
                              maxLength = Length.DISPLAY_NAME_MAX,
                              message = "Username must be between " + Length.DISPLAY_NAME_MIN + " and " + Length.DISPLAY_NAME_MAX + " characters",
                              shortCircuit = true)
  public String getUsername() {
    return username;
  }

  /**
   * @param username
   *          The username to set.
   */
  public void setUsername(String username) {
    this.username = username;
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
}
