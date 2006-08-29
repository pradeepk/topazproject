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

import org.plos.annotation.service.impl.PlosoneAnnotation;
import org.plos.annotation.service.impl.PlosoneReply;
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
   * @return an array of Annotation objects as required by the web layer
   */
  public static Annotation[] convert(final AnnotationInfo[] annotations) {
    final List<Annotation> plosoneAnnotations = new ArrayList<Annotation>();
    for (final AnnotationInfo annotation : annotations) {
      plosoneAnnotations.add(convert(annotation));
    }
    return plosoneAnnotations.toArray(new PlosoneAnnotation[plosoneAnnotations.size()]);
  }

  /**
   * @param annotation annotation
   * @return the PlosoneAnnotation
   */
  public static Annotation convert(final AnnotationInfo annotation) {
    return new PlosoneAnnotation(annotation);
  }

  /**
   * @param replies an array of Reply-ies
   * @return an array of Reply objects as required by the web layer
   */
  public static Reply[] convert(final ReplyInfo[] replies) {
    final List<Reply> plosoneReplies = new ArrayList<Reply>();
    for (final ReplyInfo reply : replies) {
      plosoneReplies.add(convert(reply));
    }
    return plosoneReplies.toArray(new PlosoneReply[plosoneReplies.size()]);
  }

  /**
   * @param reply reply
   * @return the reply for the web layer
   */
  public static Reply convert(final ReplyInfo reply) {
    return new PlosoneReply(reply);
  }
}
