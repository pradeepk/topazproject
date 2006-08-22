/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.annotation.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.ws.annotation.AnnotationClientFactory;
import org.topazproject.ws.annotation.Annotations;
import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.annotation.NoSuchIdException;
import org.springframework.beans.factory.annotation.Required;
import org.plos.util.FileUtils;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

/**
 * Provides the Create/Read/Delete annotation operations.
 */
public class AnnotationService {
  private Annotations annotationService;
  private String applicationId;
  private String defaultAnnotationType;

  private static final Log log = LogFactory.getLog(AnnotationService.class);

  /**
   * @see org.topazproject.ws.annotation.Annotations#createAnnotation(String, String, String, String, String, boolean, String, String)
   * @param target target that an annotation is being created for
   * @param context context
   * @param title title
   * @param mimeType mimeType
   * @param body body
   * @throws ApplicationException
   * @return unique identifier for the newly created annotation
   */
  public String createAnnotation(final String target, final String context, final String title, final String mimeType, final String body) throws ApplicationException {
    try {
      final String encodingCharset = "UTF-8";
      final String contentType = mimeType + ";charset=" + encodingCharset;
      return annotationService.createAnnotation(applicationId, defaultAnnotationType, target, context, null, false, title, contentType, body.getBytes(encodingCharset));
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (UnsupportedEncodingException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotations#deleteAnnotation(String, boolean)
   * @param annotation annotation
   * @param deletePreceding deletePreceding
   * @throws ApplicationException
   */
  public void deleteAnnotation(final String annotation, final boolean deletePreceding) throws ApplicationException {
    try {
      annotationService.deleteAnnotation(annotation, deletePreceding);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotations#listAnnotations(String, String, String)
   * @param target target of the annotation
   * @throws ApplicationException
   * @return a list of annotations
   */
  public AnnotationInfo[] listAnnotations(final String target) throws ApplicationException {
    try {
      return annotationService.listAnnotations(applicationId, target, defaultAnnotationType);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotations#getAnnotationInfo(String)
   * @param annotationId annotationId
   * @throws org.topazproject.ws.annotation.NoSuchIdException
   * @throws ApplicationException
   * @return Annotation
   */
  public AnnotationInfo getAnnotationInfo(final String annotationId) throws NoSuchIdException, ApplicationException {
    try {
      return annotationService.getAnnotationInfo(annotationId);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Set the id of the application
   * @param applicationId applicationId
   */
  @Required
  public void setApplicationId(final String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   * Set the default annotation type. Listed at http://www.w3.org/2000/10/annotationType
   * @param defaultAnnotationType defaultAnnotationType
   */
  public void setDefaultAnnotationType(final String defaultAnnotationType) {
    this.defaultAnnotationType = defaultAnnotationType;
  }

  /**
   * Set the service location
   * @param serviceUrl serviceUrl
   * @throws ServiceException
   * @throws MalformedURLException
   */
  public void setServicePort(final String serviceUrl) throws ServiceException, MalformedURLException {
    annotationService = AnnotationClientFactory.create(serviceUrl);
  }

  /**
   * Get the bodyUrl of the annotation.
   * @param bodyUrl bodyUrl
   * @return content of the annotation
   * @throws java.net.MalformedURLException
   */
  public String getBody(final String bodyUrl) throws IOException {
    return FileUtils.getTextFromUrl(bodyUrl);
  }

}
