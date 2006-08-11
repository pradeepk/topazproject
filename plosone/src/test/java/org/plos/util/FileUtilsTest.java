/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.util;

import junit.framework.TestCase;

import java.net.URISyntaxException;

public class FileUtilsTest extends TestCase {
  public void testFileNameExtraction() throws URISyntaxException {
    assertEquals("TransformerFactory.html", FileUtils.getFileName("http://java.sun.com/j2se/1.4.2/docs/api/javax/xml/transform/TransformerFactory.html"));
    assertEquals("TransformerFactory.txt", FileUtils.getFileName("/1.4.2/docs/api/javax/xml/transform/TransformerFactory.txt"));
    assertEquals("TransformerFactory.txt", FileUtils.getFileName("C:\\1.4.2\\docs\\api\\javax\\xml\\transform\\TransformerFactory.txt"));
  }

  public void testValidateUrl() {
    assertTrue(FileUtils.isURL("http://java.sun.com/j2se/1.4.2/docs/api/javax/xml/transform/TransformerFactory.html"));
    assertFalse(FileUtils.isURL("/java.sun.com/j2se/1.4.2/docs/api/javax/xml/transform/TransformerFactory.html"));
    assertFalse(FileUtils.isURL("C:\\1.4.2\\docs\\api\\javax\\xml\\transform\\TransformerFactory.txt"));
  }
}
