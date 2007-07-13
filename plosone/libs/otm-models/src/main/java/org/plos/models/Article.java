/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.net.URI;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.PredicateMap;
import org.topazproject.otm.annotations.Rdf;

/**
 * Model for PLOS articles.
 *
 * @author Eric Brown
 * @author Amit Kapoor
 */
@Entity(type = Rdf.topaz + "Article", model = "ri")
public class Article extends ObjectInfo {
  /** Article state of "Active" */
  public static final int STATE_ACTIVE   = 0;
  /** Article state of "Disabled" */
  public static final int STATE_DISABLED = 1;

  /** Active article states */
  public static final int[] ACTIVE_STATES = {STATE_ACTIVE};

  /**
   * The categories the article belongs to
   *
   * TODO: This needs to be changed.
   */
  @Predicate(uri = Rdf.topaz + "hasCategory")
  private Set<Category> categories = new HashSet<Category>();

  /**
   * The pointer to local profiles (if one exists) for the authors
   */
  // TODO: Change this to Set<User> once User model is done
  @Predicate(uri = Rdf.topaz + "userIsAuthor")
  private Set<URI> userAuthors = new HashSet<URI>();

  /**
   * Get the list of categories for the article
   *
   * @return the categories
   */
  public Set<Category> getCategories() {
    return categories;
  }

  /**
   * Set the list of categories for the article
   *
   * @param categories the categories to article belongs to
   */
  public void setCategories(Set<Category> categories) {
    this.categories = categories;
  }

  /**
   * Get the authors local profile if they are registered.
   *
   * @return the set of users that are authors of this article
   */
  public Set<URI> getUserAuthors() {
    return userAuthors;
  }

  /**
   * Set the local profile for the authors
   *
   * @param userAuthors the set of users that are also authors of this article
   */
  public void setUserAuthors(Set<URI> userAuthors) {
    this.userAuthors = userAuthors;
  }
}
