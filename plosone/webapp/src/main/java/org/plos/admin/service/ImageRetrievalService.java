/* $$HeadURL::                                                                                               $$                                                                        $
 * $$Id ImageRetrievalService.java 2007-05-31 $$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.service;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * User: jonnie
 * Date: May 31, 2007
 * Time: 8:26:48 PM
 */

public class ImageRetrievalService {
  public ImageRetrievalService() {}

 /**
  * This method is used to copy image data from an input stream to an output stream.
  * @param in - the input stream to read from
  * @param out - the output stream to write to
  * @throws ImageRetrievalServiceException
  */
  public void transferImage(final InputStream in,final OutputStream out) throws ImageRetrievalServiceException {
    try {
      int bytesReadTotal = 0;

      int length = 8192;
      byte[] buffer = new byte[length];

      int bytesRead;

      do {
        bytesRead = 0;

        int offset = 0;
        int remainingBytes = (length - offset);

        while (bytesRead != -1) {
          offset = offset + bytesRead;
          remainingBytes = remainingBytes - bytesRead;
          assert(remainingBytes + offset == length);

          bytesReadTotal = bytesReadTotal + bytesRead;

          // buffer is full => exit inner loop.
          if (remainingBytes <= 0) {
            assert(remainingBytes == 0);
            break;
          }

          bytesRead = in.read(buffer,offset,remainingBytes);
        }

        // buffer is as full as possible.
        // placing contents into output-stream and then resume outer loop.
        remainingBytes = offset;
        offset = 0;
        out.write(buffer,offset,remainingBytes);
      } while (bytesRead != -1);
    } catch (IOException e) {
      System.out.println(e);
      throw new ImageRetrievalServiceException("",e);
    }
  }
}
