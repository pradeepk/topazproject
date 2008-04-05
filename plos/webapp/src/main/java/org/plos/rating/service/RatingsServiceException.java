/* $HeadURL::                                                                                               $
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

package org.plos.rating.service;

/**
 * This is the exception thrown by the RatingsService to indicate the failure of an operation.
 *
 * @author jonnie
 */
public class RatingsServiceException extends Exception {

  /**
   * Creates a RatingsServiceException to be thrown to the environment.
   *
   * @param message a textual indication of the cause of failure
   * @param cause   the Throwable object received by the caller which forced
   *                the creator to throw this exception.
   */
  public RatingsServiceException(final String message,final Throwable cause) {
    super(message,cause);
  }

  /**
   * Creates a RatingsServiceException to be thrown to the environment.
   *
   * @param cause the Throwable object received by the caller which forced
   *              the creator to throw this exception.
   **/
  public RatingsServiceException(final Throwable cause) {
    super(cause);
  }

  /**
   * Creates a RatingsServiceException to be thrown to the environment.
   * WARNING: Does not chain
   *
   * @param message a textual indication of the cause of failure
   */
  public RatingsServiceException(final String message) {
    super(message);
  }
}
