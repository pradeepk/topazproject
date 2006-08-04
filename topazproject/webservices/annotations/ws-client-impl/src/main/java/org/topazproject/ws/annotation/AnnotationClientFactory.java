package org.topazproject.ws.annotation;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpSession;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.apache.commons.configuration.Configuration;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

/**
 * Factory class to generate Annotation web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationClientFactory {
  /**
   * Creates an Annotation service client stub..
   *
   * @param service the service configuration (url and credentials)
   *
   * @return Returns an Annotation service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static Annotation create(ProtectedService service)
                           throws MalformedURLException, ServiceException {
    URL                      url     = new URL(service.getServiceUri());
    AnnotationServiceLocator locator = new AnnotationServiceLocator();

    locator.setMaintainSession(true);

    Annotation annotation = locator.getAnnotationServicePort(url);

    if (service.requiresUserNamePassword()) {
      Stub stub = (Stub) annotation;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }

    return annotation;
  }

  /**
   * Creates a client that does not require any authentication.
   *
   * @param annotationServiceUri the uri for annotation service
   *
   * @return Returns an Annotation service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static Annotation create(String annotationServiceUri)
                           throws MalformedURLException, ServiceException {
    return create(ProtectedServiceFactory.createService(annotationServiceUri, null, null, false));
  }
}
