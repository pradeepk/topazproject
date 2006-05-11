
package org.topazproject.ws.admin.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 *
 */
public class AnnotationServiceTest extends TestCase {
  private Annotation service;

  public AnnotationServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    URL url =
        new URL("http://localhost:9999/ws-annotation-admin-webapp-0.1/services/AnnotationServicePort");
    AnnotationServiceLocator locator = new AnnotationServiceLocator();
    service = locator.getAnnotationServicePort(url);
  }

  public void testAll() throws RemoteException {
    basicAnnotationTest();
  }

  private void basicAnnotationTest() throws RemoteException {
    String subject = "foo";
    String annotation = "a1";
    String[] annotations = service.listAnnotations(subject);
    assertTrue("Expected empty list of annotations, got " + annotations.length, annotations.length == 0);

    boolean gotExc = false;
    try {
      String info = service.getAnnotationInfo(subject,annotation);
    } catch (NoSuchIdException nsie) {
      gotExc = true;
    }
    assertTrue("Failed to get expected NoSuchIdException", gotExc);

    gotExc = false;
    try {
      service.setAnnotationInfo(subject,annotation,"hello");
    } catch (NoSuchIdException nsie) {
      gotExc = true;
    }
    assertTrue("Failed to get expected NoSuchIdException", gotExc);

    gotExc = false;
    try {
      service.deleteAnnotation(subject,annotation);
    } catch (NoSuchIdException nsie) {
      gotExc = true;
    }
    assertTrue("Failed to get expected NoSuchIdException", gotExc);

    service.createAnnotation(subject,annotation);

    annotations = service.listAnnotations(subject);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation +"', got '" + annotations[0] + "'", annotations[0], annotation);

    String info = service.getAnnotationInfo(subject,annotation);
    assertNull("Expected no info, got '" + info + "'", info);

    service.setAnnotationInfo(subject,annotation, "hello");
    info = service.getAnnotationInfo(subject,annotation);
    assertEquals("Info mismatch, got '" + info + "'", info, "hello");

    service.deleteAnnotation(subject,annotation);
    gotExc = false;
    try {
      service.deleteAnnotation(subject,annotation);
    } catch (NoSuchIdException nsie) {
      gotExc = true;
    }
    assertTrue("Failed to get expected NoSuchIdException", gotExc);

    annotations = service.listAnnotations(subject);
    assertTrue("Expected no annotations, got " + annotations.length, annotations.length == 0);
    
  }
}
