/* $$HeadURL::                                                                                               $$                                                                        $
 * $$Id ImageStorageService.java 2007-06-06 $$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;

/**
 * User: jonnie
 * Date: Jun 6, 2007
 * Time: 9:05:11 PM
 */

public class ImageStorageService {
  private static final int DEFAULT_BUFFER_SIZE = 33554432;

  private final ByteArrayOutputStream buffer;

  public ImageStorageService() {
    this(DEFAULT_BUFFER_SIZE);
  }

  public ImageStorageService(final int numberOfBytes) {
    buffer = new ByteArrayOutputStream(numberOfBytes);
  }

 /**
  * Retrieves the contents of the URL and stores them into memory for later retrieval by clients.
  * @param url - the url from which the content is to be obtained.
  * @throws ImageStorageServiceException
  */
  public void captureImage(final URL url) throws ImageStorageServiceException {
    final ImageRetrievalService imageRetrievalService = new ImageRetrievalService();

    try {
      final InputStream in = url.openStream();
      imageRetrievalService.transferImage(in,buffer);
    } catch (IOException e) {
      throw new ImageStorageServiceException("",e);
    } catch (ImageRetrievalServiceException e) {
      throw new ImageStorageServiceException("",e);
    }
  }

  public byte[] getBytes() {
    return buffer.toByteArray();
  }
}
