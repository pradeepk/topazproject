/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.ws.annotation.AnnotationClientFactory;
import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.annotation.Annotations;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * Wrapper over annotation(not the same as reply) web service
 */
public class AnnotationWebService extends BaseAnnotationService {
  private Annotations annotationService;

  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService protectedService = createProtectedService(getConfiguration());
    annotationService = AnnotationClientFactory.create(protectedService);
  }

  /**
   * Create an annotation.
   * @param mimeType mimeType
   * @param target target
   * @param context context
   * @param title title
   * @param body body
   * @return a new annotation
   * @throws UnsupportedEncodingException
   * @throws java.rmi.RemoteException
   */
  public String createAnnotation(final String mimeType, final String target, final String context, final String title, final String body) throws RemoteException, UnsupportedEncodingException {
    final String contentType = getContentType(mimeType);
    return annotationService.createAnnotation(getApplicationId(), getDefaultType(), target, context, null, false, title, contentType, body.getBytes(getEncodingCharset()));
  }

  /**
   * Delete an annotation
   * @param annotationId annotationId
   * @param deletePreceding deletePreceding
   * @throws java.rmi.RemoteException
   * @see org.topazproject.ws.annotation.Annotations#deleteAnnotation(String, boolean)
   */
  public void deleteAnnotation(final String annotationId, final boolean deletePreceding) throws RemoteException {
      annotationService.deleteAnnotation(annotationId, deletePreceding);
  }

  /**
   * @see org.topazproject.ws.annotation.Annotations#listAnnotations(String, String, String)
   * @param target target
   * @return a list of annotations
   * @throws java.rmi.RemoteException
   */
  public AnnotationInfo[] listAnnotations(final String target) throws RemoteException {
      return annotationService.listAnnotations(getApplicationId(), target, getDefaultType());
  }

  /**
   * @see org.topazproject.ws.annotation.Annotations#getAnnotationInfo(String)
   * @param annotationId annotationId
   * @return an annotation
   * @throws RemoteException
   */
  public AnnotationInfo getAnnotation(final String annotationId) throws RemoteException {
    return annotationService.getAnnotationInfo(annotationId);
  }

  /**
   * Set the annotation as public.
   * @param annotationDoi annotationDoi
   * @throws RemoteException
   */
  public void setPublic(final String annotationDoi) throws RemoteException {
    annotationService.setAnnotationState(annotationDoi, Annotation.PUBLIC_MASK);
  }
}
