/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.annotation.service;


import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import com.opensymphony.oscache.base.NeedsRefreshException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.Constants;
import org.plos.annotation.FlagUtil;
import org.plos.permission.service.PermissionWebService;
import org.plos.service.BaseConfigurableService;
import org.plos.util.FileUtils;
import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.annotation.Annotations;
import org.topazproject.ws.annotation.ReplyInfo;
import org.plos.util.FileUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

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
  private static final String CACHE_KEY_ANNOTATION = "ANNOTATION";
  
  private GeneralCacheAdministrator articleCacheAdministrator;
  
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
  public String createAnnotation(final String target, final String context, final String olderAnnotation, 
      final String title, final String mimeType, final String body, final boolean isPublic) throws ApplicationException {
    
    /** 
     *  Placing the caching here rather than in AnnotationWebService because this
     *  is always called by the Action.  Flags also call the AnnotationWebService, and
     *  we need to distinguish between the two.  If annotation creation ever happens
     *  directly by calling the AnnotationWebService, this will result in a cache
     *  that out of date.
     */
   
    try {
      if (log.isDebugEnabled()) {
        StringBuilder debugMsg = new StringBuilder("creating annotation for target: ");
        debugMsg.append(target).append("; context: ").append(context).append("; supercedes: ");
        debugMsg.append(olderAnnotation).append("; title: " ).append(title).append("; mimeType: ");
        debugMsg.append(mimeType).append("; body: ").append(body).append("; isPublic: ").append(isPublic);
        log.debug(debugMsg);
      }
      final String annotationId = annotationWebService.createAnnotation(mimeType, target, context, olderAnnotation, title, body);
      if (isPublic) {
        setAnnotationPublic(annotationId);
      }
      articleCacheAdministrator.flushGroup(FileUtils.escapeURIAsPath(target));
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
   * Create a flag against an annotation or a reply
   * @param target target that a flag is being created for
   * @param reasonCode reasonCode
   * @param body body
   * @param mimeType mimeType @throws org.plos.ApplicationException ApplicationException
   * @param isAnnotation true if annotation, false if reply
   * @return unique identifier for the newly created flag
   * @throws org.plos.ApplicationException ApplicationException
   */
  public String createFlag(final String target, final String reasonCode, final String body, final String mimeType, final boolean isAnnotation) throws ApplicationException {
    try {
      final String flagBody = FlagUtil.createFlagBody(reasonCode, body);
      final String flagId = annotationWebService.createAnnotation(mimeType, target, null, null, null, flagBody);

      if (isAnnotation) {
        annotationWebService.setFlagged(target);
      } else {
        replyWebService.setFlagged(target);
      }
      return flagId;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Unflag the given annotation
   * @param annotationId annotationId
   * @throws ApplicationException ApplicationException
   */
  public void unflagAnnotation(final String annotationId) throws ApplicationException {
    try {
      annotationWebService.unflagAnnotation(annotationId);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (NoSuchIdException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Unflag the given reply
   * @param replyId replyId
   * @throws ApplicationException ApplicationException
   */
  public void unflagReply(final String replyId) throws ApplicationException {
    try {
      replyWebService.unflagReply(replyId);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (NoSuchIdException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Delete the given annotation along with/without the one it supercedes
   * @param annotationId annotationId
   * @param deletePreceding deletePreceding
   * @throws ApplicationException ApplicationException
   */
  public void deletePrivateAnnotation(final String annotationId, final boolean deletePreceding) throws ApplicationException {
    try {
      Annotation ai = getAnnotation(annotationId);
      articleCacheAdministrator.flushGroup(FileUtils.escapeURIAsPath(ai.getAnnotates()));
      annotationWebService.deletePrivateAnnotation(annotationId, deletePreceding);
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
      Annotation ai = getAnnotation(annotationId);
      articleCacheAdministrator.flushGroup(FileUtils.escapeURIAsPath(ai.getAnnotates()));
      annotationWebService.deletePublicAnnotation(annotationId);
      //TODO: Set the access permissions for administrator only
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
      replyWebService.deleteReply(replyId);
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
    /** Placing the caching here rather than in AnnotationWebService because this
     *  produces the objects for the Commentary view.  The Article HTML is cached
     *  which calls the AnnotationWebService listAnnotations directly, so that call
     *  won't happen too much.
     */
    
    Annotation[] allAnnotations;
    try {
      //for now, since all annotations are public, don't have to cache based on userID
      allAnnotations = (Annotation[])articleCacheAdministrator.getFromCache(target + CACHE_KEY_ANNOTATION); 
      if (log.isDebugEnabled()) {
        log.debug("retrieved annotation list from cache for article: " + target);
      }
    } catch (NeedsRefreshException nre) {
      boolean updated = false;
      try {
        //use grouping for future when annotations can be private
        AnnotationInfo[] annotations = annotationWebService.listAnnotations(target);
        allAnnotations = converter.convert(annotations);
        articleCacheAdministrator.putInCache(target + CACHE_KEY_ANNOTATION, 
                                   allAnnotations, new String[]{FileUtils.escapeURIAsPath(target)});
        updated = true;
        if (log.isDebugEnabled()) {
          log.debug("retrieved annotation list from FEDORA for article: " + target);
        }        
      } catch (RemoteException e) {
        log.error ("Could not retrieve all annotations for article: " + target);
        throw new ApplicationException(e);
      } finally {
        if (!updated)
          articleCacheAdministrator.cancelUpdate(target);
      }
    }
    return allAnnotations;
  }

  /**
   * @param target target of the annotation
   * @throws ApplicationException ApplicationException
   * @return a list of undeleted flags
   */
  public Flag[] listFlags(final String target) throws ApplicationException {
    final Annotation[] annotations = listAnnotations(target);
    final Collection<Flag> flagList = new ArrayList<Flag>(annotations.length);
    for (final Annotation annotation : annotations) {
      if (!annotation.isDeleted()) {
        flagList.add(new Flag(annotation));
      }
    }
    return flagList.toArray(new Flag[flagList.size()]);
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
    final String[] everyone = new String[]{Constants.Permission.ALL_PRINCIPALS};
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
   * Set the PermissionWebService
   * @param permissionWebService permissionWebService
   */
  public void setPermissionWebService(final PermissionWebService permissionWebService) {
    this.permissionWebService = permissionWebService;
  }

  /**
   * @return Returns the articleCacheAdministrator.
   */
  public GeneralCacheAdministrator getArticleCacheAdministrator() {
    return articleCacheAdministrator;
  }

  /**
   * @param articleCacheAdministrator The articleCacheAdministrator to set.
   */
  public void setArticleCacheAdministrator(GeneralCacheAdministrator articleCacheAdministrator) {
    this.articleCacheAdministrator = articleCacheAdministrator;
  }
}
