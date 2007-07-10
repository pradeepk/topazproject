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
 */
@Entity(type = Rdf.topaz + "Article", model = "ri")
public class Article extends ObjectInfo {

  /** Article state of "Active" */
  public static final int STATE_ACTIVE   = 0;
  /** Active article states */
  public static final int[] ACTIVE_STATES = {STATE_ACTIVE};

  /** Article state of "Disabled" */
  public static final int STATE_DISABLED = 1;

// dublin-core predicates
  /** Subjects are really a raw representation of category/subCategory */
  @Predicate(uri = Rdf.dc + "subject", dataType = Rdf.rdf + "XMLLiteral")
  private Set<String> subjects = new HashSet<String>();
  @Predicate(uri = Rdf.dc + "language")
  private String language; // always 'en'
  @Predicate(uri = Rdf.dc + "publisher", dataType = Rdf.rdf + "XMLLiteral")
  private String publisher;
  @Predicate(uri = Rdf.dc + "format")
  private String format;

  @Predicate(uri = Rdf.dc_terms + "available", dataType = Rdf.xsd + "date")
  private Date available;
  @Predicate(uri = Rdf.dc_terms + "hasPart")
  private Set<ObjectInfo> parts = new HashSet<ObjectInfo>();
  @Predicate(uri = Rdf.dc_terms + "issued", dataType = Rdf.xsd + "date")
  private Date issued;
  @Predicate(uri = Rdf.dc_terms + "dateSubmitted", dataType = Rdf.xsd + "date")
  private Date dateSubmitted;
  @Predicate(uri = Rdf.dc_terms + "dateAccepted", dataType = Rdf.xsd + "date")
  private Date dateAccepted;

  @Predicate(uri = Rdf.topaz + "hasCategory")
  private Set<Category> categories = new HashSet<Category>();
  // TODO: Change this to Set<User> once User model is done
  @Predicate(uri = Rdf.topaz + "userIsAuthor")
  private Set<URI> userAuthors = new HashSet<URI>();

  // New for 0.8:
  @Predicate(uri = Rdf.topaz + "articleType")
  private String articleType;
  @Predicate(uri = Rdf.topaz + "volume")
  private int volume;
  @Predicate(uri = Rdf.topaz + "issue")
  private int issue;
  @Predicate(uri = Rdf.topaz + "journalTitle")
  private String journalTitle;
  @Predicate(uri = Rdf.topaz + "publisherName")
  private String publisherName;
  @Predicate(uri = Rdf.topaz + "copyrightStatement")
  private String copyrightStatement;
  @Predicate(uri = Rdf.topaz + "copyrightYear")
  private int copyrightYear;
  @Predicate(uri = Rdf.topaz + "pageCount")
  private int pageCount;
  @Predicate(uri = Rdf.topaz + "affiliations")
  private Set<String> affiliations = new HashSet<String>();
  @Predicate(uri = Rdf.topaz + "authors", storeAs = Predicate.StoreAs.rdfList)
  private List<String> orderedAuthors = new ArrayList<String>();
  @Predicate(uri = Rdf.topaz + "body")
  private String body;
  @Predicate(uri = Rdf.topaz + "references", storeAs = Predicate.StoreAs.rdfList)
  private List<Reference> references = new ArrayList<Reference>();

  /**
   * @return the date the article was made available
   */
  public Date getAvailable() {
    return available;
  }

  /**
   * @param available the date the article was made available
   */
  public void setAvailable(Date available) {
    this.available = available;
  }

  /**
   * @return the categories
   */
  public Set<Category> getCategories() {
    return categories;
  }

  /**
   * @param categories the categories to article belongs to
   */
  public void setCategories(Set<Category> categories) {
    this.categories = categories;
  }

  /**
   * @return the dateAccepted
   */
  public Date getDateAccepted() {
    return dateAccepted;
  }

  /**
   * @param dateAccepted the date the article was accepted
   */
  public void setDateAccepted(Date dateAccepted) {
    this.dateAccepted = dateAccepted;
  }

