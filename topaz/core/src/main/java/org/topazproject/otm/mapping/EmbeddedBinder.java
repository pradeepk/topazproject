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
package org.topazproject.otm.mapping;

/**
 * PropertyBinder that binds an embedded entity.
 *
 * @author Pradeep Krishnan
 */
public interface EmbeddedBinder extends PropertyBinder {
  /**
   * Promote an embedded field binder up to the same level as this so that it can be added to
   * collections that contain mappers at the same level as this.
   *
   * @param b the binder to promote
   *
   * @return the promoted binder
   */
  public PropertyBinder promote(PropertyBinder b);
}
