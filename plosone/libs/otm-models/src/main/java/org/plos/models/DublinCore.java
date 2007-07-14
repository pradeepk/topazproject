/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/otm-models/src/main/java/org/#$
 * $Id: ObjectInfo.java 2758 2007-05-20 03:11:36Z ronald $
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * Model for a subset of the elements of the Dublin Core metadata
 * specification.  Details on the specification and the individual elements can
 * be found at http://dublincore.org/documents/dcmi-terms/.
 *
 * Please note that in most cases the function names map directly into the term
 * name.
 *
 * @author Amit Kapoor
 */
public class DublinCore {
  /**
   * Typically, a Title will be a name by which the resource is formally
   * known.
   */
  @Predicate(uri = Rdf.dc + "title", dataType = Rdf.rdf + "XMLLiteral")
  private String title;

  /**
   * Description may include but is not limited to: an abstract, a table of
   * contents, a graphical representation, or a free-text account of the
   * resource.
   */
  @Predicate(uri = Rdf.dc + "description", dataType = Rdf.rdf + "XMLLiteral")
  private String description;

  /**
   * Examples of a Creator include a person, an organization, or a service.
   * Typically, the name of a Creator should be used to indicate the entity.
   */
  @Predicate(uri = Rdf.dc + "creator")
  private Set<String> creators = new HashSet<String>();

  /**
   * Date may be used to express temporal information at any level of
   * granularity. Recommended best practice is to use an encoding scheme, such
   * as the W3CDTF profile of ISO 8601 [W3CDTF].
   */
  @Predicate(uri = Rdf.dc + "date", dataType = Rdf.xsd + "date")
  private Date date;

  /**
   * Recommended best practice is to identify the resource by means of a string
   * conforming to a formal identification system.
   */
  @Predicate(uri = Rdf.dc + "identifier")
  private String identifier;

  /**
   * Typically, rights information includes a statement about various property
   * rights associated with the resource, including intellectual property
   * rights.
   */
  @Predicate(uri = Rdf.dc + "rights", dataType = Rdf.rdf + "XMLLiteral")
  private String rights;

  /**
   * Recommended best practice is to use a controlled vocabulary such as the
   * DCMI Type Vocabulary [DCMITYPE]. To describe the file format, physical
   * medium, or dimensions of the resource, use the Format element.
   */
  @Predicate(uri = Rdf.dc + "type")
  private URI type;

  /**
   * Examples of a Contributor include a person, an organization, or a service.
   * Typically, the name of a Contributor should be used to indicate the
   * entity.
   */
  @Predicate(uri = Rdf.dc + "contributor")
  private Set<String> contributors = new HashSet<String>();

  /**
   * Typically, the topic will be represented using keywords, key phrases, or
   * classification codes. Recommended best practice is to use a controlled
   * vocabulary. To describe the spatial or temporal topic of the resource, use
   * the Coverage element.
   */
  @Predicate(uri = Rdf.dc + "subject", dataType = Rdf.rdf + "XMLLiteral")
  private Set<String> subjects = new HashSet<String>();

  /**
   * Recommended best practice is to use a controlled vocabulary such as RFC
   * 3066 [RFC3066].
   */
  @Predicate(uri = Rdf.dc + "language")
  private String language;

  /**
   * Examples of a Publisher include a person, an organization, or a service.
   * Typically, the name of a Publisher should be used to indicate the entity.
   */
  @Predicate(uri = Rdf.dc + "publisher", dataType = Rdf.rdf + "XMLLiteral")
  private String publisher;

  /**
   * Examples of dimensions include size and duration. Recommended best
   * practice is to use a controlled vocabulary such as the list of Internet
   * Media Types [MIME].
   */
  @Predicate(uri = Rdf.dc + "format")
  private String format;

  /**
   * The described resource may be derived from the related resource in whole
   * or in part. Recommended best practice is to identify the related resource
   * by means of a string conforming to a formal identification system.
   */
  @Predicate(uri = Rdf.dc + "source")
  private Object source;

