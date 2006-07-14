package org.topazproject.ws.annotation.service;

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
  private Annotation service;

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
        AnnotationClientFactory.create("http://localhost:9998/ws-annotation-webapp-0.1/services/AnnotationServicePort");
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
    String[] annotations = service.listAnnotations(subject, null, true);

    try {
      for (int i = 0; i < annotations.length; i++)
        service.deleteAnnotation(annotations[i], true);
    } catch (NoSuchIdException nsie) {
      fail("Unexpected NoSuchIdException");
    }

    annotations = service.listAnnotations(subject, null, true);
    assertTrue("Expected empty list of annotations, got " + annotations.length,
               annotations.length == 0);

    boolean gotExc = false;

    try {
      String info = service.getAnnotation(annotation);
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

    annotations = service.listAnnotations(subject, null, true);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0] + "'",
                 annotations[0], annotation);

    String info = service.getAnnotation(annotation);

    //System.out.println(info);
    int    i1 = info.indexOf("<a:body>") + 8;
    int    i2 = info.indexOf("</a:body>");
    String s  = info.substring(i1, i2);
    assertEquals("<a:body> mismatch, got '" + s + "'", s, bodyUrl);

    i1   = info.indexOf("<a:context>") + 11;
    i2   = info.indexOf("</a:context>");
    s    = info.substring(i1, i2);
    assertEquals("<a:context> mismatch, got '" + s + "'", s, hackContext);

    String superseded = annotation;

    try {
      annotation =
        service.createAnnotation(null, subject, context, annotation, "text/plain;charset=utf-8",
                                 bodyContent.getBytes("utf-8"));
    } catch (java.io.UnsupportedEncodingException e) {
      throw new Error(e);
    }

    info   = service.getAnnotation(annotation);

    //System.out.println(info);
    i1   = info.indexOf("<a:body>") + 8;
    i2   = info.indexOf("</a:body>");
    s    = info.substring(i1, i2);

    try {
      //System.out.println(info);
      s = (new BufferedReader(new InputStreamReader((new URL(s)).openStream()))).readLine();

      //System.out.println(info);
    } catch (IOException e) {
      throw new RemoteException("failed to read annotation body", e);
    }

    assertEquals("<a:body> mismatch, got '" + s + "'", s, bodyContent);

    i1   = info.indexOf("<a:context>") + 11;
    i2   = info.indexOf("</a:context>");
    s    = info.substring(i1, i2);
    assertEquals("<a:context> mismatch, got '" + s + "'", s, context);

    annotations = service.listAnnotations(subject, null, true);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0] + "'",
                 annotations[0], annotation);

    annotations = service.getPrecedingAnnotations(annotation, true);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + superseded + "', got '" + annotations[0] + "'",
                 annotations[0], superseded);

    annotations = service.getPrecedingAnnotations(superseded, true);
    assertTrue("Expected zero annotation, got " + annotations.length, annotations.length == 0);

    annotations = service.getLatestAnnotations(annotation, true);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0] + "'",
                 annotations[0], annotation);

    annotations = service.getLatestAnnotations(superseded, true);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0] + "'",
                 annotations[0], annotation);

    service.setAnnotationState(annotation, 42);
    annotations = service.listAnnotations(42);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0] + "'",
                 annotations[0], annotation);

    service.deleteAnnotation(annotation, false);

    annotations = service.listAnnotations(subject, null, true);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + superseded + "', got '" + annotations[0] + "'",
                 annotations[0], superseded);

    service.deleteAnnotation(superseded, true);

    gotExc = false;

    try {
      service.deleteAnnotation(annotation, true);
    } catch (NoSuchIdException nsie) {
      gotExc = true;
    }

    assertTrue("Failed to get expected NoSuchIdException", gotExc);

    annotations = service.listAnnotations(subject, null, true);
    assertTrue("Expected zero annotations, got " + annotations.length, annotations.length == 0);
  }
}
