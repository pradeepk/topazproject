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

  @Predicate(uri = Rdf.topaz + "hasCategory")
  private Set<Category> categories = new HashSet<Category>();
  // TODO: Change this to Set<User> once User model is done
  @Predicate(uri = Rdf.topaz + "userIsAuthor")
  private Set<URI> userAuthors = new HashSet<URI>();
  @Predicate(uri = Rdf.topaz + "articleType")
  private String articleType;
  @Predicate(uri = Rdf.topaz + "volume")
  private int volume;
  @Predicate(uri = Rdf.topaz + "issue")
  private int issue;
  @Predicate(uri = Rdf.topaz + "journalTitle")
  private String journalTitle;
  @Predicate(uri = Rdf.topaz + "pageCount")
  private int pageCount;
  @Predicate(uri = Rdf.topaz + "affiliations")
  private Set<String> affiliations = new HashSet<String>();

  /*
   * The DC creator list is not ordered, but there is a hierarchy in the list
   * of the authors with the most important one being mentioned first in the
   * original article. That is maintained here.
   */
  @Predicate(uri = Rdf.topaz + "authors", storeAs = Predicate.StoreAs.rdfList)
  private List<String> orderedAuthors = new ArrayList<String>();
  @Predicate(uri = Rdf.topaz + "body")
  private String body;
  @Predicate(uri = Rdf.topaz + "references", storeAs = Predicate.StoreAs.rdfList)
  private List<Reference> references = new ArrayList<Reference>();

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

  /**
   * Get the type of the article (this is PLoS specific information)
   *
   * @return the article type
   */
  public String getArticleType() {
    return articleType;
  }

  /**
   * Set the type of the article
   *
   * @param articleType the article type
   */
  public void setArticleType(String articleType) {
    this.articleType = articleType;
  }

  /**
   * Return the volume the article belongs to
   *
   * @return the volume the article belongs to
   */
  public int getVolume() {
    return volume;
  }

  /**
   * Set the volume the article belongs to
   *
   * @param volume the volume number the article belongs to
   */
  public void setVolume(int volume) {
    this.volume = volume;
  }

  /**
   * Return the issue the article belongs to
   *
   * @return the issue the article belongs to
   */
  public int getIssue() {
    return issue;
  }

  /**
   * Set the issue the article belongs to
   *
   * @param issue the issue the article belongs to
   */
  public void setIssue(int issue) {
    this.issue = issue;
  }

  /**
   * Return the journal the article belongs to
   *
   * @returns the journal title
   */
  public String getJournalTitle() {
    return journalTitle;
  }

  /**
   * Set the journal the article belongs to
   *
   * @param journalTitle the title of the journal
   */
  public void setJournalTitle(String journalTitle) {
    this.journalTitle = journalTitle;
  }

  /**
   * Get the total number of pages in the article
   *
   * @return the number of pages in the article
   */
  public int getPageCount() {
    return pageCount;
  }

  /**
   * Set the total number of pages in the article
   *
   * @param pageCount the number of pages in the aritcle
   */
  public void setPageCount(int pageCount) {
    this.pageCount = pageCount;
  }

  /**
   * Return the list of affiliations
   *
   * @return a list of affiliations
   */
  public Set<String> getAffiliations() {
    return affiliations;
  }

  /**
   * Set the list of affiliations
   *
   * @param affiliations a set of affiliations
   */
  public void setAffiliations(Set<String> affiliations) {
    this.affiliations = affiliations;
  }

  /**
   * Return the list of authors in the same order as mentioned in the original
   * article
   *
   * @return an ordered list of authors
   */
  public List<String> getOrderedAuthors() {
    return orderedAuthors;
  }

  /**
   * Set the author names in the same order as specified in the original
   * article.
   *
   * @param authors the list of authors
   */
  public void setOrderedAuthors(List<String> authors) {
    orderedAuthors = authors;
  }

  /**
   * @return the body xml
   */
  public String getBody() {
    return body;
  }

  /**
   * @param body the body xml
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * @return list of references for the article
   */
  public List<Reference> getReferences() {
    return references;
  }

  /**
   * @param set references for article
   */
  public void setReferences(List<Reference> references) {
    this.references = references;
  }
}
