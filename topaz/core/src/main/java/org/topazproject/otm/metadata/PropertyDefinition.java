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
package org.topazproject.otm.metadata;

/**
 * The base class for all property definitions.
 *
 * @author Pradeep Krishnan
 */
public class PropertyDefinition extends Definition {
  /**
   * Creates a new PropertyDefinition object.
   *
   * @param name   The name of this definition.
   */
  public PropertyDefinition(String name) {
    this(name, null, null);
  }

  /**
   * Creates a new PropertyDefinition object.
   *
   * @param name   The name of this definition.
   * @param reference The definition to refer to resolve undefined attribiutes or null.
   * @param supersedes The definition that this supersedes or null.
   */
  public PropertyDefinition(String name, String reference, String supersedes) {
    super(name, reference, supersedes);
  }
}
