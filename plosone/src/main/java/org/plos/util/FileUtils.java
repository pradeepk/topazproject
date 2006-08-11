/* $HeadURL::                                                                            $
 * $Id$
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
}
