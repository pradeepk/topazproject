/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.annotation.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.util.FileUtils;
import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.annotation.NoSuchIdException;
import org.topazproject.ws.annotation.ReplyInfo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;

/**
 * Used for both annotation and reply services.
 * Provides the Create/Read/Delete annotation operations .
 */
public class AnnotationService extends BaseConfigurableService {
  private AnnotationWebService annotationWebService;
  private ReplyWebService replyWebService;

  private static final Log log = LogFactory.getLog(AnnotationService.class);
  private AnnotationConverter converter;

  /**
   * Create an annotation.
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
      return annotationWebService.createAnnotation(mimeType, target, context, title, body);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (UnsupportedEncodingException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Create a reply
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
      return replyWebService.createReply(mimeType, root, inReplyTo, title, body, this);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (UnsupportedEncodingException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @param annotationId annotationId
   * @param deletePreceding deletePreceding
   * @throws ApplicationException
   */
  public void deleteAnnotation(final String annotationId, final boolean deletePreceding) throws ApplicationException {
    try {
      annotationWebService.deleteAnnotation(annotationId, deletePreceding);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * delete replies with a given root and base reply
   * @param root root
   * @param inReplyTo inReplyTo
   * @throws ApplicationException
   */
  public void deleteReply(final String root, final String inReplyTo) throws ApplicationException {
    try {
      replyWebService.deleteReplies(root, inReplyTo);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * delete reply with id
   * @param replyId replyId of the reply
   * @throws ApplicationException
   */
  public void deleteReply(final String replyId) throws ApplicationException {
    try {
      replyWebService.deleteReplies(replyId);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @param target target of the annotation
   * @throws ApplicationException
   * @return a list of annotations
   */
  public Annotation[] listAnnotations(final String target) throws ApplicationException {
    try {
      AnnotationInfo[] annotations = annotationWebService.listAnnotations(target);
      return converter.convert(annotations);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  public void setConverter(final AnnotationConverter converter) {
    this.converter = converter;
  }

  /**
   * List replies.
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   * @throws ApplicationException
   * @return a list of replies
   */
  public Reply[] listReplies(final String root, final String inReplyTo) throws ApplicationException {
    try {
      final ReplyInfo[] replies = replyWebService.listReplies(root, inReplyTo);
      return converter.convert(replies);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get a list of all replies
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   * @throws ApplicationException
   * @return a list of all replies
   */
  public Reply[] listAllReplies(final String root, final String inReplyTo) throws ApplicationException {
    try {
      final ReplyInfo[] replies = replyWebService.listAllReplies(root, inReplyTo);
      return converter.convert(replies);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @param annotationId annotationId
   * @throws ApplicationException
   * @return Annotation
   */
  public Annotation getAnnotation(final String annotationId) throws ApplicationException {
    try {
      final AnnotationInfo annotation = annotationWebService.getAnnotation(annotationId);
      return converter.convert(annotation);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get reply
   * @param replyId replyId
   * @return the reply object
   * @throws NoSuchIdException
   * @throws ApplicationException
   */
  public Reply getReply(final String replyId) throws NoSuchIdException, ApplicationException {
    try {
      final ReplyInfo reply = replyWebService.getReplyInfo(replyId);
      return converter.convert(reply);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  public void setAnnotationWebService(final AnnotationWebService annotationWebService) {
    this.annotationWebService = annotationWebService;
  }

  public void setReplyWebService(final ReplyWebService replyWebService) {
    this.replyWebService = replyWebService;
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
}
