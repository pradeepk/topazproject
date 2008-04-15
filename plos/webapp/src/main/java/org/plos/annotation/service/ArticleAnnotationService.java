/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.plos.annotation.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URI;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.plos.annotation.service.WebAnnotation.FLAG_MASK;
import static org.plos.annotation.service.WebAnnotation.PUBLIC_MASK;

import org.plos.article.service.FetchArticleService;

import org.plos.models.Annotation;
import org.plos.models.AnnotationBlob;
import org.plos.models.ArticleAnnotation;
import org.plos.models.Comment;

import org.plos.permission.service.PermissionsService;

import org.plos.user.PlosOneUser;

import org.plos.util.CacheAdminHelper;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.criterion.Restrictions;

import net.sf.ehcache.Ehcache;

/**
 * Wrapper over annotation(not the same as reply) web service
 */
public class ArticleAnnotationService extends BaseAnnotationService {
  public static final String ANNOTATED_KEY = "ArticleAnnotationCache-Annotation-";
  public static final String ANNOTATION_KEY = "ArticleAnnotationCache-SingleAnnotation-";
  public static final String ANNOTATION_LOCK = "ArticleAnnotationCache-lock-";
  protected static final Set<Class<?extends ArticleAnnotation>> ALL_ANNOTATION_CLASSES =
    new HashSet<Class<?extends ArticleAnnotation>>();
  private static final Log     log                    =
    LogFactory.getLog(ArticleAnnotationService.class);
  private final AnnotationsPEP       pep;
  private Session              session;
  private PermissionsService permissionsService;
  private FetchArticleService  fetchArticleService;
  private Ehcache              articleAnnotationCache;

  static {
    ALL_ANNOTATION_CLASSES.add(ArticleAnnotation.class);
  }

  /**
   * Create an ArticleAnnotationService object.
   *
   * @throws IOException on a PEP creation error
   */
  public ArticleAnnotationService() throws IOException {
    try {
      pep                                             = new AnnotationsPEP();
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      IOException ioe = new IOException("Failed to create PEP");
      ioe.initCause(e);
      throw ioe;
    }
  }

  /**
   * Create a Flag Annotation.
   *
   * @param mimeType of the body content
   * @param target the target of this annotation
   * @param body the body string
   * @param reasonCode the reason for flagging
   *
   * @return the id of the flag annotation
   *
   * @throws Exception on an error in create
   */
  public String createFlagAnnotation(final String mimeType, final String target, final String body,
                                     String reasonCode)
                              throws Exception {
    // TODO - eventually this should create a different type of annotation and not call this ...
    return createAnnotation(mimeType, target, null, null, null, body);
  }

  /**
   * Create a Comment annotation.
   *
   * @param mimeType mimeType of the body content
   * @param target target for this comment
   * @param context the context within the target that is being commented upon
   * @param olderAnnotation olderAnnotation that the new one will supersede
   * @param title title of this comment
   * @param body body of this comment
   *
   * @return the new annotation id
   *
   * @throws Exception on an error
   */
  public String createAnnotation(final String mimeType, final String target, final String context,
                                 final String olderAnnotation, final String title, final String body)
                          throws Exception {
    pep.checkAccess(AnnotationsPEP.CREATE_ANNOTATION, URI.create(target));

    final String contentType = getContentType(mimeType);

    //Ensure that it is null if the olderAnnotation is empty
    final String earlierAnnotation = StringUtils.isEmpty(olderAnnotation) ? null : olderAnnotation;

    if (earlierAnnotation != null)
      throw new UnsupportedOperationException("supersedes is not supported");

    String         user       = PlosOneUser.getCurrentUser().getUserId();
    AnnotationBlob blob       =
      new AnnotationBlob(contentType, body.getBytes(getEncodingCharset()));

    final Comment  newComment = new Comment();

    newComment.setMediator(getApplicationId());
    newComment.setType(getDefaultType());
    newComment.setAnnotates(URI.create(target));
    newComment.setContext(context);
    newComment.setTitle(title);

    if (isAnonymous())
      newComment.setAnonymousCreator(user);
    else
      newComment.setCreator(user);

    newComment.setBody(blob);
    newComment.setCreated(new Date());

    String newId = session.saveOrUpdate(newComment);

    if (log.isDebugEnabled())
      log.debug("created annotaion " + newId + " for " + target + " with body in blob "
                + blob.getId());

    boolean propagated = false;

    try {
      permissionsService.propagatePermissions(newId, new String[] { blob.getId() });
      propagated = true;

      if (log.isDebugEnabled())
        log.debug("propagated permissions for annotaion " + newId + " to " + blob.getId());
    } finally {
      if (!propagated) {
        if (log.isDebugEnabled())
          log.debug("failed to propagate permissions for annotaion " + newId + " to "
                    + blob.getId());

        try {
          deleteAnnotation(newId);
        } catch (Throwable t) {
          if (log.isDebugEnabled())
            log.debug("failed to delete partially created annotation " + newId, t);
        }
      }
    }

    // TODO - is it necessary to remove the article from the cache just because we changed 
    // an annotation?
    fetchArticleService.removeFromArticleCache(new String[] { target });
    // Flush the annotation cache for this target and the annotation
    removeAnnotationFromCache(newComment);

    if (log.isDebugEnabled()) {
      log.debug("removed " + target + " from articleAnnotationCache");
    }

    return newId;
  }