  /**
   * Date (often a range) that the resource will become or did become
   * available.
   */
  @Predicate(uri = Rdf.dc_terms + "available", dataType = Rdf.xsd + "date")
  private Date available;

  /**
   * Date of formal issuance (e.g., publication) of the resource.
   */
  @Predicate(uri = Rdf.dc_terms + "issued", dataType = Rdf.xsd + "date")
  private Date issued;

  /**
   * Date of submission of the resource (e.g. thesis, articles, etc.).
   */
  @Predicate(uri = Rdf.dc_terms + "dateSubmitted", dataType = Rdf.xsd + "date")
  private Date submitted;

  /**
   * Date of acceptance of the resource (e.g. of thesis by university
   * department, of article by journal, etc.).
   */
  @Predicate(uri = Rdf.dc_terms + "dateAccepted", dataType = Rdf.xsd + "date")
  private Date accepted;

  /**
   * Date of a statement of copyright.
   */
  @Predicate(uri = Rdf.dc_terms + "dateCopyrighted")
  private int copyrightYear;

  /**
   * A summary of the content of the resource.
   */
  @Predicate(uri = Rdf.dc_terms + "abstract")
  private Set<String> summary;

  /**
   * A bibliographic reference for the resource.
   */
  @Predicate(uri = Rdf.dc_terms + "bibliographicCitation")
  private Citation bibliographicCitation;

  /**
   * Date of creation of the object
   */
  @Predicate(uri = Rdf.dc_terms + "created", dataType = Rdf.xsd + "date")
  private Date created;

  /**
   * A legal document giving official permission to do something with the
   * resource.
   */
  @Predicate(uri = Rdf.dc_terms + "license")
  private Set<License> license;

  /**
   * Date of modification of the object
   */
  @Predicate(uri = Rdf.dc_terms + "modified", dataType = Rdf.xsd + "date")
  private Date modified;

  /**
   * The described resource references, cites, or otherwise points to the
   * referenced resource.
   */
  @Predicate(uri = Rdf.dc_terms + "references")
  private Set<Citation> references;

  /**
   * Empty contructor
   */
  public DublinCore() {
  }

  /**
   * Return the list of creators of the object
   *
   * @return the creators
   */
  public Set<String> getCreators() {
    return creators;
  }

  /**
   * Set the list of creators of the object
   *
   * @param creators the set of creators for this object
   */
  public void setCreators(Set<String> creators) {
    this.creators = creators;
  }

  /**
   * Return the list of contributors
   *
   * @return the contributors
   */
  public Set<String> getContributors() {
    return contributors;
  }

  /**
   * Set the list of contributors
   *
   * @param contributors the contributors to set
   */
  public void setContributors(Set<String> contributors) {
    this.contributors = contributors;
  }

  /**
   * Return the date
   *
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * Set the date
   *
   * @param date the date to set
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Return the type of the object
   *
   * @return the type
   */
  public URI getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(URI type) {
    this.type = type;
  }

  /**
   * Return the description of the object
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the description of the object
   *
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Return the identifier of the object
   * @return the identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Set the identifier of the object
   *
   * @param identifier the identifier to set
   */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /**
   * Return the rights of the objects
   *
   * @return the rights
   */
  public String getRights() {
    return rights;
  }

  /**
   * Set the rights of the object
   *
   * @param rights the rights to set
   */
  public void setRights(String rights) {
    this.rights = rights;
  }

  /**
   * Return the title of the object.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set the title of the object
   *
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Return the list of subjects the object is about
   *
   * @return the subjects the object is about
   */
  public Set<String> getSubjects() {
    return subjects;
  }

  /**
   * Set the list of subjects the object is about
   *
   * @param subjects the subjects the object is about
   */
  public void setSubjects(Set<String> subjects) {
    this.subjects = subjects;
  }

  /**
   * Get the language of the object
   *
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Set the language of the object
   *
   * @param language the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Get the name of the publisher of this object
   *
   * @return the publisher
   */
  public String getPublisher() {
    return publisher;
  }

