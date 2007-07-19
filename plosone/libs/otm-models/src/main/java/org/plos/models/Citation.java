/* $HeadURL::                                                                                     $
 * $Id: $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.net.URI;

import java.util.List;
import java.util.ArrayList;

import org.topazproject.otm.annotations.Embeddable;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * Citation information
 *
 * @author Eric Brown
 * @author Amit Kapoor
 */
@Entity(type = PLoS.bibtex + "Entry", model = "ri")
@UriPrefix(Rdf.topaz)
public abstract class Citation {
  @Id
  private URI   id;

  /**
   * The year of publication or, for an unpublished work, the year it was
   * written. Generally it should consist of four numerals, such as 1984,
   * although the standard styles can handle any year whose last four
   * nonpunctuation characters are numerals, such as '(about 1984)'
   *
   * TODO: Restore to correct datatype .Stored as double because of bug in
   * Mulgara
   */
  @Predicate(uri = PLoS.bibtex + "hasYear", dataType = Rdf.xsd + "double")
  private Integer year;

  @Predicate(uri = PLoS.bibtex + "hasMonth", dataType = Rdf.xsd + "string")
  private String month;

  /**
   * The volume of a journal or multivolume book.
   *
   * TODO: Restore to correct datatype .Stored as double because of bug in
   * Mulgara
   */
  @Predicate(uri = PLoS.bibtex + "hasVolume", dataType = Rdf.xsd + "double")
  private Integer volume;

  /**
   * The number of a journal, magazine, technical report, or of a work in a
   * series. An issue of a journal or magazine is usually identified by its
   * volume and number; the organization that issues a technical report usually
   * gives it a number; and sometimes books are given numbers in a named
   * series.
   */
  @Predicate(uri = PLoS.bibtex + "hasNumber", dataType = Rdf.xsd + "XMLLiteral")
  private String issue;

  /**
   * Typically, a Title will be a name by which the resource is formally known.
   */
  @Predicate(uri = Rdf.dc + "title", dataType = Rdf.rdf + "XMLLiteral")
  private String title;

  /**
   * Usually the address of the publisher or other type of institution. For
   * major publishing houses, van Leunen recommends omitting the information
   * entirely. For small publishers, on the other hand, you can help the reader
   * by giving the complete address.
   */
  @Predicate(uri = PLoS.bibtex + "hasAddress", dataType = Rdf.xsd + "String")
  private String publisherLocation;

  /**
   * The publisher's name.
   */
  @Predicate(uri = PLoS.bibtex + "hasPublisher", dataType = Rdf.xsd + "String")
  private String publisherName;

  /**
   * One or more page numbers or range of numbers, such as 42-111 or 7,41,73-97
   * or 43+ (the `+' in this last example indicates pages following that don't
   * form a simple range). To make it easier to maintain Scribe-compatible
   * databases, the standard styles convert a single dash (as in 7-33) to the
   * double dash used in TeX to denote number ranges (as in 7-33).
   */
  @Predicate(uri = PLoS.bibtex + "hasPages", dataType = Rdf.xsd + "String")
  private String pages;

  /**
   * A journal name. Abbreviations are provided for many journals; see the
   * Local Guide
   */
  @Predicate(uri = PLoS.bibtex + "hasJournal", dataType = Rdf.xsd + "String")
  private String journal;

  /**
   * Any additional information that can help the reader. The first word should
   * be capitalized.
   */
  @Predicate(uri = PLoS.bibtex + "hasNote", dataType = Rdf.xsd + "String")
  private String note;

  /**
   * Name(s) of editor(s), typed as indicated in the LaTeX book. If there is
   * also an author field, then the editor field gives the editor of the book
   * or collection in which the reference appears.
   */
  @Predicate(uri = PLoS.bibtex + "hasEditorList", storeAs = Predicate.StoreAs.rdfSeq)
  private List<UserProfile> editors = new ArrayList<UserProfile>();

  /**
   * The name(s) of the author(s), in the format described in the LaTeX book.
   */
  @Predicate(uri = PLoS.bibtex + "hasAuthorList", storeAs = Predicate.StoreAs.rdfSeq)
  private List<UserProfile> authors = new ArrayList<UserProfile>();