  /**
   * Create an annotation.
   *
   * @param annotationClass the class of annotation
   * @param mimeType mimeType of the annotation body
   * @param target target of this annotation
   * @param context context the context within the target that this applies to
   * @param olderAnnotation olderAnnotation that the new one will supersede
   * @param title title of this annotation
   * @param body body of this annotation
   *
   * @return a the new annotation id
   *
   * @throws Exception on an error
   */
  public String createArticleAnnotation(Class<ArticleAnnotation> annotationClass,
                                        final String mimeType, final String target,
                                        final String context, final String olderAnnotation,
                                        final String title, final String body)
                                 throws Exception {
    pep.checkAccess(AnnotationsPEP.CREATE_ANNOTATION, URI.create(target));

    final String contentType = getContentType(mimeType);

    //Ensure that it is null if the olderAnnotation is empty
    final String earlierAnnotation = StringUtils.isEmpty(olderAnnotation) ? null : olderAnnotation;

    if (earlierAnnotation != null)
      throw new UnsupportedOperationException("supersedes is not supported");

    String                  user       = PlosOneUser.getCurrentUser().getUserId();
    AnnotationBlob          blob       =
      new AnnotationBlob(contentType, body.getBytes(getEncodingCharset()));

    final ArticleAnnotation annotation = (ArticleAnnotation) annotationClass.newInstance();

    annotation.setMediator(getApplicationId());
    // a.setType(getDefaultType());
    annotation.setAnnotates(URI.create(target));
    annotation.setContext(context);
    annotation.setTitle(title);

    if (isAnonymous())
      annotation.setAnonymousCreator(user);
    else
      annotation.setCreator(user);

    annotation.setBody(blob);
    annotation.setCreated(new Date());

    String newId = session.saveOrUpdate(annotation);

    if (log.isDebugEnabled())
      log.debug("created annotaion " + newId + " for " + target + " with body in blob "
                + blob.getId());

    boolean propagated = false;

    try {
      permissionsService.propagatePermissions(newId, new String[] { blob.getId() });
      propagated = true;

      if (log.isDebugEnabled())
        log.debug("propagated permissions for annotaion " + newId + " to " + blob.getId());
    } finally {
      if (!propagated) {
        if (log.isDebugEnabled())
          log.debug("failed to propagate permissions for annotaion " + newId + " to "
                    + blob.getId());

        try {
          deleteAnnotation(newId);
        } catch (Throwable t) {
          if (log.isDebugEnabled())
            log.debug("failed to delete partially created annotation " + newId, t);
        }
      }
    }

    fetchArticleService.removeFromArticleCache(new String[] { target });

    if (log.isDebugEnabled()) {
      log.debug("removed " + target + " from articleAnnotationCache");
    }

    return newId;
  }

