/* $HeadURL::                                                                                     $
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
package org.plos.article.util;

/**
 * Signifies that the requested object does not exist.
 *
 * @author Ronald Tschalär
 * @author Eric Brown
 */
public class NoSuchObjectIdException extends Exception {
  private final String id;

  /**
   * Create a new exception instance with a default exception message.
   *
   * @param id      the (non-existant) id
   */
  public NoSuchObjectIdException(String id) {
    this(id, "", null);
  }

  /**
   * Create a new exception instance.
   *
   * @param id      the (non-existant) id
   * @param message the exception message
   */
  public NoSuchObjectIdException(String id, String message) {
    this(id, message, null);
  }

  /**
   * Create a new exception instance.
   *
   * @param id      the (non-existant) id
   * @param message the exception message
   * @param cause   the exception cause
   */
  public NoSuchObjectIdException(String id, String message, Throwable cause) {
    super("(id=" + id + ")" + message, cause);
    this.id = id;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }
}
