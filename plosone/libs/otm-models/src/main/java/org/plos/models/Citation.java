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

import java.util.List;
import java.util.ArrayList;

import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.annotations.Embeddable;
import org.topazproject.otm.annotations.UriPrefix;

@Embeddable
@UriPrefix(Rdf.topaz)
public class Citation {
  private String citationType;
  private Integer year;
  private String source;
  private String comment;
  private Integer volume;
  private String publisherLocation;
  private String publisherName;

  @Predicate(uri = Rdf.dc + "title", dataType = Rdf.rdf + "XMLLiteral")
  private String articleTitle;
  @Predicate(uri = Rdf.topaz + "firstPage")
  private Integer firstPage;
  @Predicate(uri = Rdf.topaz + "lastPage")
  private Integer lastPage;
  @Predicate(uri = Rdf.topaz + "authors", storeAs = Predicate.StoreAs.rdfList)
  private List<String> authors = new ArrayList<String>();
  @Predicate(uri = Rdf.topaz + "editors", storeAs = Predicate.StoreAs.rdfList)
  private List<String> editors = new ArrayList<String>();

  /**
   * @return the citation type
   */
  public String getCitationType() {
    return citationType;
  }

  /**
   * @param citationType the type of this citation
   */
  public void setCitationType(String citationType) {
    this.citationType = citationType;
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
   * @return the title of the citation's article
   */
  public String getArticleTitle() {
    return articleTitle;
  }

  /**
   * @param articleTitle the title of the citation's article
   */
  public void setArticleTitle(String articleTitle) {
    this.articleTitle = articleTitle;
  }

  /**
   * @return source the source of the citation
   */
  public String getSource() {
    return source;
  }

  /**
   * @param source the source of the citation
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * @return the comment associated with this citation
   */
  public String getComment() {
    return comment;
  }

  /**
   * @param comment the comment for this citation
   */
  public void setComment(String comment) {
    this.comment = comment;
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
   * @return the first page the citaiton is on
   */
  public Integer getFirstPage() {
    return firstPage;
  }

  /**
   * @param firstPage the page the citation is from
   */
  public void setFirstPage(Integer firstPage) {
    this.firstPage = firstPage;
  }

  /**
   * @return the last page the citation is on
   */
  public Integer getLastPage() {
    return lastPage;
  }

  /**
   * @param lastPage the last page the citaiton is found on
   */
  public void setLastPage(Integer lastPage) {
    this.lastPage = lastPage;
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
}
