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

  @Predicate(uri = Rdf.dc_terms + "hasPart")
  private Set<ObjectInfo> parts = new HashSet<ObjectInfo>();
  // This will be used to indicate the PLoS type of article
  @Predicate(uri = Rdf.rdf + "type", dataType = Rdf.xsd + "anyURI")
  private String articleType;

  /**
   * The categories the article belongs to
   *
   * TODO: This needs to be changed.
   */
  @Predicate(uri = Rdf.topaz + "hasCategory")
  private Set<Category> categories = new HashSet<Category>();

  /** 
   * @return the different parts of the article 
   */
  public Set<ObjectInfo> getParts() {
    return parts;
  }

  /** 
   * @param parts the different parts of the article 
   */
  public void setParts(Set<ObjectInfo> parts) {
    this.parts = parts;
  }

  /**
   * Get the list of categories for the article
   *
   * @return the categories
   */
  public Set<Category> getCategories() {
    return categories;
  }

  /**
   * Set the article type. PLoS uses the same underlying schema (NLM DTD) for
   * the content for different 'types' of content. For example eLetter,
   * Correspondence, Editorial comment, etc. This articleType is used to stored
   * the value of the type. Please note that the string passed should be a
   * valid URI.
   *
   * @param articleType the string representation of the URI for the type
   *
   * @throws IllegalArgumentException if the string is not a valid URI.
   */
  public void setArticleType(String articleType) {
    assert URI.create(articleType) != null : "Invalid PLoS Article Type" + articleType;
    this.articleType = articleType;
  }

  /**
   * Return the PLoS type of the article. The returned string is an URI.
   *
   * @return the article type as a string representation of a URI.
   */
  public String getArticleType() {
    return articleType;
  }

  /**
   * Set the list of categories for the article
   *
   * @param categories the categories to article belongs to
   */
  public void setCategories(Set<Category> categories) {
    this.categories = categories;
  }
}