  /**
   * Set the name of the publisher of this object
   *
   * @param publisher the name of the publisher
   */
  public void setPublisher(String publisher) {
    this.publisher = publisher;
   }

  /**
   * Get the format of the object
   *
   * @return format
   */
  public String getFormat() {
    return format;
  }

  /**
   * Set the format of the object
   *
   * @param format the dc:format to set
   */
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   * Get the date the object was made available
   *
   * @return the date the object was made available
   */
  public Date getAvailable() {
    return available;
  }

  /**
   * Set the date the object was made available.
   *
   * @param available the date the object was made available
   */
  public void setAvailable(Date available) {
    this.available = available;
  }

  /**
   * Get the issued date
   *
   * @return the issued date
   */
  public Date getIssued() {
    return issued;
  }

  /**
   * Set the issued date
   *
   * @param issued the date the object was issued
   */
  public void setIssued(Date issued) {
    this.issued = issued;
  }

  /**
   * Get the submission date
   *
   * @return the submission date
   */
  public Date getSubmitted() {
    return submitted;
  }

  /**
   * Set the submission date
   *
   * @param submitted the date the object was submitted
   */
  public void setSubmitted(Date submitted) {
    this.submitted = submitted;
  }

  /**
   * Return the acceptance date
   *
   * @return the accpetance date
   */
  public Date getAccepted() {
    return accepted;
  }

  /**
   * Set the acceptance date
   *
   * @param accepted the date the object was accepted
   */
  public void setAccepted(Date accepted) {
    this.accepted = accepted;
  }

  /**
   * Return the year of the copyright
   *
   * @return the year of the copyright
   */
  public int getCopyrightYear() {
    return copyrightYear;
  }

  /**
   * Set the year of the copyright
   *
   * @param copyrightYear the year of the copyright
   */
  public void setCopyrightYear(int copyrightYear) {
    this.copyrightYear = copyrightYear;
  }

  /**
   * Return the source of the object
   *
   * @return the source of the object
   */
  public Object getSource() {
    return source;
  }

  /**
   * Set the source of the object
   *
   * @param source the source of the object
   */
  public void setSource(Object source) {
    this.source = source;
  }

  /**
   * Return the abstract/summary on the object
   *
   * @return the abstract/summary of the object
   */
  public Set<String> getSummary() {
    return summary;
  }

  /**
   * Set the abstract/summary of the object
   *
   * @param summary the summary/abstract of the object
   */
  public void setSummary(Set<String> summary) {
    this.summary = summary;
  }

  /**
   * Return the bibliographic citation for the object
   *
   * @return bibliographic citation for the object
   */
  public Citation getBibliographicCitation() {
    return bibliographicCitation;
  }

  /**
   * bibliographic citation for the object
   *
   * @param bibliographicCitation bibliographic citation for the object
   */
  public void setBibliographicCitation(Citation bibliographicCitation) {
    this.bibliographicCitation = bibliographicCitation;
  }

  /**
   * Return the creation date
   *
   * @return the creation date
   */
  public Date getCreated() {
    return created;
  }

  /**
   * Set the creation date
   *
   * @param created the date the object was created
   */
  public void setCreated(Date created) {
    this.created = created;
  }

  /**
   * Return the set of licenses for this object
   *
   * @return the set of licenses for this object
   */
  public Set<License> getLicense() {
    return license;
  }

  /**
   * Set the set of licenses for this object
   *
   * @param license the set of licenses governing this object
   */
  public void setLicense(Set<License> license) {
    this.license = license;
  }

  /**
   * Return the modified date
   *
   * @return the modified date
   */
  public Date getModified() {
    return modified;
  }

  /**
   * Set the modified date
   *
   * @param modified the date the object was modified
   */
  public void setModified(Date modified) {
    this.modified = modified;
  }

  /**
   * Return the set of objects referred to by this object
   *
   * @return the set of objects referred to by this object
   */
  public Set<Citation> getReferences() {
    return references;
  }

  /**
   * Set the set of objects referred to by this object
   *
   * @param references the set of objects referred to by this object
   */
  public void setReferences(Set<Citation> references) {
    this.references = references;
  }
}