  /**
   * Unflag an annotation.
   *
   * @param annotationId annotationId
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  public void unflagAnnotation(final String annotationId)
                        throws OtmException, SecurityException {
    pep.checkAccess(pep.SET_ANNOTATION_STATE, URI.create(annotationId));

    ArticleAnnotation a = session.get(ArticleAnnotation.class, annotationId);
    a.setState(a.getState() & ~FLAG_MASK);
  }

  /**
   * Delete an annotation subtree and not just mark it.
   *
   * @param annotationId annotationId
   * @param deletePreceding deletePreceding
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  public void deletePrivateAnnotation(final String annotationId, final boolean deletePreceding)
                               throws OtmException, SecurityException {
    pep.checkAccess(pep.DELETE_ANNOTATION, URI.create(annotationId));
    deleteAnnotation(annotationId);
  }

  /**
   * Mark an annotation as deleted.
   *
   * @param annotationId annotationId
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  public void deletePublicAnnotation(final String annotationId)
                              throws OtmException, SecurityException {
    pep.checkAccess(pep.DELETE_ANNOTATION, URI.create(annotationId));
    deleteAnnotation(annotationId);
  }

  /**
   * Mark the given flag as deleted.
   *
   * @param flagId flagId
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  public void deleteFlag(final String flagId) throws OtmException, SecurityException {
    pep.checkAccess(pep.DELETE_ANNOTATION, URI.create(flagId));
    deleteAnnotation(flagId);
  }

  private void deleteAnnotation(final String annotationId)
                         throws OtmException {
    ArticleAnnotation a = session.get(ArticleAnnotation.class, annotationId);

    if (a != null) {
      session.delete(a);
      fetchArticleService.removeFromArticleCache(new String[] { a.getAnnotates().toString() });
      removeAnnotationFromCache(a);
    }
  }

  /**
   * List annotations for a target.
   *
   * @param target the target
   *
   * @return the list
   *
   * @throws OtmException on an error
   */
  private List<ArticleAnnotation> listAnnotationsForTarget(String target)
                                             throws OtmException {
    return listAnnotationsForTarget(target, null);
  }

  /**
   * Retrieve all Annotation instances that annotate the given target DOI. If
   * annotationClassTypes is null, then all annotation types are retrieved. If
   * annotationClassTypes is not null, only the Annotation class types in the annotationClassTypes
   * Set are returned.
   *
   * @param target
   * @param annotationClassTypes
   *
   * @return list of annotations
   *
   * @throws OtmException on an error
   */
  private List<ArticleAnnotation> listAnnotationsForTarget(final String target,
                                                    Set<Class<?extends ArticleAnnotation>> annotationClassTypes)
                                             throws OtmException {
    // lock @ Article level
    final Object     lock           = (FetchArticleService.ARTICLE_LOCK + target).intern();
    List<ArticleAnnotation> allAnnotations =
      CacheAdminHelper.getFromCacheE(articleAnnotationCache, ANNOTATED_KEY + target, -1, lock,
                                     "annotation list",
                                     new CacheAdminHelper.EhcacheUpdaterE<List<ArticleAnnotation>, OtmException>() {
          public List<ArticleAnnotation> lookup() throws OtmException {
            return listAnnotations(target, getApplicationId(), -1, ALL_ANNOTATION_CLASSES);
          }
        });

    /*
     * TODO: Since we cache the set of all annotations, we can't query and cache a limited
     * set of annotations at this time, so we have to filter out the types here and query
     * for all types above when populating the cache
     */
    if (annotationClassTypes == null)
      annotationClassTypes = ALL_ANNOTATION_CLASSES;

    List<ArticleAnnotation> filteredAnnotations = new ArrayList<ArticleAnnotation>(allAnnotations.size());

    for (ArticleAnnotation a : allAnnotations) {
      for (Class classType : annotationClassTypes) {
        if (classType.isInstance(a)) {
          filteredAnnotations.add(a);

          break;
        }
      }
    }

    return filteredAnnotations;
  }

  private List<ArticleAnnotation> listAnnotations(final String target, final String mediator,
                                           final int state,
                                           final Set<Class<?extends ArticleAnnotation>> classTypes)
                                    throws OtmException {
    List<ArticleAnnotation> combinedAnnotations = new ArrayList<ArticleAnnotation>();

    for (Class anClass : classTypes) {
      Criteria c = session.createCriteria(anClass);
      setRestrictions(c, target, mediator, state);
      combinedAnnotations.addAll((List<ArticleAnnotation>) c.list());
    }

    if (log.isDebugEnabled()) {
      log.debug("retrieved annotation list from TOPAZ for target: " + target);
    }

    return combinedAnnotations;
  }

