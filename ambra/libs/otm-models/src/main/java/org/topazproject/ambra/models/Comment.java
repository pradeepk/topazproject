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
package org.topazproject.ambra.models;

import org.topazproject.otm.annotations.Entity;

/**
 * Class that will represent comments by users persisted as comment annotations by Ambra.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Comment.RDF_TYPE)
public class Comment extends ArticleAnnotation {
  private static final long  serialVersionUID = 7759871310632000347L;
  public  static final String RDF_TYPE = Annotea.W3C_TYPE_NS + "Comment";
  /*
   * inherited javadoc
   */
  public String getType() {
    return RDF_TYPE;
  }
}
