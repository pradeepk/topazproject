/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.annotation.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.util.FileUtils;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ws.annotation.AnnotationClientFactory;
import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.annotation.Annotations;
import org.topazproject.ws.annotation.NoSuchIdException;
import org.topazproject.ws.annotation.Replies;
import org.topazproject.ws.annotation.RepliesClientFactory;
import org.topazproject.ws.annotation.ReplyInfo;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

/**
 * Provides the Create/Read/Delete annotation operations.
 */
public class AnnotationService {
  private Annotations annotationService;
  private Replies replyService;
  private String applicationId;
  private String defaultAnnotationType;
  private String defaultReplyType;

  private String encodingCharset = "UTF-8";
  private static final Log log = LogFactory.getLog(AnnotationService.class);
  private boolean isAnonymous;

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
      final String contentType = mimeType + ";charset=" + encodingCharset;
      return annotationService.createAnnotation(applicationId, defaultAnnotationType, target, context, null, false, title, contentType, body.getBytes(encodingCharset));
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (UnsupportedEncodingException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Replies#createReply(String, String, String, String, boolean, String, String, byte[])
   * @param root root
   * @param inReplyTo inReplyTo
   * @param title title
   * @param mimeType mimeType
   * @param body body
   * @throws ApplicationException
   * @return unique identifier for the newly created reply
   */
  public String createReply(final String root, final String inReplyTo, final String title, final String mimeType, final String body) throws ApplicationException {
    try {
      final String contentType = mimeType + ";charset=" + encodingCharset;
      return replyService.createReply(applicationId, defaultReplyType, root, inReplyTo, isAnonymous, title, contentType, body.getBytes(encodingCharset));
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (UnsupportedEncodingException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotations#deleteAnnotation(String, boolean)
   * @param annotationId annotationId
   * @param deletePreceding deletePreceding
   * @throws ApplicationException
   */
  public void deleteAnnotation(final String annotationId, final boolean deletePreceding) throws ApplicationException {
    try {
      annotationService.deleteAnnotation(annotationId, deletePreceding);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Replies#deleteReplies(String, String)
   * @param root root
   * @param inReplyTo inReplyTo
   * @throws ApplicationException
   */
  public void deleteReply(final String root, final String inReplyTo) throws ApplicationException {
    try {
      replyService.deleteReplies(root, inReplyTo);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Replies#deleteReplies(String)
   * @param replyId replyId of the reply
   * @throws ApplicationException
   */
  public void deleteReply(final String replyId) throws ApplicationException {
    try {
      replyService.deleteReplies(replyId);
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
  public Annotation[] listAnnotations(final String target) throws ApplicationException {
    try {
      final AnnotationInfo[] annotations = annotationService.listAnnotations(applicationId, target, defaultAnnotationType);
      return Converter.convert(annotations);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Replies#listReplies(String, String)
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   * @throws ApplicationException
   * @return a list of replies
   */
  public Reply[] listReplies(final String root, final String inReplyTo) throws ApplicationException {
    try {
      final ReplyInfo[] replies = replyService.listReplies(root, inReplyTo);
      return Converter.convert(replies);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Replies#listAllReplies(String, String)
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   * @throws ApplicationException
   * @return a list of all replies
   */
  public Reply[] listAllReplies(final String root, final String inReplyTo) throws ApplicationException {
    try {
      final ReplyInfo[] replies = replyService.listAllReplies(root, inReplyTo);
      return Converter.convert(replies);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotations#getAnnotationInfo(String)
   * @param annotationId annotationId
   * @throws ApplicationException
   * @return Annotation
   */
  public Annotation getAnnotation(final String annotationId) throws ApplicationException {
    try {
      final AnnotationInfo annotation = annotationService.getAnnotationInfo(annotationId);
      return Converter.convert(annotation);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Replies#getReplyInfo(String)
   * @param replyId replyId
   * @return the reply object
   * @throws NoSuchIdException
   * @throws ApplicationException
   */
  public Reply getReply(final String replyId) throws NoSuchIdException, ApplicationException {
    try {
      final ReplyInfo reply = replyService.getReplyInfo(replyId);
      return Converter.convert(reply);
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
   * Set the default annotation type.
   * @param defaultAnnotationType defaultAnnotationType
   * @see org.topazproject.ws.annotation.Annotations
   */
  public void setDefaultAnnotationType(final String defaultAnnotationType) {
    this.defaultAnnotationType = defaultAnnotationType;
  }

  /**
   * Set the default annotation type.
   * @param defaultReplyType defaultReplyType
   * @see org.topazproject.ws.annotation.Replies
   */
  public void setDefaultReplyType(final String defaultReplyType) {
    this.defaultReplyType = defaultReplyType;
  }

  /**
   * Set the annotation service.
   * @param annotationServiceUrl annotationServiceUrl
   * @throws ServiceException
   * @throws MalformedURLException
   */
  public void setAnnotationServicePort(final String annotationServiceUrl) throws ServiceException, MalformedURLException {
    annotationService = AnnotationClientFactory.create(annotationServiceUrl);
  }

  /**
   * Get the bodyUrl of the annotation.
   * @param bodyUrl bodyUrl
   * @return content of the annotation
   * @throws ApplicationException
   */
  public String getBody(final String bodyUrl) throws ApplicationException {
    try {
      return FileUtils.getTextFromUrl(bodyUrl);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @param encodingCharset charset for encoding the data to be persisting in
   */
  public void setEncodingCharset(final String encodingCharset) {
    this.encodingCharset = encodingCharset;
  }

  /**
   * Set the reply service.
   * @param replyServiceUrl replyServiceUrl
   * @throws MalformedURLException
   * @throws ServiceException
   */
  public void setReplyServicePort(final String replyServiceUrl) throws MalformedURLException, ServiceException {
    this.replyService = RepliesClientFactory.create(replyServiceUrl);
  }

  /**
   * Set whether the user isAnonymous.
   * @param isAnonymous true if user isAnonymous
   */
  public void setAnonymous(final boolean isAnonymous) {
    this.isAnonymous = isAnonymous;
  }
}
