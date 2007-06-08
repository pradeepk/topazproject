/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.service;

/**
 * User: jonnie
 * Date: Jun 1, 2007
 * Time: 11:43:14 AM
 */

public class ImageRetrievalServiceException extends Exception {
  public ImageRetrievalServiceException(final String message,final Throwable cause) {
    super(message,cause);
  }

  public ImageRetrievalServiceException(final Throwable cause) {
    super(cause);
  }
}
