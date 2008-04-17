/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.plos.models;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;

/**
 * This holds the information returned about a representation of an object.
 *
 * @author Pradeep Krishnan
 */
@Entity(model = "ri", type = Rdf.topaz + "Representation")
public class Representation extends Blob {
  @Id
  private String                                                           id;
  @Predicate(uri = Rdf.topaz + "representation/name")
  private String                                                           name;
  @Predicate(uri = Rdf.topaz + "representation/contentType")
  private String                                                           contentType;
  @Predicate(uri = Rdf.topaz + "representation/objectSize")
  private long                                                             size;
  @Predicate(uri = Rdf.topaz + "representation/doi")
  private ObjectInfo                                                       object;

  /**
   * No argument constructor for OTM to instantiate.
   */
  public Representation() {
  }

  /**
   * Creates a new Representation object. The id of the object is 
   * derived from the doi of the ObjectInfo and the representation name.
   *
   * @param object the object this representation belongs to
   * @param name the name of this representation
   */
  public Representation(ObjectInfo object, String name) {
    setObject(object);
    setName(name);
    setId(object.getId().toString() + "/" + name);
  }

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
   * Get the object that this representation represents.
   *
   * @return the doi
   */
  public ObjectInfo getObject() {
    return object;
  }

  /**
   * Set the object that this representation represents.
   *
   * @param object the object
   */
  public void setObject(ObjectInfo object) {
    this.object = object;
  }

  /**
   * Get the mime-type of the content of the representation.
   *
   * @return the content type.
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Set the mime-type of the content of the representation.
   *
   * @param contentType the content type.
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * Get the object size of this representation.
   *
   * @return the object size.
   */
  public long getSize() {
    return size;
  }

  /**
   * Set the object size of this representation.
   *
   * @param size the object size
   */
  public void setSize(long size) {
    this.size = size;
  }

  /**
   * Get id.
   *
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  public void setId(String id) {
    this.id = id;
  }
}
