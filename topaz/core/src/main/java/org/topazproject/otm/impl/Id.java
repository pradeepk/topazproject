/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.impl;

/**
 * A unique identifier for an Entity.
 *
 * @author Pradeep Krishnan
  */
class Id {
  private final String id;
  private final Class  clazz;

  /**
   * Creates a new Id object.
   *
   * @param clazz entity class
   * @param id entity id
   */
  public Id(Class clazz, String id) throws NullPointerException {
    this.id      = id;
    this.clazz   = clazz;
    if (id == null)
      throw new NullPointerException("id cannot be null");
    if (clazz == null)
      throw new NullPointerException("clazz cannot be null");
  }

  /**
   * Gets the entity id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the entity class
   *
   * @return the entity Class
   */
  public Class getClazz() {
    return clazz;
  }

  /*
   * inherited javadoc
   */
  public int hashCode() {
    return id.hashCode();
  }

  /*
   * inherited javadoc
   */
  public boolean equals(Object other) {
    if (!(other instanceof Id))
      return false;

    Id o = (Id) other;

    return id.equals(o.id) && (clazz.isAssignableFrom(o.clazz) || o.clazz.isAssignableFrom(clazz));
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return id;
  }
}
