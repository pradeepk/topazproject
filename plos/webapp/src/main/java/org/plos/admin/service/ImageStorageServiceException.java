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

package org.plos.admin.service;

/**
 * This is the exception thrown by the ImageStorageService to indicate the
 * failure of an operation.
 *
 * @author jonnie
 */
public class ImageStorageServiceException extends Exception {

  /**
   * Creates an ImageStorageServiceException to be thrown to the environment
   *
   * @param message a textual indication of the cause of failure
   * @param cause   the Throwable object received by the caller which forced
   *                the creator to throw this exception.
   */
  public ImageStorageServiceException(final String message,final Throwable cause) {
    super(message,cause);
  }

  /**
   * Creates an ImageStorageServiceException to be thrown to the environment
   *
   * @param cause the Throwable object received by the caller which forced
   *              the creator to throw this exception.
   **/
  public ImageStorageServiceException(final Throwable cause) {
    super(cause);
  }
}
