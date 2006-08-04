/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.annotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 * Tests the Annotation web service.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationServiceTest extends TestCase {
  private Annotations service;

  /**
   * Creates a new AnnotationServiceTest object.
   *
   * @param testName name of this test
   */
  public AnnotationServiceTest(String testName) {
    super(testName);
  }

  /**
   * Sets up the test. Gets the client stub for making calls to the service.
   *
   * @throws ServiceException indicates an error in setting up the client stub
   * @throws RemoteException indicates an error in setting up the client stub
   * @throws Error DOCUMENT ME!
   */
  protected void setUp() throws ServiceException, RemoteException {
    try {
      service =
        AnnotationClientFactory.create("http://localhost:9997/ws-annotation-webapp-0.1/services/AnnotationServicePort");
    } catch (MalformedURLException e) {
      throw new Error(e);
    }
  }

  /**
   * Runs all tests
   *
   * @throws RemoteException on an error from the service
   */
  public void testAll() throws RemoteException {
    basicAnnotationTest();
  }

  private void basicAnnotationTest() throws RemoteException {
    String   subject     = "foo:bar";
    String   context     = "foo:bar##xpointer(id(\"Main\")/p[2])";
    String   hackContext = "$user/$annotates/$s/$created/\\'\"\'";
    String   annotation  = "annotation:id#42";
    String   bodyUrl     = "http://gandalf.topazproject.org";
    String   bodyContent = "This is a comment on foo:bar";
    AnnotationInfo[] annotations = service.listAnnotations(subject, null);

    try {
      for (int i = 0; i < annotations.length; i++)
        service.deleteAnnotation(annotations[i].getId(), true);
    } catch (NoSuchIdException nsie) {
      fail("Unexpected NoSuchIdException");
    }

    annotations = service.listAnnotations(subject, null);
    assertTrue("Expected empty list of annotations, got " + annotations.length,
               annotations.length == 0);

    boolean gotExc = false;

    try {
      AnnotationInfo info = service.getAnnotationInfo(annotation);
    } catch (NoSuchIdException nsie) {
      gotExc = true;
    }

    assertTrue("Failed to get expected NoSuchIdException", gotExc);

    gotExc = false;

    try {
      service.deleteAnnotation(annotation, false);
    } catch (NoSuchIdException nsie) {
      gotExc = true;
    }

    assertTrue("Failed to get expected NoSuchIdException", gotExc);

    gotExc = false;

    try {
      annotation = service.createAnnotation(null, subject, null, null, "bad:url/{context}");
    } catch (Exception e) {
      gotExc = true;
    }

    assertTrue("Failed to get expected IllegalArgumentException", gotExc);

    annotation   = service.createAnnotation(null, subject, hackContext, null, bodyUrl);

    annotations = service.listAnnotations(subject, null);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0].getId() 
        + "'",
                 annotations[0].getId(), annotation);

    AnnotationInfo info = service.getAnnotationInfo(annotation);
    assertEquals(info.getId(), annotations[0].getId());
    assertEquals(info.getType(), annotations[0].getType());
    assertEquals(info.getAnnotates(), annotations[0].getAnnotates());
    assertEquals(info.getContext(), annotations[0].getContext());
    assertEquals(info.getBody(), annotations[0].getBody());
    assertEquals(info.getSupersedes(), annotations[0].getSupersedes());
    assertEquals(info.getCreator(), annotations[0].getCreator());
    assertEquals(info.getCreated(), annotations[0].getCreated());

    assertEquals(info.getBody(), bodyUrl);
    assertEquals(info.getAnnotates(), subject);
    assertEquals(info.getContext(), hackContext);

    String superseded = annotation;

    try {
      annotation =
        service.createAnnotation(null, subject, context, annotation, "text/plain;charset=utf-8",
                                 bodyContent.getBytes("utf-8"));
    } catch (java.io.UnsupportedEncodingException e) {
      throw new Error(e);
    }

    info   = service.getAnnotationInfo(annotation);

    String s;
    try {
      s = (new BufferedReader(new InputStreamReader((new URL(info.getBody())).openStream()))).readLine();

    } catch (IOException e) {
      throw new RemoteException("failed to read annotation body", e);
    }

    assertEquals("<a:body> mismatch, got '" + s + "'", s, bodyContent);

    assertEquals("<a:context> mismatch, got '" + s + "'", info.getContext(), context);

    annotations = service.listAnnotations(subject, null);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0].getId() + "'",
                 annotations[0].getId(), annotation);

    annotations = service.getPrecedingAnnotations(annotation);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + superseded + "', got '" + annotations[0].getId() + "'",
                 annotations[0].getId(), superseded);

    annotations = service.getPrecedingAnnotations(superseded);
    assertTrue("Expected zero annotation, got " + annotations.length, annotations.length == 0);

    annotations = service.getLatestAnnotations(annotation);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0].getId() + "'",
                 annotations[0].getId(), annotation);

    annotations = service.getLatestAnnotations(superseded);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0].getId() + "'",
                 annotations[0].getId(), annotation);

    service.setAnnotationState(annotation, 42);
    String[] ids = service.listAnnotations(42);
    assertTrue("Expected one annotation, got " + ids.length, ids.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + ids[0] + "'",
                 ids[0], annotation);

    service.deleteAnnotation(annotation, false);

    annotations = service.listAnnotations(subject, null);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + superseded + "', got '" + annotations[0].getId() + "'",
                 annotations[0].getId(), superseded);

    service.deleteAnnotation(superseded, true);

    gotExc = false;

    try {
      service.deleteAnnotation(annotation, true);
    } catch (NoSuchIdException nsie) {
      gotExc = true;
    }

    assertTrue("Failed to get expected NoSuchIdException", gotExc);

    annotations = service.listAnnotations(subject, null);
    assertTrue("Expected zero annotations, got " + annotations.length, annotations.length == 0);
  }
}
