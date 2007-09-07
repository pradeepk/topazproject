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

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * Marker class to mark an Aggregation as a "Volume".
 *
 * @author Jeff Suttor
 */
@Entity(type = PLoS.plos + "Volume", model = "ri")
public class Volume extends Aggregation {
  @Predicate(uri = PLoS.plos + "key")
  private String  key;

  /** Journal's eIssn */
  @Predicate(uri = PLoS.plos + "Volume/journal")
  private String journal;

  @Predicate(uri = PLoS.plos + "Volume/prevVolume")
  private URI prevVolume;

  @Predicate(uri = PLoS.plos + "Volume/nextVolume")
  private URI nextVolume;

  @Predicate(uri = PLoS.plos + "Volume/image")
  private URI image;

  /**
   * Get the internal key used to identify this Volume.
   *
   * @return the key.
   */
  public String getKey() {
    return key;
  }

  /**
   * Set the internal key used to identify this Volume.
   *
   * @param key the key.
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Get the image for this Volume.
   *
   * @return the image, may be null.
   */
  public URI getImage() {
    return image;
  }

  /**
   * Set the image for this Volume.
   *
   * @param image the image, may be null.
   */
  public void setImage(URI image) {
    this.image = image;
  }

  /**
   * Get the Journal (eIssn) for this Volume.
   *
   * @return the Journal (eIssn).
   */
  public String getJournal() {
    return journal;
  }

  /**
   * Set the Journal (eIssn) for this Volume.
   *
   * @param journal the Journal (eIssn).
   */
  public void setJournal(String journal) {
    this.journal = journal;
  }

  /**
   * Get the previous Volume for this Volume.
   *
   * @return the previous Volume, may be null.
   */
  public URI getPrevVolume() {
    return prevVolume;
  }

  /**
   * Set the previous Volume for this Volume.
   *
   * @param prevVolume the previous Volume, may be null.
   */
  public void setPrevVolume(URI prevVolume) {
    this.prevVolume = prevVolume;
  }

  /**
   * Get the next Volume for this Volume.
   *
   * @return the next Volume, may be null.
   */
  public URI getNextVolume() {
    return nextVolume;
  }

  /**
   * Set the next Volume for this Volume.
   *
   * @param nextVolume the next Volume, may be null.
   */
  public void setNextVolume(URI nextVolume) {
    this.nextVolume = nextVolume;
  }
}
