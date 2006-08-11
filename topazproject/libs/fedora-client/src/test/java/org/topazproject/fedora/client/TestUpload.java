/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedora.client;

import java.io.ByteArrayInputStream;

import java.net.URI;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import junit.framework.TestCase;

/**
 * Test upload to Fedora.
 *
 * @author Pradeep Krishnan
 */
public class TestUpload extends TestCase {
  private static String uri    = "http://localhost:9090/fedora/management/upload";
  private static String uname  = "fedoraAdmin";
  private static String passwd = "fedoraAdmin";

  //
  private static ProtectedService svc =
    ProtectedServiceFactory.createService(uri, uname, passwd, true);
  private Uploader                uploader;
  private boolean                 skip = true;

  /**
   * Creates a new TestUpload object.
   */
  public TestUpload() {
    //skip = false;
  }

  /**
   * Sets up the tests. Creats the stub.
   *
   * @throws Exception on failure
   */
  public void setUp() throws Exception {
    if (skip)
      return;

    uploader = new Uploader(svc);
  }

  /**
   * Tests upload
   *
   * @throws Exception on failure
   */
  public void testUploadBytes() throws Exception {
    if (skip)
      return;

    String s = uploader.upload(new byte[100000]);
    URI    u = new URI(s);
    assertTrue(u.isAbsolute());
  }

  /**
   * Tests upload
   *
   * @throws Exception on failure
   */
  public void testUploadStream() throws Exception {
    if (skip)
      return;

    String s = uploader.upload(new ByteArrayInputStream(new byte[100000]));
    URI    u = new URI(s);
    assertTrue(u.isAbsolute());
  }

  /**
   * Tests upload
   *
   * @throws Exception on failure
   */
  public void testUploadFixedStream() throws Exception {
    if (skip)
      return;

    String s = uploader.upload(new ByteArrayInputStream(new byte[100000]), 100000);
    URI    u = new URI(s);
    assertTrue(u.isAbsolute());
  }
}
