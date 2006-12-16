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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import static org.plos.Constants.Length;
import static org.plos.Constants.PLOS_ONE_USER_KEY;
import static org.plos.Constants.ReturnCode.NEW_PROFILE;
import static org.plos.Constants.ReturnCode.UPDATE_PROFILE;
import org.plos.user.PlosOneUser;
import org.plos.user.UserProfileGrant;
import org.plos.user.service.DisplayNameAlreadyExistsException;
import org.plos.util.ProfanityCheckingService;
import org.plos.util.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
  private String givenNames;
  private String surnames;
  private String positionType;
  private String organizationType;
  private String organizationName;
  private String postalAddress;
  private String biographyText;
  private String interestsText;
  private String researchAreasText;
  private String homePage;
  private String weblog;
  private String city;
  private String country;
  private String title;
  private String nameVisibility = PUBLIC;
  private String extendedVisibility;
  private String orgVisibility;
  private ProfanityCheckingService profanityCheckingService;

  public static final String PRIVATE = "private";
  public static final String PUBLIC = "public";
  private static final Map<String, String[]> visibilityMapping = new HashMap<String, String[]>();

  private static String GIVEN_NAMES = UserProfileGrant.GIVENNAMES.getFieldName();
  private static final String POSTAL_ADDRESS = UserProfileGrant.POSTAL_ADDRESS.getFieldName();
  private static final String ORGANIZATION_TYPE = UserProfileGrant.ORGANIZATION_TYPE.getFieldName();

  private static final String NAME_GROUP = "name";
  private static final String EXTENDED_GROUP = "extended";
  private static final String ORG_GROUP = "org";
  private final String HTTP_PREFIX = "http://";
  private static final Pattern spacePattern = Pattern.compile("\\s");
  boolean isDisplayNameSet = true;

  static {
    visibilityMapping.put(NAME_GROUP,
                          new String[]{
                                  GIVEN_NAMES,
                                  UserProfileGrant.DISPLAY_NAME.getFieldName(),
                                  UserProfileGrant.SURNAMES.getFieldName(),
                                  UserProfileGrant.CITY.getFieldName(),
                                  UserProfileGrant.COUNTRY.getFieldName()});
    visibilityMapping.put(EXTENDED_GROUP,
                          new String[]{
                                  POSTAL_ADDRESS});
    visibilityMapping.put(ORG_GROUP,
                          new String[]{
                                  ORGANIZATION_TYPE,
                                  UserProfileGrant.ORGANIZATION_NAME.getFieldName(),
                                  UserProfileGrant.TITLE.getFieldName(),
                                  UserProfileGrant.POSITION_TYPE.getFieldName()});
  }

  /**
   * Will take the CAS ID and create a user in Topaz associated with that auth ID. If auth ID
   * already exists, it will not create another user. Email and Username are required and the
   * profile will be updated.  
   * 
   * @return status code for webwork
   */
  public String executeSaveUser() throws Exception {
    if (!validates()) {
      email = fetchUserEmailAddress();
      if (null == getPlosOneUserFromSession()) {
        isDisplayNameSet = false;
      }
      return INPUT;
    }
    final Map<String, Object> sessionMap = getSessionMap();
    authId = getUserId(sessionMap);

    topazId = getUserService().lookUpUserByAuthId(authId);
    if (topazId == null) {
      isDisplayNameSet = false;
      topazId = getUserService().createUser(authId);
    }
    if (log.isDebugEnabled()) {
      log.debug("Topaz ID: " + topazId + " with authID: " + authId);
    }

    homePage = makeValidUrl(homePage);
    weblog = makeValidUrl(weblog);
    
    final PlosOneUser newUser = createPlosOneUser();
    if (!isDisplayNameSet) {
      //if new user then capture the displayname
      newUser.setDisplayName(this.displayName);
    }

    try {
      //If a field is not among the private fields, it is saved as public
      getUserService().setProfile(newUser, getPrivateFields());
    } catch (DisplayNameAlreadyExistsException ex) {
      email = fetchUserEmailAddress();
      addFieldError("displayName", "Username is already in use. Please select a different username");
      return INPUT;
    }

    sessionMap.put(PLOS_ONE_USER_KEY, newUser);
    return SUCCESS;
  }

  private String makeValidUrl(final String url) throws Exception {
    if (StringUtils.isEmpty(url) || url.equalsIgnoreCase(HTTP_PREFIX)) {
      return StringUtils.EMPTY;
    }
    return TextUtils.makeValidUrl(url);
  }

  public String executeRetrieveUserProfile() throws Exception {
    final PlosOneUser plosOneUser = getPlosOneUserFromSession();

    authId = plosOneUser.getAuthId();
    topazId = plosOneUser.getUserId();
    email = plosOneUser.getEmail();
    displayName = plosOneUser.getDisplayName();
    realName = plosOneUser.getRealName();
    givenNames = plosOneUser.getGivenNames();
    surnames = plosOneUser.getSurnames();
    title = plosOneUser.getTitle();
    positionType = plosOneUser.getPositionType();
    organizationType = plosOneUser.getOrganizationType();
    organizationName = plosOneUser.getOrganizationName();
    postalAddress = plosOneUser.getPostalAddress();
    biographyText = plosOneUser.getBiographyText();
    interestsText = plosOneUser.getInterestsText();
    researchAreasText = plosOneUser.getResearchAreasText();
    homePage = plosOneUser.getHomePage();
    weblog = plosOneUser.getWeblog();
    city = plosOneUser.getCity();
    country = plosOneUser.getCountry();

    final Collection<UserProfileGrant> grants = getUserService().getProfileFieldsThatArePrivate(topazId);
    setVisibility(grants);
    
    return SUCCESS;
  }

  /**
   * Prepopulate the user profile data as available
   * @return return code for webwork
   * @throws Exception Exception
   */
  public String prePopulateUserDetails() throws Exception {
    final Map<String, Object> sessionMap = getSessionMap();
    final PlosOneUser plosOneUser = (PlosOneUser) sessionMap.get(PLOS_ONE_USER_KEY);

    isDisplayNameSet = false;
    //If the user has no topaz id
    if (null == plosOneUser) {
      email = fetchUserEmailAddress();
      log.debug("new profile with email: " + email);
      return NEW_PROFILE;
    } else if (null == plosOneUser.getDisplayName()) {
      // if the user has no display name, possibly getting migrated from an old system
      try {
        log.debug("this is an existing user with email: " + plosOneUser.getEmail());
        email = plosOneUser.getEmail();
        realName = plosOneUser.getRealName();
      } catch(NullPointerException  ex) {
        log.debug("Profile was not found");
      }
      return UPDATE_PROFILE;
    }
    //else just forward the user to a success page, which might be the home page.
    isDisplayNameSet = true;
    return SUCCESS;
  }

  private PlosOneUser createPlosOneUser() throws ApplicationException {
    PlosOneUser plosOneUser = getPlosOneUserFromSession();
    if (plosOneUser == null) {
      plosOneUser = new PlosOneUser(this.authId);
      plosOneUser.setEmail(fetchUserEmailAddress());
    }

    plosOneUser.setUserId(this.topazId);
    plosOneUser.setRealName(this.realName);
    plosOneUser.setTitle(this.title);
    plosOneUser.setSurnames(this.surnames);
    plosOneUser.setGivenNames(this.givenNames);
    plosOneUser.setPositionType(this.positionType);
    plosOneUser.setOrganizationType(this.organizationType);
    plosOneUser.setOrganizationName(this.organizationName);
    plosOneUser.setPostalAddress(this.postalAddress);
    plosOneUser.setBiographyText(this.biographyText);
    plosOneUser.setInterestsText(this.interestsText);
    plosOneUser.setResearchAreasText(this.researchAreasText);
    plosOneUser.setHomePage(StringUtils.stripToNull(this.homePage));
    plosOneUser.setWeblog(StringUtils.stripToNull(this.weblog));
    plosOneUser.setCity(this.city);
    plosOneUser.setCountry(this.country);
    return plosOneUser;
  }

  private PlosOneUser getPlosOneUserFromSession() {
    final Map<String, Object> sessionMap = getSessionMap();
    return (PlosOneUser) sessionMap.get(PLOS_ONE_USER_KEY);
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
    boolean isValid = true;
    final int usernameLength = StringUtils.stripToEmpty(displayName).length();

    //Don't validate displayName if the user already has a display name
    if (null == getPlosOneUserFromSession()) {
      if (!spacePattern.matcher(displayName).find()) {
        if (usernameLength < Integer.parseInt(Length.DISPLAY_NAME_MIN)
              || usernameLength > Integer.parseInt(Length.DISPLAY_NAME_MAX)) {
          addFieldError("displayName", "Username must be between " + Length.DISPLAY_NAME_MIN + " and " + Length.DISPLAY_NAME_MAX + " characters");
          isValid = false;
        }
      } else {
        addFieldError("displayName", "Username may not contain spaces");
        isValid = false;
      }
    }
    if (StringUtils.isBlank(givenNames)) {
      addFieldError("givenNames", "First/Given Name cannot be empty");
      isValid = false;
    }
    if (StringUtils.isBlank(surnames)) {
      addFieldError("surnames", "Last/Family Name cannot be empty");
      isValid = false;
    }
    //TODO: does everyone live in a city?
    if (StringUtils.isBlank(city)) {
      addFieldError("city", "City cannot be empty");
      isValid = false;
    }
    if (StringUtils.isBlank(country)) {
      addFieldError("country", "Country cannot be empty");
      isValid = false;
    }
    if (isInvalidUrl(homePage)) {
      addFieldError("homePage", "Home page URL is not valid");
      isValid = false;
    }
    if (isInvalidUrl(weblog)) {
      addFieldError("weblog", "Weblog URL is not valid");
      isValid = false;
    }

    if (profanityCheckFailed()) {
      isValid = false;
    }

    //Displayname may be null if the user is just editing the profile and so can't change it 
    if (null != displayName && maliciousContentFound()) {
      isValid = false;
    }

    return isValid;
  }

  private boolean maliciousContentFound() {
    if (TextUtils.isPotentiallyMalicious(displayName)) {
      addFieldError("displayName", "Invalid characters found in field");
      return true;
    }
    return false;
  }

  private boolean isInvalidUrl(final String url) {
    if (StringUtils.isEmpty(url)) {
      return false;
    }
    return !TextUtils.verifyUrl(url) && !TextUtils.verifyUrl(HTTP_PREFIX + url);
  }

  private boolean profanityCheckFailed() {
    boolean isProfane = false;
    final String[][] fieldsToValidate = getFieldMappings();

    for (final String[] field : fieldsToValidate) {
      if (isProfane(field[0], field[1])) {
        isProfane = true;
      }
    }

    return isProfane;
  }

  private String[][] getFieldMappings() {
    final String[][] toValidateArray = new String[][]{
            {"displayName", displayName},
            {"realName", realName},
            {"title", title},
            {"surnames", surnames},
            {"givenNames", givenNames},
            {"positionType", positionType},
            {"organizationName", organizationName},
            {"postalAddress", postalAddress},
            {"biographyText", biographyText},
            {"interestsText", interestsText},
            {"researchAreasText", researchAreasText},
            {"homePage", homePage},
            {"weblog", weblog},
            {"city", city},
            {"country", country},
    };
    return toValidateArray;
  }

  private boolean isProfane(final String fieldName, final String fieldValue) {
    final List<String> profanityValidationMessages = profanityCheckingService.validate(fieldValue);
    if (profanityValidationMessages.size() > 0 ) {
      addFieldError(fieldName, StringUtils.join(profanityValidationMessages.toArray(), ", "));
      return true;
    }
    return false;
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
  public String getGivenNames() {
    return givenNames;
  }

  /**
   * Setter for property 'givenNames'.
   *
   * @param givenNames Value to set for property 'givenNames'.
   */
  public void setGivenNames(final String givenNames) {
    this.givenNames = givenNames;
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

  private void setVisibility(final Collection<UserProfileGrant> grants) {
    final String[] privateFields = new String[grants.size()];
    int i = 0;
    for (final UserProfileGrant grant : grants) {
      privateFields[i++] = grant.getFieldName();
    }

    nameVisibility = setFieldVisibility(privateFields, GIVEN_NAMES);
    extendedVisibility = setFieldVisibility(privateFields, POSTAL_ADDRESS);
    orgVisibility = setFieldVisibility(privateFields, ORGANIZATION_TYPE);
  }

  private String setFieldVisibility(final String[] privateFields, final String fieldName) {
    return ArrayUtils.contains(privateFields, fieldName) ? PRIVATE : PUBLIC;
  }

  /**
   * Get the private fields
   * @return the private fields
   */
  private String[] getPrivateFields() {
    final Collection<String> privateFieldsList = new ArrayList<String>();
    CollectionUtils.addAll(privateFieldsList, getRespectiveFields(nameVisibility, NAME_GROUP));
    CollectionUtils.addAll(privateFieldsList, getRespectiveFields(extendedVisibility, EXTENDED_GROUP));
    CollectionUtils.addAll(privateFieldsList, getRespectiveFields(orgVisibility, ORG_GROUP));

    //Add email as a private field.
    CollectionUtils.addAll(privateFieldsList, new String[]{UserProfileGrant.EMAIL.getFieldName()});

    return privateFieldsList.toArray(new String[privateFieldsList.size()]);
  }

  private String[] getRespectiveFields(final String fieldVisibilityToCheck, final String key) {
    if(StringUtils.stripToEmpty(fieldVisibilityToCheck).equalsIgnoreCase(PRIVATE)) {
      return visibilityMapping.get(key);
    }
    return ArrayUtils.EMPTY_STRING_ARRAY;
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
   * @return Value for property 'surnames'.
   */
  public String getSurnames() {
    return surnames;
  }

  /**
   * Setter for property 'surnames'.
   * @param surnames Value to set for property 'surnames'.
   */
  public void setSurnames(final String surnames) {
    this.surnames = surnames;
  }

  /**
   * Getter for title.
   * @return Value of title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Setter for title.
   * @param title Value to set for title.
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * Getter for extendedVisibility.
   * @return Value of extendedVisibility.
   */
  public String getExtendedVisibility() {
    return getVisibility(extendedVisibility);
  }

  /**
   * Setter for extendedVisibility.
   * @param extendedVisibility Value to set for extendedVisibility.
   */
  public void setExtendedVisibility(final String extendedVisibility) {
    this.extendedVisibility = extendedVisibility;
  }

  /**
   * Getter for nameVisibility.
   * @return Value of nameVisibility.
   */
  public String getNameVisibility() {
    return getVisibility(nameVisibility);
  }

  /**
   * Setter for nameVisibility.
   * @param nameVisibility Value to set for nameVisibility.
   */
  private void setNameVisibility(final String nameVisibility) {
    this.nameVisibility = nameVisibility;
  }

  /**
   * Getter for orgVisibility.
   * @return Value of orgVisibility.
   */
  public String getOrgVisibility() {
    return getVisibility(orgVisibility);
  }

  /**
   * Setter for orgVisibility.
   * @param orgVisibility Value to set for orgVisibility.
   */
  public void setOrgVisibility(final String orgVisibility) {
    this.orgVisibility = orgVisibility;
  }

  private String getVisibility(final String visibility) {
    return StringUtils.isBlank(visibility) ? PUBLIC : visibility;
  }

  /**
   * Getter for homePage.
   * @return Value of homePage.
   */
  public String getHomePage() {
    return homePage;
  }

  /**
   * Setter for homePage.
   * @param homePage Value to set for homePage.
   */
  public void setHomePage(final String homePage) {
    this.homePage = homePage;
  }

  /**
   * Getter for weblog.
   * @return Value of weblog.
   */
  public String getWeblog() {
    return weblog;
  }

  /**
   * Setter for weblog.
   * @param weblog Value to set for weblog.
   */
  public void setWeblog(final String weblog) {
    this.weblog = weblog;
  }

  /**
   * Getter for profanityCheckingService.
   * @return Value of profanityCheckingService.
   */
  public ProfanityCheckingService getProfanityCheckingService() {
    return profanityCheckingService;
  }

  /**
   * Setter for profanityCheckingService.
   * @param profanityCheckingService Value to set for profanityCheckingService.
   */
  public void setProfanityCheckingService(final ProfanityCheckingService profanityCheckingService) {
    this.profanityCheckingService = profanityCheckingService;
  }

  /**
   * Getter for isDisplayNameSet.
   * @return Value of isDisplayNameSet.
   */
  public boolean getIsDisplayNameSet() {
    return isDisplayNameSet;
  }
}
