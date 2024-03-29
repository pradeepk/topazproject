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

import org.topazproject.otm.id.IdentifierGenerator;

/**
 * The definition for an Id field.
 *
 * @author Pradeep Krishnan
 */
public class IdDefinition extends PropertyDefinition {
  private final IdentifierGenerator gen;

  /**
   * Creates a new IdDefinition object.
   *
   * @param name   The name of this definition.
   * @param gen id generator or null
   */
  public IdDefinition(String name, IdentifierGenerator gen) {
    super(name);
    this.gen = gen;
  }

  /**
   * Gets the generator for this field
   *
   * @return the generator to use for this field (or null if there isn't one)
   */
  public IdentifierGenerator getGenerator() {
    return gen;
  }
}
