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

import org.plos.ApplicationException;
import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.annotation.ReplyInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * A kind of utility class to convert types between topaz and plosone types fro Annotations and Replies
 */
public class AnnotationConverter {
  private AnnotationLazyLoaderFactory lazyLoaderFactory;

  /**
   * @param annotations an array of annotations
   * @return an array of Annotation objects as required by the web layer
   * @throws org.plos.ApplicationException
   */
  public Annotation[] convert(final AnnotationInfo[] annotations) throws ApplicationException {
    final List<Annotation> plosoneAnnotations = new ArrayList<Annotation>();
    for (final AnnotationInfo annotation : annotations) {
      plosoneAnnotations.add(convert(annotation));
    }
    return plosoneAnnotations.toArray(new Annotation[plosoneAnnotations.size()]);
  }

  /**
   * @param annotation annotation
   * @return the Annotation
   * @throws ApplicationException
   */
  public Annotation convert(final AnnotationInfo annotation) throws ApplicationException {
    final AnnotationLazyLoader lazyLoader = lazyLoaderFactory.create(annotation);

    return new Annotation(annotation) {
      protected String getOriginalBodyContent() throws ApplicationException {
        return lazyLoader.getBody();
      }
    };

  }

  /**
   * @param replies an array of Replies
   * @return an array of Reply objects as required by the web layer
   * @throws org.plos.ApplicationException ApplicationException
   */
  public Reply[] convert(final ReplyInfo[] replies) throws ApplicationException {
    final Collection<Reply> plosoneReplies = new ArrayList<Reply>();
    final Map<String, Reply> repliesMap = new HashMap<String, Reply>();

    String annotationId = null;
    if (replies.length > 0) {
      annotationId = replies[0].getRoot();
    }

    for (final ReplyInfo reply : replies) {
      final String replyTo = reply.getInReplyTo();

      final Reply convertedObj = convert(reply);
      repliesMap.put(reply.getId(), convertedObj);

      if (replyTo.equals(annotationId)) {
        plosoneReplies.add(convertedObj);
      } else {
        final Reply savedReply = repliesMap.get(replyTo);
        savedReply.addReply(convertedObj);
      }
    }

    return plosoneReplies.toArray(new Reply[plosoneReplies.size()]);
  }

  /**
   * @param reply reply
   * @return the reply for the web layer
   * @throws ApplicationException ApplicationException
   */
  public Reply convert(final ReplyInfo reply) throws ApplicationException {
    final AnnotationLazyLoader lazyLoader = lazyLoaderFactory.create(reply);

    return new Reply(reply) {
      protected String getOriginalBodyContent() throws ApplicationException {
        return lazyLoader.getBody();
      }

    };
  }

  /**
   * Set the lazy loader factory.
   * @param lazyLoaderFactory lazyLoaderFactory
   */
  public void setLazyLoaderFactory(final AnnotationLazyLoaderFactory lazyLoaderFactory) {
    this.lazyLoaderFactory = lazyLoaderFactory;
  }
}
