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

package org.plos.util;

import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for working with files
 */
public class FileUtils {
  public static final String NEW_LINE = System.getProperty("line.separator");
  private static Map<String, String> mimeTypeMap;
  private static final ConfigurableMimeFileTypeMap mimetypesFileTypeMap = new ConfigurableMimeFileTypeMap();

  //TODO: is this still required
  private static final Map<String, String> customMimeTypes = new HashMap<String, String>();

  static {
    customMimeTypes.put("xml", "text/xml");
  }

  /**
   * Return the filename from the given absolute path or url
   * @param absolutePath absolutePath
   * @return filename
   */
  public static String getFileName(final String absolutePath) {
    int lastIndex = absolutePath.lastIndexOf("/");
    if (lastIndex < 0) {
      lastIndex = absolutePath.lastIndexOf("\\");
    }
    return absolutePath.substring(lastIndex + 1);
  }

  /**
   * Create a local text copy of the url.
   * @param url url
   * @param targetFilename targetFilename
   * @throws IOException IOException
   */
  public static void createLocalCopyOfTextFile(final String url, final String targetFilename) throws IOException {
    final FileWriter fileWriter = new FileWriter(targetFilename);
    fileWriter.write(getTextFromUrl(url));
    fileWriter.close();
  }

  /**
   * Is this a http like URL.
   * @param url url
   * @return true if it a url
   */
  public static boolean isHttpURL(final String url) {
    return url.startsWith("http://") || url.startsWith("https://") ;
  }

  /**
   * Gets all the text content from the given url. It is expected that the url will have all content as a text type.
   * @param url url
   * @return the whole content from the url
   * @throws IOException IOException
   */
  public static String getTextFromUrl(final String url) throws IOException {
    // Read all the text returned by the server
    return getTextFromCharStream(new URL(url).openStream());
  }

  /**
   * Return the text from a character stream
   * @param inputStream inputStream
   * @return character text
   * @throws IOException IOException
   */
  public static String getTextFromCharStream(final InputStream inputStream) throws IOException {
    final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

    final StringBuilder sb = new StringBuilder();
    final char[] cbuf = new char[1024];
    int numRead;
    while (((numRead = in.read(cbuf)) >= 0)) {
      sb.append(cbuf, 0, numRead);
    }
    in.close();

    return sb.toString();
  }

  /**
   * Return the mime-type(content type) for a given file name
   * @param filename filename
   * @return the mime type
   */
  public static String getContentType(final String filename) {
    //TODO: Is custom still required
    final String customMimeType = customMimeTypes.get(filename.toLowerCase());
    if (null != customMimeType) {
      return customMimeType;
    }
    return mimetypesFileTypeMap.getContentType("a." + filename.toLowerCase());
  }

  /**
   * Return the first file extension that maps to a mimeType
   * @param mimeType mimeType
   * @return file extension
   * @throws java.io.IOException IOException
   */
  public static String getDefaultFileExtByMimeType(final String mimeType) throws IOException {
    if (null == mimeTypeMap) {
      populateAllMimeMappings();
    }

    final String extension = mimeTypeMap.get(mimeType.toLowerCase());
    return null == extension ? "" : extension;
  }

  public static void populateAllMimeMappings() throws IOException {
    final MimeTypeToFileExtMapper mimeTypeMapToFileExt = new MimeTypeToFileExtMapper();
    mimeTypeMap = mimeTypeMapToFileExt.getFileExtListByMimeType();
  }

  public static String escapeURIAsPath (final String inURI) {
    StringBuffer buf = new StringBuffer();

    int i = 0;
    while ( i < inURI.length() ) {
      char c = inURI.charAt(i);
      if ( ' ' != c && '/' != c && ':' != c)
        buf.append( c );
      i++;
    }
    return buf.toString();

  }

  public static String escapeURIAsPath (final URI inURI) {
    return (inURI == null) ? null : escapeURIAsPath(inURI.toString());
  }

}