  /**
   * Helper method to set restrictions on the criteria used in listAnnotations()
   *
   * @param c the criteria
   * @param target the target that is being annotated
   * @param mediator the mediator that does the annotation on the user's behalf
   * @param state the state to filter by (0 for all non-zero, -1 to not restrict by state)
   */
  private static void setRestrictions(Criteria c, final String target, final String mediator,
                                      final int state) {
    if (mediator != null) {
      c.add(Restrictions.eq("mediator", mediator));
    }

    if (state != -1) {
      if (state == 0) {
        c.add(Restrictions.ne("state", "0"));
      } else {
        c.add(Restrictions.eq("state", "" + state));
      }
    }

    if (target != null) {
      c.add(Restrictions.eq("annotates", target));
    }
  }

  /**
   * See listAnnotations(target, annotationClassTypes). This method returns the result of
   * that method that with annoationClassTypes set to null.
   *
   * @param target
   *
   * @return a list of annotations
   *
   * @throws OtmException on an error
   */
  public ArticleAnnotation[] listAnnotations(final String target)
                                   throws OtmException {
    return listAnnotations(target, null);
  }

  /**
   * Retrieve all Annotation instances that annotate the given target DOI. If
   * annotationClassTypes is null, then all annotation types are retrieved. If
   * annotationClassTypes is not null, only the Annotation class types in the annotationClassTypes
   * Set are returned. Each Class in annotationClassTypes should extend Annotation. E.G.
   * Comment.class or FormalCorrection.class
   *
   * @param target target doi that the listed annotations annotate
   * @param annotationClassTypes a set of Annotation class types to filter the results
   *
   * @return a list of annotations
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  public ArticleAnnotation[] listAnnotations(final String target,
                                          Set<Class<?extends ArticleAnnotation>> annotationClassTypes)
                                   throws OtmException, SecurityException {
    pep.checkAccess(AnnotationsPEP.LIST_ANNOTATIONS, URI.create(target));


    List<ArticleAnnotation> all      = listAnnotationsForTarget(target, annotationClassTypes);
    List<ArticleAnnotation> filtered = new ArrayList(all.size());

    for (ArticleAnnotation a : all) {
      try {
        pep.checkAccess(pep.GET_ANNOTATION_INFO, a.getId());
        filtered.add(a);
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("no permission for viewing annotation " + a.getId()
                    + " and therefore removed from list");
      }
    }

    return filtered.toArray(new ArticleAnnotation[filtered.size()]);
  }

  /**
   * Loads the article annotation with the given id.
   *
   * @param annotationId annotationId
   *
   * @return an annotation
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  public ArticleAnnotation getAnnotation(final String annotationId)
                               throws OtmException, SecurityException {
    final Object lock = (ANNOTATION_LOCK + annotationId).intern();
    ArticleAnnotation   a    =
      CacheAdminHelper.getFromCacheE(articleAnnotationCache, ANNOTATION_KEY + annotationId, -1,
                                    lock, "individual annotation",
                                    new CacheAdminHelper.EhcacheUpdaterE<ArticleAnnotation, OtmException>() {
          public ArticleAnnotation lookup() throws OtmException {
            return session.get(ArticleAnnotation.class, annotationId);
          }
        });

    if (a == null)
      throw new IllegalArgumentException("invalid annoation id: " + annotationId);


    pep.checkAccess(pep.GET_ANNOTATION_INFO, URI.create(annotationId));
    return a;
  }

  /**
   * Set the annotation as public.
   *
   * @param annotationDoi annotationDoi
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  public void setPublic(final String annotationDoi) throws OtmException, SecurityException {
    pep.checkAccess(pep.SET_ANNOTATION_STATE, URI.create(annotationDoi));

    ArticleAnnotation a = session.get(ArticleAnnotation.class, annotationDoi);
    a.setState(a.getState() | PUBLIC_MASK);
    removeAnnotationFromCache(a);
  }

  /**
   * Set the annotation as flagged.
   *
   * @param annotationId annotationId
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  public void setFlagged(final String annotationId) throws OtmException, SecurityException {
    pep.checkAccess(pep.SET_ANNOTATION_STATE, URI.create(annotationId));

    ArticleAnnotation a = session.get(ArticleAnnotation.class, annotationId);
    a.setState(a.getState() | FLAG_MASK);
    removeAnnotationFromCache(a);
  }

  /**
   * List the set of annotations in a specific administrative state.
   *
   * @param mediator if present only those annotations that match this mediator are returned
   * @param state the state to filter the list of annotations by or 0 to return annotations in any
   *        administrative state
   *
   * @return an array of annotation metadata; if no matching annotations are found, an empty array
   *         is returned
   *
   * @throws OtmException if some error occurred
   */
  public ArticleAnnotation[] listAnnotations(final String mediator, final int state)
                                   throws OtmException {
    pep.checkAccess(pep.LIST_ANNOTATIONS_IN_STATE, pep.ANY_RESOURCE);
    List<ArticleAnnotation> annotations =
      listAnnotations(null, mediator, state, ALL_ANNOTATION_CLASSES);

    return annotations.toArray(new ArticleAnnotation[annotations.size()]);
  }

