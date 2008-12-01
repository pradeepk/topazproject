/* $HeadURL::                                                                                     $
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

package org.topazproject.ambra.article.service;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;

import org.apache.commons.codec.binary.Base64;

/**
 * Helper class to generate an XML descript of a zip archive. The DTD of the result is as
 * follows:
 * <pre>
 *   &lt;!ELEMENT ZipInfo (ZipEntry*) &gt;
 *   &lt;!ATTLIST ZipInfo
 *       name           CDATA          #IMPLIED &gt;
 *
 *   &lt;!ELEMENT ZipEntry (Comment?, Extra?) &gt;
 *   &lt;!ATTLIST ZipEntry
 *       name           CDATA          #REQUIRED
 *       isDirectory    (true | false) "false"
 *       crc            CDATA          #IMPLIED
 *       size           CDATA          #IMPLIED
 *       compressedSize CDATA          #IMPLIED
 *       time           CDATA          #IMPLIED &gt;
 *
 *   &lt;!ELEMENT Comment (#PCDATA) &gt;
 *   &lt;!ELEMENT Extra   (#PCDATA) &gt;
 * </pre>
 * Example:
 * &lt;pre&gt;
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;!DOCTYPE ZipInfo SYSTEM "zip.dtd"&gt;
 * &lt;ZipInfo name="foo.zip"&gt;
 *   &lt;ZipEntry name="README.txt"/&gt;
 *   &lt;ZipEntry name="src" isDirectory="true"&gt;
 *     &lt;Extra&gt;iFkCB8w@w9==&lt;/Extra&gt;
 *   &lt;/ZipEntry&gt;
 *   &lt;ZipEntry name="src/java" isDirectory="true"&gt;
 *     &lt;Extra&gt;iFkCB8w@w9==&lt;/Extra&gt;
 *   &lt;/ZipEntry&gt;
 *   &lt;ZipEntry name="src/java/blah.java"&gt;
 *     &lt;Comment&gt;A test file&lt;/Comment&gt;
 *   &lt;/ZipEntry&gt;
 * &lt;/ZipInfo&gt;
 * &lt;/pre&gt;
 *
 * @author Ronald Tschalär
 */
public class Zip2Xml {

  /**
   * Generate a description of the given zip archive.
   *
   * @param zip  the zip archive to describe
   * @return the xml doc describing the archive (adheres to zip.dtd)
   * @throws IOException if an exception occurred reading the zip archive
   */
  public static String describeZip(Zip zip) throws IOException {
    StringBuilder res = new StringBuilder(500);
    res.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    res.append("<ZipInfo");
    if (zip.getName() != null)
      res.append(" name=\"").append(attrEscape(zip.getName())).append("\"");
    res.append(">\n");

    Enumeration<? extends ZipEntry> entries = zip.getEntries();
    while (entries.hasMoreElements())
      entry2xml(entries.nextElement(), res);

    res.append("</ZipInfo>\n");
    return res.toString();
  }

  /**
   * Generate a description for a single zip-entry.
   *
   * @param ze  the zip entry to describe.
   * @param buf the buffer to place the description into
   */
  private static void entry2xml(ZipEntry ze, StringBuilder buf) {
    buf.append("<ZipEntry name=\"").append(attrEscape(ze.getName())).append("\"");

    if (ze.isDirectory())
      buf.append(" isDirectory=\"true\"");
    if (ze.getCrc() >= 0)
      buf.append(" crc=\"").append(ze.getCrc()).append("\"");
    if (ze.getSize() >= 0)
      buf.append(" size=\"").append(ze.getSize()).append("\"");
    if (ze.getCompressedSize() >= 0)
      buf.append(" compressedSize=\"").append(ze.getCompressedSize()).append("\"");
    if (ze.getTime() >= 0)
      buf.append(" time=\"").append(ze.getTime()).append("\"");

    if (ze.getComment() != null || ze.getExtra() != null) {
      buf.append(">\n");

      if (ze.getComment() != null)
        buf.append("<Comment>").append(xmlEscape(ze.getComment())).append("</Comment>\n");
      if (ze.getExtra() != null)
        buf.append("<Extra>").append(base64Encode(ze.getExtra())).append("</Extra>\n");

      buf.append("</ZipEntry>\n");
    } else {
      buf.append("/>\n");
    }
  }

  private static String xmlEscape(String str) {
    return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
  }

  private static String attrEscape(String str) {
    return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;");
  }

  private static String base64Encode(byte[] data) {
    try {
      return new String(Base64.encodeBase64(data), "ISO-8859-1");
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);  // can't happen
    }
  }
}
