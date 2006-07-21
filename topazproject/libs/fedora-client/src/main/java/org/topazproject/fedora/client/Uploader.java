package org.topazproject.fedora.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;

import org.topazproject.authentication.ProtectedService;

/**
 * Fedora uploader client.
 *
 * @author Pradeep Krishnan
 */
public class Uploader {
  private static MultiThreadedHttpConnectionManager connectionManager =
    new MultiThreadedHttpConnectionManager();

  //
  private HttpClient client;
  private String     uploaderUri;

  static {
    // xxx: tune this
    connectionManager.setMaxConnectionsPerHost(100);
    connectionManager.setMaxTotalConnections(100);
  }

  /**
   * Creates a new Uploader object.
   *
   * @param service The uploader service configuration
   */
  public Uploader(ProtectedService service) {
    this.uploaderUri   = service.getServiceUri();
    client             = new HttpClient(connectionManager);

    if (service.requiresUserNamePassword()) {
      client.getParams().setAuthenticationPreemptive(true);

      Credentials defaultcreds =
        new UsernamePasswordCredentials(service.getUserName(), service.getPassword());

      client.getState().setCredentials(AuthScope.ANY, defaultcreds);
    }
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
   * Uploads the contents of an input stream. Copies to a local file before upload.
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
    MultipartPostMethod post = new MultipartPostMethod(uploaderUri);
    post.addPart(part);

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
