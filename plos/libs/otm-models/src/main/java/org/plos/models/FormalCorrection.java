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

import org.topazproject.otm.annotations.Entity;

@Entity(type = FormalCorrection.RDF_TYPE)
public class FormalCorrection extends Correction implements ArticleAnnotation {
  private static final long serialVersionUID = 4949878990530615857L;

  public static final String RDF_TYPE = Annotea.TOPAZ_TYPE_NS + "FormalCorrection";
  public String getType() {
    return RDF_TYPE;
  }

  /**
   * Human friendly string for display and debugging.
   *
   * @return String for human consumption.
   */
  public String toString() {
    return "FormalCorrection: {"
            + "type: " + getType()
            + ", " + super.toString()
            + "}";
  }
}
