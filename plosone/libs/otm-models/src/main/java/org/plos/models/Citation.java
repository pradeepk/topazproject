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
   */
  @Predicate(uri = PLoS.bibtex + "hasYear", dataType = Rdf.xsd + "nonNegativeInteger")
  private int year;

  @Predicate(uri = PLoS.bibtex + "hasMonth", dataType = Rdf.xsd + "string")
  private String month;

  /**
   * The volume of a journal or multivolume book.
   */
  @Predicate(uri = PLoS.bibtex + "hasVolume", dataType = Rdf.xsd + "nonNegativeInteger")
  private int volume;

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
  @Predicate(uri = PLoS.bibtex + "hasEditorList", dataType = Rdf.xsd + "String",
      storeAs = Predicate.StoreAs.rdfSeq)
  private List<String> editors = new ArrayList<String>();

  /**
   * The name(s) of the author(s), in the format described in the LaTeX book.
   */
  @Predicate(uri = PLoS.bibtex + "hasAuthorList", dataType = Rdf.xsd + "String",
      storeAs = Predicate.StoreAs.rdfSeq)
  private List<String> authors = new ArrayList<String>();

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
  public int getYear() {
    return year;
  }

  /**
   * @param year the year of the citation
   */
  public void setYear(int year) {
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
  public int getVolume() {
    return volume;
  }

  /**
   * @param volume the volume of this citation
   */
  public void setVolume(int volume) {
    this.volume = volume;
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
  public List<String> getEditors() {
    return editors;
  }

  /**
   * @param editors the editors of this citation
   */
  public void setEditors(List<String> editors) {
    this.editors = editors;
  }

  /**
   * @return the authors of this citation
   */
  public List<String> getAuthors() {
    return authors;
  }

  /**
   * @param authors the authors for this citation
   */
  public void setAuthors(List<String> authors) {
    this.authors = authors;
  }
}
