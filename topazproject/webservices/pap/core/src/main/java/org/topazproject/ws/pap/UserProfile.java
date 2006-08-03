/*
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap;

/**
 * This defines a user's profile. It is modeled on <a href="http://xmlns.com/foaf/0.1/">foaf</a>
 * and <a href="http://vocab.org/bio/0.1/">bio</a>, and represents a subset of the
 * <var>foaf:Person</var> and <var>bio:*</var>.
 *
 * <p>Additionally, each property has a list of allowed readers associated with it. In each case
 * the list may be empty (readable by all) or a list of principals (groups and users) who are
 * allowed to see the property; if the list is empty then only the user can read the property.
 *
 * <p>This class is a bean.
 *
 * @author Ronald Tschalär
 */
public class UserProfile {
  /** the readers permission representing world readable property: "{@value}" */
  public static final String[] WORLD_READABLE = null;
  /** the readers permission representing a private property: "{@value}" */
  public static final String[] PRIVATE        = new String[0];

  /** The name to use for display; stored in topaz:displayName; subPropertyOf foaf:nick */
  private String displayName;
  /** Their real name, usually as &lt;first&gt;, &lt;last&gt;; stored in foaf:name */
  private String realName;
  /** The title by which they go; stored in foaf:title */
  private String title;
  /** 'male' or 'female'; stored in foaf:gender */
  private String gender;
  /** email address; stored in foaf:mbox */
  private String email;
  /** url of their homepage; stored in foaf:homepage */
  private String homePage;
  /** url of their blog; stored in foaf:weblog */
  private String weblog;
  /** url pointing to their biography; stored in bio:olb */
  private String biography;
  /** a list of urls pointing to stuff representing their interests; foaf:interest */
  private String[] interests;
  /** url pointing to a webpage listing their publications; foaf:publications */
  private String publications;

  /** The list of allowed readers of the displayName property */
  private String[] displayNameReaders;
  /** The list of allowed readers of the realName property */
  private String[] realNameReaders;
  /** The list of allowed readers of the title property */
  private String[] titleReaders;
  /** The list of allowed readers of the gender property */
  private String[] genderReaders;
  /** The list of allowed readers of the email property */
  private String[] emailReaders;
  /** The list of allowed readers of the homePage property */
  private String[] homePageReaders;
  /** The list of allowed readers of the weblog property */
  private String[] weblogReaders;
  /** The list of allowed readers of the biography property */
  private String[] biographyReaders;
  /** The list of allowed readers of the interests property */
  private String[] interestsReaders;
  /** The list of allowed readers of the publications property */
  private String[] publicationsReaders;

  /**
   * Get the name to use for display on the site.
   *
   * @return the display name, or null
   */
  public String getDisplayName()
  {
    return displayName;
  }

  /**
   * Set the name to use for display on the site.
   *
   * @param displayName the display name; may be null
   */
  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  /**
   * Get the real name, usually as &lt;first&gt;, &lt;last&gt;.
   *
   * @return real name, or null
   */
  public String getRealName()
  {
    return realName;
  }

  /**
   * Set the real name, usually as &lt;first&gt;, &lt;last&gt;.
   *
   * @param realName the real name; may be null
   */
  public void setRealName(String realName)
  {
    this.realName = realName;
  }

  /**
   * Get the title (e.g. 'Mrs', 'Dr', etc).
   *
   * @return the title, or null
   */
  public String getTitle()
  {
    return title;
  }

  /**
   * Set the title (e.g. 'Mrs', 'Dr', etc).
   *
   * @param title the title; may be null
   */
  public void setTitle(String title)
  {
    this.title = title;
  }

  /**
   * Get the gender. Valid values are 'male' and 'female'.
   *
   * @return the gender, or null
   */
  public String getGender()
  {
    return gender;
  }

  /**
   * Set the gender. Valid values are 'male' and 'female'.
   *
   * @param gender the gender; may be null
   */
  public void setGender(String gender)
  {
    this.gender = gender;
  }

  /**
   * Get the email address.
   *
   * @return the email, or null
   */
  public String getEmail()
  {
    return email;
  }

  /**
   * Set the email address.
   *
   * @param email the email; may be null
   */
  public void setEmail(String email)
  {
    this.email = email;
  }

  /**
   * Get the url of the user's homepage.
   *
   * @return the homepage url, or null
   */
  public String getHomePage()
  {
    return homePage;
  }

  /**
   * Set the url of the user's homepage.
   *
   * @param homePage the homepage url; may be null
   */
  public void setHomePage(String homePage)
  {
    this.homePage = homePage;
  }

  /**
   * Get the url of the user's blog.
   *
   * @return the weblog url, or null
   */
  public String getWeblog()
  {
    return weblog;
  }

  /**
   * Set the url of the user's blog.
   *
   * @param weblog the weblog url; may be null
   */
  public void setWeblog(String weblog)
  {
    this.weblog = weblog;
  }

  /**
   * Get the url of the user's biography.
   *
   * @return the biography url, or null
   */
  public String getBiography()
  {
    return biography;
  }

