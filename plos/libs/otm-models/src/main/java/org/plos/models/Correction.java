/* $HeadURL::$
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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

import java.io.Serializable;
import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * Correction is an abstract Model class that represents types of correction annotation. 
 * It is a superclass to MinorCorrection and FormalCorrection 
 * 
 * @author Alex Worden
 */
@Entity(type = Annotea.W3C_TYPE_NS + "Change")
public class Correction extends Annotation implements ArticleAnnotation, Serializable {
  private static final long serialVersionUID = -8174779804923945692L;

  @Predicate(uri = Annotea.W3C_NS + "body")
  private URI body;

  /* (non-Javadoc)
   * @see org.plos.models.ArticleAnnotation#getBody()
   */
  public URI getBody() {
    return body;
  }

  /* (non-Javadoc)
   * @see org.plos.models.ArticleAnnotation#setBody(java.net.URI)
   */
  public void setBody(URI body) {
    this.body = body;
  }

  /**
   * Human friendly string for display and debugging.
   *
   * @return String for human consumption.
   */
  public String toString() {
    return "Correction: " + super.toString();
  }
}

