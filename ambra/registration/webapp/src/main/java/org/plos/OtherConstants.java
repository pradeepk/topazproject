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
package org.plos;

import java.util.Map;

/**
 * A place to be able to access all constants which don't belong anywhere else.
 */
public class OtherConstants {
  private Map map;

  /**
   * Getter for map.
   * @param key key
   * @return Value for map.
   */
  public Object getValue(final String key) {
    return map.get(key);
  }

  /**
   * Setter for property map.
   * @param map Value to map.
   */
  public void setAllConstants(final Map map) {
    this.map = map;
  }
}