  /**
   * Set the url of the user's biography.
   *
   * @param biography the biography url; may be null
   */
  public void setBiography(String biography)
  {
    this.biography = biography;
  }

  /**
   * Get a list of url's, usually of webpages, representing the user's interests.
   *
   * @return the list of url's, or null. Note that the order of the entries will be arbitrary.
   */
  public String[] getInterests()
  {
      return interests;
  }

  /**
   * Set a list of url's, usually of webpages, representing the user's interests.
   *
   * @param interests the list of url's; may be null. Note that the order will not be preserved.
   */
  public void setInterests(String[] interests)
  {
      this.interests = interests;
  }

  /**
   * Get the url of the page listing the user's publications.
   *
   * @return a url, or null
   */
  public String getPublications()
  {
      return publications;
  }

  /**
   * Set the url of the page listing the user's publications.
   *
   * @param publications the url; may be null
   */
  public void setPublications(String publications)
  {
      this.publications = publications;
  }

  /**
   * Get the allowed readers of the displayName property.
   *
   * @return the list of readers; may be null
   */
  public String[] getDisplayNameReaders()
  {
      return displayNameReaders;
  }

  /**
   * Set the allowed readers of the displayName property.
   *
   * @param displayNameReaders the list of readers; may be null
   */
  public void setDisplayNameReaders(String[] displayNameReaders)
  {
      this.displayNameReaders = displayNameReaders;
  }

  /**
   * Get the allowed readers of the realName property.
   *
   * @return the list of readers; may be null
   */
  public String[] getRealNameReaders()
  {
      return realNameReaders;
  }

  /**
   * Set the allowed readers of the realName property.
   *
   * @param realNameReaders the list of readers; may be null
   */
  public void setRealNameReaders(String[] realNameReaders)
  {
      this.realNameReaders = realNameReaders;
  }

  /**
   * Get the allowed readers of the title property.
   *
   * @return the list of readers; may be null
   */
  public String[] getTitleReaders()
  {
      return titleReaders;
  }

  /**
   * Set the allowed readers of the title property.
   *
   * @param titleReaders the list of readers; may be null
   */
  public void setTitleReaders(String[] titleReaders)
  {
      this.titleReaders = titleReaders;
  }

  /**
   * Get the allowed readers of the gender property.
   *
   * @return the list of readers; may be null
   */
  public String[] getGenderReaders()
  {
      return genderReaders;
  }

  /**
   * Set the allowed readers of the gender property.
   *
   * @param genderReaders the list of readers; may be null
   */
  public void setGenderReaders(String[] genderReaders)
  {
      this.genderReaders = genderReaders;
  }

  /**
   * Get the allowed readers of the email property.
   *
   * @return the list of readers; may be null
   */
  public String[] getEmailReaders()
  {
      return emailReaders;
  }

  /**
   * Set the allowed readers of the email property.
   *
   * @param emailReaders the list of readers; may be null
   */
  public void setEmailReaders(String[] emailReaders)
  {
      this.emailReaders = emailReaders;
  }

  /**
   * Get the allowed readers of the homePage property.
   *
   * @return the list of readers; may be null
   */
  public String[] getHomePageReaders()
  {
      return homePageReaders;
  }

  /**
   * Set the allowed readers of the homePage property.
   *
   * @param homePageReaders the list of readers; may be null
   */
  public void setHomePageReaders(String[] homePageReaders)
  {
      this.homePageReaders = homePageReaders;
  }

  /**
   * Get the allowed readers of the weblog property.
   *
   * @return the list of readers; may be null
   */
  public String[] getWeblogReaders()
  {
      return weblogReaders;
  }

  /**
   * Set the allowed readers of the weblog property.
   *
   * @param weblogReaders the list of readers; may be null
   */
  public void setWeblogReaders(String[] weblogReaders)
  {
      this.weblogReaders = weblogReaders;
  }

  /**
   * Get the allowed readers of the biography property.
   *
   * @return the list of readers; may be null
   */
  public String[] getBiographyReaders()
  {
      return biographyReaders;
  }

  /**
   * Set the allowed readers of the biography property.
   *
   * @param biographyReaders the list of readers; may be null
   */
  public void setBiographyReaders(String[] biographyReaders)
  {
      this.biographyReaders = biographyReaders;
  }

  /**
   * Get the allowed readers of the interests property.
   *
   * @return the list of readers; may be null
   */
  public String[] getInterestsReaders()
  {
      return interestsReaders;
  }

  /**
   * Set the allowed readers of the interests property.
   *
   * @param interestsReaders the list of readers; may be null
   */
  public void setInterestsReaders(String[] interestsReaders)
  {
      this.interestsReaders = interestsReaders;
  }

  /**
   * Get the allowed readers of the publications property.
   *
   * @return the list of readers; may be null
   */
  public String[] getPublicationsReaders()
  {
      return publicationsReaders;
  }

  /**
   * Set the allowed readers of the publications property.
   *
   * @param publicationsReaders the list of readers; may be null
   */
  public void setPublicationsReaders(String[] publicationsReaders)
  {
      this.publicationsReaders = publicationsReaders;
  }
}
