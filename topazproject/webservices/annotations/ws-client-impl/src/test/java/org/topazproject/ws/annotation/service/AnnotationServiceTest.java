package org.topazproject.ws.annotation.service;

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

public class AnnotationServiceTest extends TestCase {
  private Annotation service;

  public AnnotationServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    URL                      url =
      new URL("http://localhost:9998/ws-annotation-webapp-0.1/services/AnnotationServicePort");
    AnnotationServiceLocator locator = new AnnotationServiceLocator();
    locator.setMaintainSession(true);
    service = locator.getAnnotationServicePort(url);
  }

  public void testAll() throws RemoteException {
    //basicAnnotationTest();
  }

  private void basicAnnotationTest() throws RemoteException {
    String   subject     = "foo:bar";
    String   annotation  = "annotaion:id#42";
    String[] annotations = service.listAnnotations(subject);
   
    try {
      for (int i = 0; i < annotations.length; i++)
        service.deleteAnnotation(annotations[i]);
    } catch (NoSuchIdException nsie){
      assertTrue("Unexpected NoSuchIdException", false);
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

    annotation = service.createAnnotation(subject, "hello");

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
