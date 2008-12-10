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
package org.topazproject.fedora.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;

/**
 * Fedora uploader client.
 *
 * @author Pradeep Krishnan
 */
public class Uploader {
  private static MultiThreadedHttpConnectionManager connectionManager =
    new MultiThreadedHttpConnectionManager();

  //
  private HttpClient       client;
  private String           serviceUri;

  static {
    // XXX: tune this
    connectionManager.getParams().setDefaultMaxConnectionsPerHost(100);
    connectionManager.getParams().setMaxTotalConnections(100);
  }

  /**
   * Creates a new Uploader object.
   *
   * @param serviceUri The uploader service uri
   * @param uname   The uploader user name or null
   * @param passwd  The uploader service passwd
   */
  public Uploader(String serviceUri, String uname, String passwd) {
    this.serviceUri   = serviceUri;
    client            = new HttpClient(connectionManager);

    if (uname != null) {
      client.getParams().setAuthenticationPreemptive(true);

      Credentials defaultcreds =
        new UsernamePasswordCredentials(uname, passwd);

      client.getState().setCredentials(AuthScope.ANY, defaultcreds);
    }
  }

  /**
   * Download from an uploaded URI.
   *
   * @param uploaded the URI that was returned by an upload
   * @return the inputstream to read
   * @throws IOException on an error
   */
  public InputStream download(String uploaded) throws IOException {
     GetMethod get = new GetMethod(serviceUri + "?" + uploaded);

     int resultCode = client.executeMethod(get);

     if (resultCode != 200)
       throw new IOException(HttpStatus.getStatusText(resultCode) + ":"
                             + replaceNewlines(get.getResponseBodyAsString(), " "));

     return get.getResponseBodyAsStream();
  }

  /**
   * Uploads a file to fedora.
   *
   * @param file the file to upload
   *
   * @return Returns a uri that can be used in setting up a data-stream for a fedora object.
   *
   * @throws IOException on an error
   */
  public String upload(File file) throws IOException {
    return upload(new FilePart("file", file));
  }

  /**
   * Uploads a byte array to fedora.
   *
   * @param bytes the bytes to upload.
   *
   * @return Returns a uri that can be used in setting up a data-stream for a fedora object.
   *
   * @throws IOException on an error
   */
  public String upload(byte[] bytes) throws IOException {
    return upload(new FilePart("file", new ByteArrayPartSource("byte-array", bytes)));
  }

  /**
   * Uploads the contents of a fixed length input stream to fedora.
   *
   * @param in the input stream to upload.
   * @param length the length of the input stream.
   *
   * @return Returns a uri that can be used in setting up a data-stream for a fedora object.
   *
   * @throws IOException on an error
   */
  public String upload(final InputStream in, final long length)
                throws IOException {
    final int markLength = 20000; // Auth errors will probably be reported before this

    return upload(new FilePart("file",
                               new PartSource() {
        private boolean marked = false;

        public InputStream createInputStream() throws IOException {
          if (marked)
            in.reset();

          in.mark(markLength);
          marked = true;

          return in;
        }

        public String getFileName() {
          return "fixed-length-input-stream";
        }

        public long getLength() {
          return length;
        }
      }) {
        public boolean isRepeatable() {
          return in.markSupported() && super.isRepeatable();
        }
      });
  }

  /**
   * Uploads the contents of an input stream. Copies to a local file before upload. Use only if any
   * of the other upload methods cannot be used.
   *
   * @param in the input stream to upload
   *
   * @return Returns a uri that can be used in setting up a data-stream for a fedora object.
   *
   * @throws IOException on an error
   */
  public String upload(InputStream in) throws IOException {
    FileOutputStream out      = null;
    File             tempFile = null;

    try {
      byte[] buf = new byte[4096];
      int    len;

      tempFile   = File.createTempFile("fedora-upload-", null);
      out        = new FileOutputStream(tempFile);

      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }

      out.close();
      out = null;

      return upload(tempFile);
    } finally {
      try {
        if (in != null)
          in.close();
      } catch (Throwable t) {
      }

      try {
        if (out != null)
          out.close();
      } catch (Throwable t) {
      }

      try {
        tempFile.delete();
      } catch (Throwable t) {
      }
    }
  }

  private String upload(Part part) throws IOException {
    PostMethod post = new PostMethod(serviceUri);
    post.setRequestEntity(new MultipartRequestEntity(new Part[]{part},post.getParams()));

    try {
      int resultCode = client.executeMethod(post);

      if (resultCode != 201)
        throw new IOException(HttpStatus.getStatusText(resultCode) + ":"
                              + replaceNewlines(post.getResponseBodyAsString(), " "));

      return replaceNewlines(post.getResponseBodyAsString(), "");
    } finally {
      post.releaseConnection();
    }
  }

  private static String replaceNewlines(String in, String replaceWith) {
    return in.replaceAll("\r", replaceWith).replaceAll("\n", replaceWith);
  }
}
