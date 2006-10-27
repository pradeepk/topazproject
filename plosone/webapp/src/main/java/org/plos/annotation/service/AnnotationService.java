/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.annotation.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.permission.service.PermissionWebService;
import org.plos.service.BaseConfigurableService;
import org.plos.util.FileUtils;
import org.plos.util.FlagUtil;
import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.annotation.Annotations;
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
  private PermissionWebService permissionWebService;

  /**
   * Create an annotation.
   * @param target target that an annotation is being created for
   * @param context context
   * @param olderAnnotation olderAnnotation that the new one will supersede
   * @param title title
   * @param mimeType mimeType
   * @param body body
   * @param isPublic isPublic
   * @throws org.plos.ApplicationException ApplicationException
   * @return unique identifier for the newly created annotation
   */
  public String createAnnotation(final String target, final String context, final String olderAnnotation, final String title, final String mimeType, final String body, final boolean isPublic) throws ApplicationException {
    try {
      final String annotationId = annotationWebService.createAnnotation(mimeType, target, context, olderAnnotation, title, body);
      if (isPublic) {
        setAnnotationPublic(annotationId);
      }
      return annotationId;
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (NoSuchIdException e) {
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
   * @throws ApplicationException ApplicationException
   * @return unique identifier for the newly created reply
   */
  public String createReply(final String root, final String inReplyTo, final String title, final String mimeType, final String body) throws ApplicationException {
    try {
      return replyWebService.createReply(mimeType, root, inReplyTo, title, body, this);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (NoSuchIdException e) {
      throw new ApplicationException(e);
    } catch (UnsupportedEncodingException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Create an flag.
   * @param target target that a flag is being created for
   * @param reasonCode reasonCode
   * @param body body
   * @param mimeType mimeType @throws org.plos.ApplicationException ApplicationException
   * @return unique identifier for the newly created flag
   * @throws org.plos.ApplicationException ApplicationException
   */
  public String createFlag(final String target, final String reasonCode, final String body, final String mimeType) throws ApplicationException {
    try {
      final String flagBody = FlagUtil.createFlagBody(reasonCode, body);
      final String annotationId = annotationWebService.createAnnotation(mimeType, target, null, null, null, flagBody);
      setAnnotationAsFlagged(target);
      return annotationId;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Delete the given annotation along with/without the one it supercedes
   * @param annotationId annotationId
   * @param deletePreceding deletePreceding
   * @throws ApplicationException ApplicationException
   */
  public void deleteAnnotation(final String annotationId, final boolean deletePreceding) throws ApplicationException {
    try {
      annotationWebService.deleteAnnotation(annotationId, deletePreceding);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (NoSuchIdException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Delete the given annotation along with/without the one it supercedes
   * @param annotationId annotationId
   * @throws ApplicationException ApplicationException
   */
  public void deletePublicAnnotation(final String annotationId) throws ApplicationException {
    try {
      annotationWebService.deletePublicAnnotation(annotationId);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (NoSuchIdException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Delete replies with a given root and base reply
   * @param root root
   * @param inReplyTo inReplyTo
   * @throws ApplicationException ApplicationException
   */
  public void deleteReply(final String root, final String inReplyTo) throws ApplicationException {
    try {
      replyWebService.deleteReplies(root, inReplyTo);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (NoSuchIdException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Delete the given flag
   * @param flagId flagId
   * @throws org.plos.ApplicationException ApplicationException
   */
  public void deleteFlag(final String flagId) throws ApplicationException {
    try {
      annotationWebService.deleteFlag(flagId);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (NoSuchIdException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * delete reply with id
   * @param replyId replyId of the reply
   * @throws ApplicationException ApplicationException
   */
  public void deleteReply(final String replyId) throws ApplicationException {
    try {
      replyWebService.deleteReplies(replyId);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (NoSuchIdException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @param target target of the annotation
   * @throws ApplicationException ApplicationException
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
   * @throws ApplicationException ApplicationException
   * @return a list of replies
   */
  public Reply[] listReplies(final String root, final String inReplyTo) throws ApplicationException {
    try {
      final ReplyInfo[] replies = replyWebService.listReplies(root, inReplyTo);
      return converter.convert(replies);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (NoSuchIdException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get a list of all replies
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   * @throws ApplicationException ApplicationException
   * @return a list of all replies
   */
  public Reply[] listAllReplies(final String root, final String inReplyTo) throws ApplicationException {
    try {
      final ReplyInfo[] replies = replyWebService.listAllReplies(root, inReplyTo);
      return converter.convert(replies);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (NoSuchIdException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @param annotationId annotationId
   * @throws ApplicationException ApplicationException
   * @return Annotation
   */
  public Annotation getAnnotation(final String annotationId) throws ApplicationException {
    try {
      final AnnotationInfo annotation = annotationWebService.getAnnotation(annotationId);
      return converter.convert(annotation);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (NoSuchIdException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get reply
   * @param replyId replyId
   * @return the reply object
   * @throws NoSuchIdException NoSuchIdException
   * @throws ApplicationException ApplicationException
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
   * @throws ApplicationException ApplicationException
   */
  public String getBody(final String bodyUrl) throws ApplicationException {
    try {
      return FileUtils.getTextFromUrl(bodyUrl);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Set the annotation as public.
   * @param annotationDoi annotationDoi
   * @throws ApplicationException ApplicationException
   */
  public void setAnnotationPublic(final String annotationDoi) throws ApplicationException {
    final String[] everyone = new String[]{AnnotationPermission.ALL_PRINCIPALS};
    try {
      permissionWebService.grant(
              annotationDoi,
              new String[]{
                      Annotations.Permissions.GET_ANNOTATION_INFO}, everyone);

      permissionWebService.revoke(
              annotationDoi,
              new String[]{
                      Annotations.Permissions.DELETE_ANNOTATION,
                      Annotations.Permissions.SUPERSEDE});

      annotationWebService.setPublic(annotationDoi);

    } catch (final Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Set the annotation as flagged.
   * @param annotationId annotationId
   * @throws org.plos.ApplicationException ApplicationException
   */
  private void setAnnotationAsFlagged(final String annotationId) throws ApplicationException {
    try {
      annotationWebService.setFlagged(annotationId);
    } catch (final RemoteException e) {
      throw new ApplicationException(e);
    } catch (NoSuchIdException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Set the PermissionWebService
   * @param permissionWebService permissionWebService
   */
  public void setPermissionWebService(final PermissionWebService permissionWebService) {
    this.permissionWebService = permissionWebService;
  }
}
