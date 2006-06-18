package org.topazproject.ws.annotation.service;

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
   */
  protected void setUp() throws ServiceException, RemoteException {
    URL url;

    try {
      url =
        new URL("http://localhost:9998/ws-annotation-webapp-0.1/services/AnnotationServicePort");
    } catch (MalformedURLException e) {
      throw new Error(e);
    }

    AnnotationServiceLocator locator = new AnnotationServiceLocator();
    locator.setMaintainSession(true);
    service = locator.getAnnotationServicePort(url);
  }

  /**
   * Runs all tests
   *
   * @throws RemoteException on an error from the service
   */
  public void testAll() throws RemoteException {
    //basicAnnotationTest();
  }

  private void basicAnnotationTest() throws RemoteException {
    String   subject     = "foo:bar";
    String   annotation  = "annotation:id#42";
    String[] annotations = service.listAnnotations(subject);

    try {
      for (int i = 0; i < annotations.length; i++)
        service.deleteAnnotation(annotations[i]);
    } catch (NoSuchIdException nsie) {
      fail("Unexpected NoSuchIdException");
    }

    annotations = service.listAnnotations(subject);
    assertTrue("Expected empty list of annotations, got " + annotations.length,
               annotations.length == 0);

    boolean gotExc = false;

    try {
      String info = service.getAnnotationInfo(annotation);
    } catch (NoSuchIdException nsie) {
      gotExc = true;
    }

    assertTrue("Failed to get expected NoSuchIdException", gotExc);

    gotExc = false;

    try {
      service.setAnnotationInfo(annotation, "hello");
    } catch (NoSuchIdException nsie) {
      gotExc = true;
    }

    assertTrue("Failed to get expected NoSuchIdException", gotExc);

    gotExc = false;

    try {
      service.deleteAnnotation(annotation);
    } catch (NoSuchIdException nsie) {
      gotExc = true;
    }

    assertTrue("Failed to get expected NoSuchIdException", gotExc);

    annotation   = service.createAnnotation(subject, "hello");

    annotations = service.listAnnotations(subject);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0] + "'",
                 annotations[0], annotation);

    String info = service.getAnnotationInfo(annotation);
    assertEquals("Info mismatch, got '" + info + "'", info, "hello");

    service.setAnnotationInfo(annotation, "bye");
    info = service.getAnnotationInfo(annotation);
    assertEquals("Info mismatch, got '" + info + "'", info, "bye");

    service.setAnnotationState(annotation, 42);
    annotations = service.listAnnotations(42);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0] + "'",
                 annotations[0], annotation);

    service.deleteAnnotation(annotation);
    gotExc = false;

    try {
      service.deleteAnnotation(annotation);
    } catch (NoSuchIdException nsie) {
      gotExc = true;
    }

    assertTrue("Failed to get expected NoSuchIdException", gotExc);

    annotations = service.listAnnotations(subject);
    assertTrue("Expected no annotations, got " + annotations.length, annotations.length == 0);
  }
}
