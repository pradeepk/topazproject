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

import org.apache.commons.lang.StringUtils;
import static org.plos.annotation.service.Annotation.FLAG_MASK;
import static org.plos.annotation.service.Annotation.PUBLIC_MASK;
import static org.plos.annotation.service.Annotation.DELETE_MASK;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.ws.annotation.AnnotationClientFactory;
import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.annotation.Annotations;
import org.topazproject.ws.annotation.NoSuchAnnotationIdException;

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
    final ProtectedService protectedService = getProtectedService();
    annotationService = AnnotationClientFactory.create(protectedService);
  }

  /**
   * Create an annotation.
   * @param mimeType mimeType
   * @param target target
   * @param context context
   * @param olderAnnotation olderAnnotation that the new one will supersede
   * @param title title
   * @param body body
   * @return a the new annotation id
   * @throws java.io.UnsupportedEncodingException UnsupportedEncodingException
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.ws.annotation.NoSuchAnnotationIdException NoSuchAnnotationIdException
   */
  public String createAnnotation(final String mimeType, final String target, final String context, final String olderAnnotation, final String title, final String body) throws RemoteException, NoSuchAnnotationIdException, UnsupportedEncodingException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    final String contentType = getContentType(mimeType);
    //Ensure that it is null if the olderAnnotation is empty
    final String earlierAnotation = StringUtils.isEmpty(olderAnnotation) ? null : olderAnnotation;

    return annotationService.createAnnotation(getApplicationId(), getDefaultType(), target, context, earlierAnotation, false, title, contentType, body.getBytes(getEncodingCharset()));
  }

  /**
   * Delete an annotation
   * @param annotationId annotationId
   * @param deletePreceding deletePreceding
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.ws.annotation.NoSuchAnnotationIdException NoSuchAnnotationIdException
   * @see org.topazproject.ws.annotation.Annotations#deleteAnnotation(String, boolean)
   */
  public void deleteAnnotation(final String annotationId, final boolean deletePreceding) throws RemoteException, NoSuchAnnotationIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    annotationService.deleteAnnotation(annotationId, deletePreceding);
  }

  /**
   * Delete an annotation
   * @param annotationId annotationId
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.ws.annotation.NoSuchAnnotationIdException NoSuchAnnotationIdException
   * @see org.topazproject.ws.annotation.Annotations#deleteAnnotation(String, boolean)
   */
  public void deletePublicAnnotation(final String annotationId) throws RemoteException, NoSuchAnnotationIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    annotationService.setAnnotationState(annotationId, PUBLIC_MASK | DELETE_MASK);
  }

  /**
   * Delete the given flag
   * @param flagId flagId
   * @throws org.topazproject.ws.annotation.NoSuchAnnotationIdException NoSuchAnnotationIdException
   * @throws java.rmi.RemoteException RemoteException
   */
  public void deleteFlag(final String flagId) throws NoSuchAnnotationIdException, RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    annotationService.setAnnotationState(flagId, DELETE_MASK);
  }

  /**
   * @see org.topazproject.ws.annotation.Annotations#listAnnotations(String, String, String)
   * @param target target
   * @return a list of annotations
   * @throws java.rmi.RemoteException RemoteException
   */
  public AnnotationInfo[] listAnnotations(final String target) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return annotationService.listAnnotations(getApplicationId(), target, getDefaultType());
  }

  /**
   * @see org.topazproject.ws.annotation.Annotations#getAnnotationInfo(String)
   * @param annotationId annotationId
   * @return an annotation
   * @throws RemoteException RemoteException
   * @throws org.topazproject.ws.annotation.NoSuchAnnotationIdException NoSuchAnnotationIdException
   */
  public AnnotationInfo getAnnotation(final String annotationId) throws RemoteException, NoSuchAnnotationIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return annotationService.getAnnotationInfo(annotationId);
  }

  /**
   * Set the annotation as public.
   * @param annotationDoi annotationDoi
   * @throws RemoteException RemoteException
   * @throws org.topazproject.ws.annotation.NoSuchAnnotationIdException NoSuchAnnotationIdException
   */
  public void setPublic(final String annotationDoi) throws RemoteException, NoSuchAnnotationIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    annotationService.setAnnotationState(annotationDoi, PUBLIC_MASK);
  }

  /**
   * Set the annotation as flagged
   * @param annotationId annotationId
   * @throws NoSuchAnnotationIdException NoSuchAnnotationIdException
   * @throws RemoteException RemoteException
   */
  public void setFlagged(final String annotationId) throws NoSuchAnnotationIdException, RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    annotationService.setAnnotationState(annotationId, PUBLIC_MASK | FLAG_MASK);
  }

}
