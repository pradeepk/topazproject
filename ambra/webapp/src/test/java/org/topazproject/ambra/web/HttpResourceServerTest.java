/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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

package org.topazproject.ambra.web;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.assertEquals;

import java.net.URL;
import java.io.IOException;

/**
 * @author Dragisa Krsmanovic
 * TODO: Test ranges
 */
public class HttpResourceServerTest {
  private HttpResourceServer server;
  private MockHttpServletResponse responseMock;
  private MockHttpServletRequest requestMock;
  private static final String EXPECTED_TEXT = "Hello World !";
  private static final String EXPECTED_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<test>Hello World !</test>";
  private HttpResourceServer.Resource txtResource;
  private HttpResourceServer.Resource xmlResource;

  @BeforeClass
  protected void setUpClass() throws Exception {
    txtResource = new HttpResourceServer.URLResource(this.getClass().getResource("/TestResource.txt"));
    xmlResource = new HttpResourceServer.URLResource(this.getClass().getResource("/TestResource.xml"));
  }

  @BeforeMethod
  protected void setUpMethod() throws Exception {
    server = new HttpResourceServer();
    responseMock = new MockHttpServletResponse();
    requestMock = new MockHttpServletRequest();
  }

  @Test
  public void testServerResourceTxt() throws IOException {
    server.serveResource(requestMock, responseMock, txtResource);
    assertEquals(responseMock.getContentAsString(), EXPECTED_TEXT, "Wrong content served");
    assertEquals(responseMock.getContentType(), "text/plain", "Wrong content type");
    int length = EXPECTED_TEXT.getBytes().length;
    assertEquals(responseMock.getContentLength(), length, "Wrong content length");
  }

  @Test
  public void testServerResourceXml() throws IOException {
    server.serveResource(requestMock, responseMock, xmlResource);
    assertEquals(responseMock.getContentAsString(), EXPECTED_XML, "Wrong content served");
    assertEquals(responseMock.getContentType(), "application/xml", "Wrong content type");
    assertEquals(responseMock.getContentLength(), EXPECTED_XML.getBytes().length,
        "Wrong content length");
  }

  @Test
  public void testServerResourceForHead() throws IOException {
    requestMock.setMethod("HEAD");
    server.serveResource(requestMock, responseMock, txtResource);
    assertEquals(responseMock.getContentAsString(), "", "Content is not empty");
    assertEquals(responseMock.getContentType(), "text/plain", "Wrong content type");
    assertEquals(responseMock.getContentLength(), EXPECTED_TEXT.getBytes().length,
        "Wrong content length");
  }

  @Test
  public void testServerResourceWithContent() throws IOException {
    server.serveResource(requestMock, responseMock, true, txtResource);
    assertEquals(responseMock.getContentAsString(), EXPECTED_TEXT, "Wrong content served");
    assertEquals(responseMock.getContentType(), "text/plain", "Wrong content type");
    assertEquals(responseMock.getContentLength(), EXPECTED_TEXT.getBytes().length,
        "Wrong content length");
  }

  @Test
  public void testServerResourceWithoutContent() throws IOException {
    server.serveResource(requestMock, responseMock, false, txtResource);
    assertEquals(responseMock.getContentAsString(), "", "Content is not empty");
    assertEquals(responseMock.getContentType(), "text/plain", "Wrong content type");
    assertEquals(responseMock.getContentLength(), EXPECTED_TEXT.getBytes().length,
        "Wrong content length");
  }
}
