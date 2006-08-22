/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.util;

import org.w3c.dom.Document;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.net.URL;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Utility methods for working with files
 */
public class FileUtils {
  public static final String NEW_LINE = System.getProperty("line.separator");

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
   * @throws IOException
   */
  public static void createLocalCopyOfTextFile(final String url, final String targetFilename) throws IOException {
    final BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
    final FileWriter fileWriter = new FileWriter(targetFilename);
    String str;
    while ((str = in.readLine()) != null) {
      fileWriter.write(str + NEW_LINE);
    }
    in.close();
    fileWriter.close();
  }

  /**
   * Serialize a node to a given file.
   * @param doc node of an xml doc
   * @param outputFileName outputFileName
   * @throws IOException
   */
  public static void serializeNode(final Document doc, final String outputFileName) throws IOException {
    final XMLSerializer serializer = new XMLSerializer();
    serializer.setOutputCharStream(new FileWriter(outputFileName));
    serializer.serialize(doc);
  }

  /**
   * Is this a syntactically valid URL.
   * @param url url
   * @return true if it a url
   */
  public static boolean isURL(final String url) {
    return url.startsWith("http");
  }

  /**
   * Gets all the text content from the given url. It is expected that the url will have all content as a text type.
   * @param bodyUrl bodyUrl
   * @throws IOException
   * @return the whole content from the url
   */
  public static String getTextFromUrl(final String bodyUrl) throws IOException {
    final StringBuilder sb = new StringBuilder();
    final URL url = new URL(bodyUrl);

    // Read all the text returned by the server
    final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
    String line;
    while ((line = in.readLine()) != null) {
      // line is one line of text; readLine() strips the newline character(s)
      sb.append(line).append(NEW_LINE);
    }
    in.close();

    return sb.toString().trim();
  }
}
