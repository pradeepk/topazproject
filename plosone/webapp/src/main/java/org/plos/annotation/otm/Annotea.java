/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.otm;

import java.net.URI;

import java.util.Date;

import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * Annotea meta-data.
 *
 * @author Pradeep Krishnan
 */
@Entity(model = "ri")
@UriPrefix(Annotea.NS)
public abstract class Annotea {
  /**
   * DOCUMENT ME!
   */
  public static final String NS = "http://www.w3.org/2000/10/annotation-ns#";
  @Id @GeneratedValue(generatorClass = "org.topazproject.otm.id.GUIDGenerator",
      uriPrefix = "info:doi/10.1371/annotation/")
  private URI                                               id;
  private Date                                              created;
  //private Object                                            body;

  @Predicate(uri = Rdf.dc + "creator")
  private String                                            creator;
  @Predicate(uri = Rdf.dc + "title")
  private String                                            title;
  @Predicate(uri = Rdf.dc_terms + "mediator")
  private String                                            mediator;
  @Predicate(uri = Rdf.topaz + "state")
  private int                                               state;

/**
   * Creates a new Annotea object.
   */
  public Annotea() {
  }

/**
   * Creates a new Annotea object.
   */
  public Annotea(URI id) {
    this.id = id;
  }

  /**
   * Get creator.
   *
   * @return creator as String.
   */
  public String getCreator() {
    return creator;
  }

  /**
   * Set creator.
   *
   * @param creator the value to set.
   */
  public void setCreator(String creator) {
    this.creator = creator;
  }

  /**
   * Get created.
   *
   * @return created as Date.
   */
  public Date getCreated() {
    return created;
  }

  /**
   * Set created.
   *
   * @param created the value to set.
   */
  public void setCreated(Date created) {
    this.created = created;
  }

  /**
   * Get body.
   *
   * @return body as an Object.
   */
  //public abstract AnnotationBody getBody();

  /**
   * Set body.
   *
   * @param body the value to set.
   */
  //public abstract void setBody(AnnotationBody body);
  
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
   * Get title.
   *
   * @return title as String.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set title.
   *
   * @param title the value to set.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get mediator.
   *
   * @return mediator as String.
   */
  public String getMediator() {
    return mediator;
  }

  /**
   * Set mediator.
   *
   * @param mediator the value to set.
   */
  public void setMediator(String mediator) {
    this.mediator = mediator;
  }

  /**
   * Get state.
   *
   * @return state as int.
   */
  public int getState() {
    return state;
  }

  /**
   * Set state.
   *
   * @param state the value to set.
   */
  public void setState(int state) {
    this.state = state;
  }
}
