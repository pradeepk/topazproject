/* $HeadURL::                                                                            $
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
package org.topazproject.otm.criterion;

import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * A parameter that can be used in place of a 'value' in Criterions.  Parameters are resolved
 * at the query execution time from a value map that is set up on {@link
 * org.topazproject.otm.Criteria Criteria}.
 *
 * @author Pradeep Krishnan
 *
 * @see org.topazproject.otm.Parameterizable
 */
@Entity(types = {Criterion.RDF_TYPE + "/Parameter"}, graph = Criterion.GRAPH)
@UriPrefix(Criterion.NS)
public class Parameter {
  private String parameterName;
  private URI    parameterId;

  /**
   * Creates a new Parameter object.
   */
  public Parameter() {
  }

  /**
   * Creates a new Parameter object.
   *
   * @param name The name of the parameter
   */
  public Parameter(String name) {
    this.parameterName = name;
  }

  /**
   * Get parameterName.
   *
   * @return parameterName as String.
   */
  public String getParameterName() {
    return parameterName;
  }

  /**
   * Set parameterName.
   *
   * @param parameterName the value to set.
   */
  @Predicate
  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return "Parameter[" + parameterName + "]";
  }

  /**
   * Get parameterId.
   *
   * @return parameterId as URI.
   */
  public URI getParameterId() {
    return parameterId;
  }

  /**
   * Set parameterId. Used for persistence. Ignored otherwise.
   *
   * @param parameterId the value to set.
   */
  @Id
  @GeneratedValue(uriPrefix = Criterion.RDF_TYPE + "/Parameter/Id/")
  public void setParameterId(URI parameterId) {
    this.parameterId = parameterId;
  }
}
