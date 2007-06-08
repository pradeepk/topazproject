/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.service;

public class ImageStorageServiceException extends Exception {
  public ImageStorageServiceException(final String message,final Throwable cause) {
    super(message,cause);
  }

  public ImageStorageServiceException(final Throwable cause) {
    super(cause);
  }
}
