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
package org.topazproject.ambra.annotation.service;

import java.io.IOException;
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
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.Constants;
import org.topazproject.ambra.annotation.FlagUtil;
import org.topazproject.ambra.article.service.FetchArticleService;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.models.Annotation;
import org.topazproject.ambra.models.AnnotationBlob;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.user.AmbraUser;
import org.topazproject.ambra.xacml.AbstractSimplePEP;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Interceptor.Updates;
import org.topazproject.otm.Session.FlushMode;
import org.topazproject.otm.criterion.Restrictions;

/**
 * Wrapper over annotation(not the same as reply) web service
 */
public class ArticleAnnotationService extends BaseAnnotationService {
  public static final String ANNOTATED_KEY = "ArticleAnnotationCache-Annotation-";

  protected static final Set<Class<?extends ArticleAnnotation>> ALL_ANNOTATION_CLASSES =
    new HashSet<Class<?extends ArticleAnnotation>>();
  private static final Log     log                    =
    LogFactory.getLog(ArticleAnnotationService.class);
  private final AnnotationsPEP       pep;
  private Cache              articleAnnotationCache;
  private Invalidator        invalidator;

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
      pep  = new AnnotationsPEP();
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      IOException ioe = new IOException("Failed to create PEP");
      ioe.initCause(e);
      throw ioe;
    }
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
   * @param isPublic to set up public permissions
   *
   * @return a the new annotation id
   *
   * @throws Exception on an error
   */
  private String createAnnotation(Class<? extends ArticleAnnotation> annotationClass,
                   final String mimeType, final String target,
                   final String context, final String olderAnnotation,
                   final String title, final String body, boolean isPublic)
                     throws Exception {

    pep.checkAccess(AnnotationsPEP.CREATE_ANNOTATION, URI.create(target));

    final String contentType = getContentType(mimeType);

    //Ensure that it is null if the olderAnnotation is empty
    final String earlierAnnotation = StringUtils.isEmpty(olderAnnotation) ? null : olderAnnotation;

    if (earlierAnnotation != null)
      throw new UnsupportedOperationException("supersedes is not supported");

    String                  user       = AmbraUser.getCurrentUser().getUserId();
    AnnotationBlob          blob       =
      new AnnotationBlob(contentType, body.getBytes(getEncodingCharset()));

    final ArticleAnnotation annotation = annotationClass.newInstance();

    annotation.setMediator(getApplicationId());
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

    permissionsService.propagatePermissions(newId, new String[] { blob.getId() });

    if (isPublic)
      setAnnotationPublic(newId);

    return newId;
  }

  /**
   * Delete an annotation.
   *
   * @param annotationId annotationId
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void deleteAnnotation(final String annotationId)
    throws OtmException, SecurityException {
    pep.checkAccess(AnnotationsPEP.DELETE_ANNOTATION, URI.create(annotationId));
    ArticleAnnotation a = session.get(ArticleAnnotation.class, annotationId);

    if (a != null)
      session.delete(a);
  }

  /**
   * Retrieve all Annotation instances that annotate the given target DOI.
   *
   * @param target the target
   *
   * @return list of annotations
   *
   * @throws OtmException on an error
   */
  private List<ArticleAnnotation> lookupAnnotations(final String target)
      throws OtmException {
    // This flush is so that our own query cache reflects change.
    // TODO : implement query caching in OTM and let it manage this cached query
    if (session.getFlushMode().implies(FlushMode.always))
      session.flush();

    // lock @ Article level
    final Object     lock           = (FetchArticleService.ARTICLE_LOCK + target).intern();

    return articleAnnotationCache.get(ANNOTATED_KEY + target, -1,
          new Cache.SynchronizedLookup<List<ArticleAnnotation>, OtmException>(lock) {
          @Override
          public List<ArticleAnnotation> lookup() throws OtmException {
            return loadAnnotations(target, getApplicationId(), -1, ALL_ANNOTATION_CLASSES);
          }
        });
  }

  private List<ArticleAnnotation> filterAnnotations(List<ArticleAnnotation> allAnnotations,
      Set<Class<?extends ArticleAnnotation>> annotationClassTypes) {

    if ((annotationClassTypes == null) || (annotationClassTypes.size() == 0)
       || ALL_ANNOTATION_CLASSES.equals(annotationClassTypes))
      return allAnnotations;

    List<ArticleAnnotation> filteredAnnotations = new
      ArrayList<ArticleAnnotation>(allAnnotations.size());

    for (ArticleAnnotation a : allAnnotations) {
      for (Class<? extends ArticleAnnotation> classType : annotationClassTypes) {
        if (classType.isInstance(a)) {
          filteredAnnotations.add(a);

          break;
        }
      }
    }

    return filteredAnnotations;
  }

  @SuppressWarnings("unchecked")
  private List<ArticleAnnotation> loadAnnotations(final String target, final String mediator,
      final int state, final Set<Class<?extends ArticleAnnotation>> classTypes)
    throws OtmException {
    List<ArticleAnnotation> combinedAnnotations = new ArrayList<ArticleAnnotation>();

    for (Class<? extends ArticleAnnotation> anClass : classTypes) {
      Criteria c = session.createCriteria(anClass);
      setRestrictions(c, target, mediator, state);
      combinedAnnotations.addAll(c.list());
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
   * Retrieve all Annotation instances that annotate the given target DOI. If annotationClassTypes
   * is null, then all annotation types are retrieved. If annotationClassTypes is not null, only the
   * Annotation class types in the annotationClassTypes Set are returned. Each Class in
   * annotationClassTypes should extend Annotation. E.G.  Comment.class or FormalCorrection.class
   *
   * @param target target doi that the listed annotations annotate
   * @param annotationClassTypes a set of Annotation class types to filter the results
   *
   * @return a list of annotations
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(readOnly = true)
  public ArticleAnnotation[] listAnnotations(final String target,
      Set<Class<?extends ArticleAnnotation>> annotationClassTypes)
    throws OtmException, SecurityException {
    pep.checkAccess(AnnotationsPEP.LIST_ANNOTATIONS, URI.create(target));


    List<ArticleAnnotation> filtered = new ArrayList<ArticleAnnotation>();

    for (ArticleAnnotation a : filterAnnotations(lookupAnnotations(target), annotationClassTypes)) {
      try {
        pep.checkAccess(AnnotationsPEP.GET_ANNOTATION_INFO, a.getId());
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
   * @throws IllegalArgumentException if an annotation with this id does not exist
   */
  @Transactional(readOnly = true)
  public ArticleAnnotation getAnnotation(final String annotationId)
    throws OtmException, SecurityException, IllegalArgumentException {
    pep.checkAccess(AnnotationsPEP.GET_ANNOTATION_INFO, URI.create(annotationId));
    // comes from object-cache.
    ArticleAnnotation   a = session.get(ArticleAnnotation.class, annotationId);
    if (a == null)
      throw new IllegalArgumentException("invalid annoation id: " + annotationId);

    return a;
  }

  /**
   * Set the annotation context.
   *
   * @param id the annotation id
   * @param context the context to set
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   * @throws IllegalArgumentException if an annotation with this id does not exist
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void updateContext(String id, String context)
    throws OtmException, SecurityException, IllegalArgumentException {

    pep.checkAccess(AnnotationsPEP.UPDATE_ANNOTATION, URI.create(id));

    Annotation<?> a = session.get(Annotation.class, id);
    if (a == null)
      throw new IllegalArgumentException("invalid annoation id: " + id);

    a.setContext(context);
  }

  /**
   * List the set of annotations in a specific administrative state.
   *
   * @param mediator if present only those annotations that match this mediator are returned
   * @param state the state to filter the list of annotations by or 0 to return annotations in any
   *              administrative state
   *
   * @return an array of annotation metadata; if no matching annotations are found, an empty array
   *         is returned
   *
   * @throws OtmException if some error occurred
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(readOnly = true)
  public ArticleAnnotation[] listAnnotations(final String mediator, final int state)
                                   throws OtmException, SecurityException {
    pep.checkAccess(AnnotationsPEP.LIST_ANNOTATIONS_IN_STATE, AbstractSimplePEP.ANY_RESOURCE);
    List<ArticleAnnotation> annotations =
      loadAnnotations(null, mediator, state, ALL_ANNOTATION_CLASSES);

    return annotations.toArray(new ArticleAnnotation[annotations.size()]);
  }

  /**
   * Replaces the Annotation with DOI targetId with a new Annotation of type newAnnotationClassType.
   * Converting requires that a new class type be used, so the old annotation properties are copied
   * over to the new type. All annotations that referenced the old annotation are updated to
   * reference the new annotation. The old annotation is deleted.  The given newAnnotationClassType
   * should implement the interface ArticleAnnotation. Known annotation classes that implement this
   * interface are Comment, FormalCorrection, MinorCorrection
   *
   * @param srcAnnotationId the DOI of the annotation to convert
   * @param newAnnotationClassType the Class of the new annotation type. Should implement
   *                               ArticleAnnotation
   *
   * @return the id of the new annotation
   *
   * @throws Exception on an error
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String convertArticleAnnotationToType(final String srcAnnotationId,
      final Class<? extends ArticleAnnotation> newAnnotationClassType) throws Exception {
    ArticleAnnotation srcAnnotation = session.get(ArticleAnnotation.class, srcAnnotationId);

    if (srcAnnotation == null) {
      log.error("Annotation was null for id: " + srcAnnotationId);

      return null;
    }

    ArticleAnnotation oldAn = srcAnnotation;
    ArticleAnnotation newAn = newAnnotationClassType.newInstance();

    BeanUtils.copyProperties(newAn, oldAn);
    /*
     * This should not have been copied (original ArticleAnnotation interface did not have a setId()
     * method..but somehow it is! Reset to null.
     */
    newAn.setId(null);

    String newId    = session.saveOrUpdate(newAn);
    URI    newIdUri = new URI(newId);
    String pp[]     = new String[] { newAn.getBody().getId() };

    permissionsService.propagatePermissions(newId, pp);
    permissionsService.cancelPropagatePermissions(srcAnnotationId, pp);

    /*
     * Find all Annotations that refer to the old Annotation and update their target 'annotates'
     * property to point to the new Annotation.
     */
    List<ArticleAnnotation> refAns = lookupAnnotations(oldAn.getId().toString());

    for (ArticleAnnotation refAn : refAns) {
      refAn.setAnnotates(newIdUri);
    }

    //Delete the original annotation (orphan-delete must be disabled for 'body')
    oldAn.setBody(null);
    session.delete(oldAn);

    setAnnotationPublic(newId);

    return newId;
  }

  /**
   * Sets the article-annotation cache.
   *
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *                               to use.
   */
  @Required
  public void setArticleAnnotationCache(Cache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
    if (invalidator == null) {
      invalidator = new Invalidator();
      articleAnnotationCache.getCacheManager().registerListener(invalidator);
    }
  }

  /**
   * Create an annotation.
   *
   * @param target target that an annotation is being created for
   * @param context context
   * @param olderAnnotation olderAnnotation that the new one will supersede
   * @param title title
   * @param mimeType mimeType
   * @param body body
   * @param isPublic isPublic
   * @throws Exception on an error
   * @return unique identifier for the newly created annotation
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String createComment(final String target, final String context,
                              final String olderAnnotation, final String title,
                              final String mimeType, final String body,
                              final boolean isPublic) throws Exception {

    if (log.isDebugEnabled()) {
      log.debug("creating Comment for target: " + target + "; context: " + context +
                "; supercedes: " + olderAnnotation + "; title: " + title + "; mimeType: " +
                mimeType + "; body: " + body + "; isPublic: " + isPublic);
    }

    String annotationId = createAnnotation(Comment.class, mimeType, target, context,
                                           olderAnnotation, title, body, true);

    if (log.isDebugEnabled()) {
      final AmbraUser user = AmbraUser.getCurrentUser();
      log.debug("Comment created with ID: " + annotationId + " for user: " + user +
                  " for IP: " + ServletActionContext.getRequest().getRemoteAddr());
    }

    return annotationId;
  }

  /**
   * Set the annotation as public.
   *
   * @param annotationDoi annotationDoi
   *
   * @throws OtmException if some error occurred
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setAnnotationPublic(final String annotationDoi)
                throws OtmException, SecurityException {
    final String[] everyone = new String[]{Constants.Permission.ALL_PRINCIPALS};
    permissionsService.grant(
            annotationDoi,
            new String[]{
                    AnnotationsPEP.GET_ANNOTATION_INFO}, everyone);

    permissionsService.revoke(
            annotationDoi,
            new String[]{
                    AnnotationsPEP.DELETE_ANNOTATION,
                    AnnotationsPEP.SUPERSEDE}, everyone);
  }

  /**
   * Create a flag against an annotation or a reply
   *
   * @param target target that a flag is being created for
   * @param reasonCode reasonCode
   * @param body body
   * @param mimeType mimeType
   * @return unique identifier for the newly created flag
   * @throws Exception on an error
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String createFlag(final String target, final String reasonCode, final String body, final String mimeType)
        throws Exception {
    final String flagBody = FlagUtil.createFlagBody(reasonCode, body);
    return createComment(target, null, null, null, mimeType, flagBody, true);
  }

  private class Invalidator extends AbstractObjectListener {
    @Override
    public void objectChanged(Session session, ClassMetadata cm, String id, Object o,
        Updates updates) {
      handleEvent(id, o, updates, false);
    }
    @Override
    public void objectRemoved(Session session, ClassMetadata cm, String id, Object o) {
      handleEvent(id, o, null, true);
    }
    private void handleEvent(String id, Object o, Updates updates, boolean removed) {
      if ((o instanceof Article) && removed) {
        if (log.isDebugEnabled())
          log.debug("Invalidating annotation list for the article that was deleted.");
        articleAnnotationCache.remove(ANNOTATED_KEY + id);
      } else if (o instanceof ArticleAnnotation) {
        if (log.isDebugEnabled())
          log.debug("ArticleAnnotation changed/deleted. Invalidating annotation list " +
              " for the target this was annotating or is about to annotate.");
        articleAnnotationCache.remove(ANNOTATED_KEY + ((ArticleAnnotation)o).getAnnotates().toString());
        if ((updates != null) && updates.isChanged("annotates")) {
           List<String> v = updates.getOldValue("annotates");
           if (v.size() == 1)
             articleAnnotationCache.remove(ANNOTATED_KEY + v.get(0));
        }
        if (removed)
          articleAnnotationCache.remove(ANNOTATED_KEY + id);
      }
    }
  }
}
