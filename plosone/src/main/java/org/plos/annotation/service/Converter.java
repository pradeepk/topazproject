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

import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.annotation.ReplyInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to convert types between topaz and plosone types
 */
public class Converter {
  /**
   * @param annotations an array of annotation-s
   * @param lazyFactory lazyFactory
   * @return an array of Annotation objects as required by the web layer
   * @throws ApplicationException
   */
  public static Annotation[] convert(final AnnotationInfo[] annotations, final AnnotationLazyLoaderFactory lazyFactory) throws ApplicationException {
    final List<Annotation> plosoneAnnotations = new ArrayList<Annotation>();
    for (final AnnotationInfo annotation : annotations) {
      plosoneAnnotations.add(convert(annotation, lazyFactory));
    }
    return plosoneAnnotations.toArray(new Annotation[plosoneAnnotations.size()]);
  }

  /**
   * @param annotation annotation
   * @param annotationLazyFactory annotationLazyFactory
   * @return the Annotation
   * @throws ApplicationException
   */
  public static Annotation convert(final AnnotationInfo annotation, final AnnotationLazyLoaderFactory annotationLazyFactory) throws ApplicationException {
    return new Annotation(annotation, annotationLazyFactory.create(annotation.getBody()));
  }

  /**
   * @param replies an array of Reply-ies
   * @param lazyLoaderFactory lazyLoaderFactory
   * @return an array of Reply objects as required by the web layer
   * @throws ApplicationException
   */
  public static Reply[] convert(final ReplyInfo[] replies, final AnnotationLazyLoaderFactory lazyLoaderFactory) throws ApplicationException {
    final List<Reply> plosoneReplies = new ArrayList<Reply>();
    for (final ReplyInfo reply : replies) {
      plosoneReplies.add(convert(reply, lazyLoaderFactory));
    }
    return plosoneReplies.toArray(new Reply[plosoneReplies.size()]);
  }

  /**
   * @param reply reply
   * @param annotationLazyFactory annotationLazyFactory
   * @return the reply for the web layer
   * @throws ApplicationException
   */
  public static Reply convert(final ReplyInfo reply, final AnnotationLazyLoaderFactory annotationLazyFactory) throws ApplicationException {
    return new Reply(reply, annotationLazyFactory.create(reply.getBody()));
  }
}