  /**
   * @return the dateSubmitted
   */
  public Date getDateSubmitted() {
    return dateSubmitted;
  }

  /**
   * @param dateSubmitted the date the article was submitted
   */
  public void setDateSubmitted(Date dateSubmitted) {
    this.dateSubmitted = dateSubmitted;
  }

  /**
   * @return dc:format
   */
  public String getFormat() {
    return format;
  }

  /**
   * @param format the dc:format to set
   */
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   * @return the dc_terms:issued
   */
  public Date getIssued() {
    return issued;
  }

  /**
   * @param issued the dc:issued when the article was issued
   */
  public void setIssued(Date issued) {
    this.issued = issued;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }

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
   * @return the publisher
   */
  public String getPublisher() {
    return publisher;
  }

  /**
   * @param publisher the name of the publisher
   */
  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  /**
   * @return the subjects the article is about
   */
  public Set<String> getSubjects() {
    return subjects;
  }

  /**
   * @param subjects the subjects the article is about
   */
  public void setSubjects(Set<String> subjects) {
    this.subjects = subjects;
  }

  /**
   * @return the set of users that are authors of this article
   */
  public Set<URI> getUserAuthors() {
    return userAuthors;
  }

  /**
   * @param userAuthors the set of users that are also authors of this article
   */
  public void setUserAuthors(Set<URI> userAuthors) {
    this.userAuthors = userAuthors;
  }

  /**
   * @return the article type
   */
  public String getArticleType() {
    return articleType;
  }

  /**
   * @param articleType the article type
   */
  public void setArticleType(String articleType) {
    this.articleType = articleType;
  }

  /**
   * @return the volume the article belongs to
   */
  public int getVolume() {
    return volume;
  }

  /**
   * @param volume the volume number the article belongs to
   */
  public void setVolume(int volume) {
    this.volume = volume;
  }

  /**
   * @return the issue the article belongs to
   */
  public int getIssue() {
    return issue;
  }

  /**
   * @param issue the issue the article belongs to
   */
  public void setIssue(int issue) {
    this.issue = issue;
  }

  /**
   * @returns the journal title
   */
  public String getJournalTitle() {
    return journalTitle;
  }

  /**
   * @param journalTitle the title of the journal
   */
  public void setJournalTitle(String journalTitle) {
    this.journalTitle = journalTitle;
  }

  /**
   * @return the name of the publisher
   */
  public String getPublisherName() {
    return publisherName;
  }

  /**
   * @param publisherName the name of the publisher
   */
  public void setPublisherName(String publisherName) {
    this.publisherName = publisherName;
  }

  /**
   * @return the copyright on the aritcle
   */
  public String getCopyrightStatement() {
    return copyrightStatement;
  }

  /**
   * @param copyrightStatement the copyright on the article
   */
  public void setCopyrightStatement(String copyrightStatement) {
    this.copyrightStatement = copyrightStatement;
  }

  /**
   * @return the year of the copyright
   */
  public int getCopyrightYear() {
    return copyrightYear;
  }

  /**
   * @param copyrightYear the year of the copyright
   */
  public void setCopyrightYear(int copyrightYear) {
    this.copyrightYear = copyrightYear;
  }

  /**
   * @return the number of pages in the article
   */
  public int getPageCount() {
    return pageCount;
  }

  /**
   * @param pageCount the number of pages in the aritcle
   */
  public void setPageCount(int pageCount) {
    this.pageCount = pageCount;
  }

  /**
   * @return a list of affiliations
   */
  public Set<String> getAffiliations() {
    return affiliations;
  }

  /**
   * @param affiliations a set of affiliations
   */
  public void setAffiliations(Set<String> affiliations) {
    this.affiliations = affiliations;
  }

  /**
   * @return an ordered list of authors
   */
  public List<String> getOrderedAuthors() {
    return orderedAuthors;
  }

  /**
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
