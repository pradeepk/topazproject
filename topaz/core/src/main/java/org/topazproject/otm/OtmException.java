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

package org.topazproject.otm;

/**
 * The base exception class for all exceptions thrown by the OTM layer.
 *
 * @author Ronald Tschalär
 */
public class OtmException extends RuntimeException {
  /**
   * Create a new exception instance with a message.
   *
   * @param msg the details about the exception
   */
  public OtmException(String msg) {
    super(msg);
  }

  /**
   * Create a new chained exception instance with a message.
   *
   * @param msg   the details about the exception
   * @param cause the underlying exception that caused this exception
   */
  public OtmException(String msg, Throwable cause) {
    super(msg);
    initCause(cause);
  }
}