  /**
   * The WWW Universal Resource Locator that points to the item being
   * referenced. This often is used for technical reports to point to the ftp
   * or web site where the postscript source of the report is located.
   */
  @Predicate(uri = PLoS.bibtex + "hasURL", dataType = Rdf.xsd + "String")
  private String url;

  /**
   * An abstract of the work
   */
  @Predicate(uri = PLoS.bibtex + "hasAbstract")
  private String summary;

  /**
   * This will be used to indicate the type of citation
   */
  @Predicate(uri = Rdf.rdf + "type", dataType = Rdf.xsd + "anyURI")
  private String citationType;

  /**
   * Get id.
   *
   * @return id as URI.
   */
  public URI getId() {
    return id;
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  public void setId(URI id) {
    this.id = id;
  }

  /**
   * @returns the year of the citation (if available)
   */
  public Integer getYear() {
    return year;
  }

  /**
   * @param year the year of the citation
   */
  public void setYear(Integer year) {
    this.year = year;
  }

  /**
   * @returns the month of the citation (if available)
   */
  public String getMonth() {
    return month;
  }

  /**
   * @param month the month of the citation
   */
  public void setMonth(String month) {
    this.month = month;
  }

  /**
   * @return the volume this citation is in
   */
  public Integer getVolume() {
    return volume;
  }

  /**
   * @param volume the volume of this citation
   */
  public void setVolume(Integer volume) {
    this.volume = volume;
  }

  /**
   * @return the issue of the citation's article
   */
  public String getIssue() {
    return issue;
  }

  /**
   * @param title the issue of the citation's article
   */
  public void setIssue(String issue) {
    this.issue = issue;
  }

  /**
   * @return the title of the citation's article
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title of the citation's article
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the publisher's location
   */
  public String getPublisherLocation() {
    return publisherLocation;
  }

  /**
   * @param publisherLocation the location of the publisher
   */
  public void setPublisherLocation(String publisherLocation) {
    this.publisherLocation = publisherLocation;
  }

  /**
   * @return the publisher's name
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
   * @return the pages the citation is on
   */
  public String getPages() {
    return pages;
  }

  /**
   * @param pages the pages the citation is from
   */
  public void setPages(String pages) {
    this.pages = pages;
  }

  /**
   * @return journal the source of the citation
   */
  public String getJournal() {
    return journal;
  }

  /**
   * @param journal the journal of the citation
   */
  public void setJournal(String journal) {
    this.journal = journal;
  }

  /**
   * @return the note associated with this citation
   */
  public String getNote() {
    return note;
  }

  /**
   * @param note the note for this citation
   */
  public void setNote(String note) {
    this.note = note;
  }

  /**
   * @return the editors of this citation
   */
  public List<UserProfile> getEditors() {
    return editors;
  }

  /**
   * @param editors the editors of this citation
   */
  public void setEditors(List<UserProfile> editors) {
    this.editors = editors;
  }

  /**
   * @return the authors of this citation
   */
  public List<UserProfile> getAuthors() {
    return authors;
  }

  /**
   * @param authors the authors for this citation
   */
  public void setAuthors(List<UserProfile> authors) {
    this.authors = authors;
  }

  /**
   * @return the URL for the object
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url the URL for the object
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Return the abstract/summary on the object
   *
   * @return the abstract/summary of the object
   */
  public String getSummary() {
    return summary;
  }

  /**
   * Set the abstract/summary of the object
   *
   * @param summary the summary/abstract of the object
   */
  public void setSummary(String summary) {
    this.summary = summary;
  }

  /**
   * Set the citation type. Bibtex specifies different type of citations and
   * this field is intended to track that. Please note that the string passed
   * should be a valid URI.
   *
   * @param citationType the string representation of the URI for the type
   *
   * @throws IllegalArgumentException if the string is not a valid URI.
   */
  public void setCitationType(String citationType) {
    assert URI.create(citationType) != null : "Invalid PLoS Citation Type" + citationType;
    this.citationType = citationType;
  }

  /**
   * Return the type of the citation. The returned string is an URI.
   *
   * @return the citation type as a string representation of a URI.
   */
  public String getCitationType() {
    return citationType;
  }
}