  /**
   * Replaces the Annotation with DOI targetId with a new Annotation of type
   * newAnnotationClassType. Converting requires that a new class type be used, so the old
   * annotation properties are copied over to the new type. All annotations that referenced the
   * old annotation are updated to reference the new annotation. The old annotation is deleted.
   * The given newAnnotationClassType should implement the interface ArticleAnnotation. Known
   * annotation classes that implement this interface are Comment, FormalCorrection,
   * MinorCorrection
   *
   * @param srcAnnotationId - the DOI of the annotation to convert
   * @param newAnnotationClassType - the Class of the new annotation type. Should implement
   *        ArticleAnnotation
   *
   * @return the id of the new annotation
   *
   * @throws Exception on an error
   */
  public String convertArticleAnnotationToType(final String srcAnnotationId,
                                               final Class newAnnotationClassType)
                                        throws Exception {
    ArticleAnnotation srcAnnotation = session.get(ArticleAnnotation.class, srcAnnotationId);

    if (srcAnnotation == null) {
      log.error("Annotation was null for id: " + srcAnnotationId);

      return null;
    }

    ArticleAnnotation oldAn = (ArticleAnnotation) srcAnnotation;
    ArticleAnnotation newAn = (ArticleAnnotation) newAnnotationClassType.newInstance();

    BeanUtils.copyProperties(newAn, oldAn);
    newAn.setId(null); // this should not have been copied (original
                       // ArticleAnnotation interface did not have a setId()
                       // method...but somehow it is! Reset to null.

    pep.checkAccess(pep.CREATE_ANNOTATION, oldAn.getAnnotates());

    String newId    = session.saveOrUpdate(newAn);
    URI    newIdUri = new URI(newId);
    permissionsService.propagatePermissions(newId, new String[] { newAn.getBody().getId() });

    // Find all Annotations that refer to the old Annotation and update their
    // target 'annotates'
    // property to point to the new Annotation.
    List<ArticleAnnotation> refAns = listAnnotationsForTarget(oldAn.getId().toString());

    for (ArticleAnnotation refAn : refAns) {
      refAn.setAnnotates(newIdUri);
      removeAnnotationFromCache(refAn);
    }

    // Delete the original annotation from mulgara. Note, we don't call
    // deleteAnnotation() here
    // since that also removes the body of the article from fedora and we are
    // referencing that from
    // the new annotation.
    removeAnnotationFromCache(srcAnnotation);
    oldAn.setBody(null);
    session.delete(oldAn);

    // We must clear the annotated article from the article cache
    String target = newAn.getAnnotates().toString();
    fetchArticleService.removeFromArticleCache(new String[] { target });

    if (log.isDebugEnabled()) {
      log.debug("removed " + target + " from articleAnnotationCache");
    }

    return newAn.getId().toString();
  }

  /**
   * Remove this annotation from the cache, and remove the cached list of annotation that is
   * targets
   *
   * @param a Annotation
   */
  public void removeAnnotationFromCache(ArticleAnnotation a) {
    articleAnnotationCache.remove(ANNOTATION_KEY + a.getId().toString());
    articleAnnotationCache.remove(ANNOTATED_KEY + a.getAnnotates().toString());
  }

  /**
   * Sets the article-annotation cache.
   *
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *        to use.
   */
  @Required
  public void setArticleAnnotationCache(Ehcache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set the fetchArticleService.
   *
   * @param fetchArticleService To use.
   */
  @Required
  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  /**
   * Set the PermissionsService
   *
   * @param permissionsService permissionWebService
   */
  @Required
  public void setPermissionsService(final PermissionsService permissionsService) {
    this.permissionsService = permissionsService;
  }
}
