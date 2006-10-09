/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.article;

/** 
 * This hold the information returned about a representation of an object.
 *
 * <p>This class is a bean.
 * 
 * @author Ronald Tschalär
 */
public class RepresentationInfo {
  /** The name of the representation. */
  private String name;
  /** The mime-type of the representation content. */
  private String contentType;
  /** The size, in bytes, of the representation content. */
  private long   size;

  /**
   * Get the name of the representation.
   *
   * @return the name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the representation.
   *
   * @param name the name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the mime-type of the content of the representation.
   *
   * @return the content-type.
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Set the mime-type of the content of the representation.
   *
   * @param contentType the content-type.
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * Get the size of the representation.
   *
   * @return the size, in bytes, or -1 if unknown.
   */
  public long getSize() {
    return size;
  }

  /**
   * Set the size of the representation.
   *
   * @param size the size, in bytes.
   */
  public void setSize(long size) {
    this.size = size;
  }
}
