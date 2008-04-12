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

import java.net.URI;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.SimpleTimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.models.Blob;
import org.plos.models.Annotation;
import org.plos.models.Annotea;
import org.plos.models.ArticleAnnotation;

/**
 * Annotation meta-data - compatible with topaz annotation ws.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationInfo {
  private static final Log log = LogFactory.getLog(AnnotationInfo.class);
  private ArticleAnnotation ann;
  private static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

  static {
    fmt.setTimeZone(new SimpleTimeZone(0, "UTC"));
  }

/**
   * Creates a new AnnotationInfo object.
   */
  public AnnotationInfo(ArticleAnnotation ann) {
    this.ann      = ann;
  }

  /**
   * Get annotation type.
   *
   * @return annotation type as String.
   */
  public String getType() {
    return ann.getType();
  }

  /**
   * Get annotates.
   *
   * @return annotates as a URI
   */
  public String getAnnotates() {
    URI u = ann.getAnnotates();

    return (u == null) ? null : u.toString();
  }

  /**
   * Get context.
   *
   * @return context as String.
   */
  public String getContext() {
    return ann.getContext();
  }

  /**
   * Get creator.
   *
   * @return creator as String.
   */
  public String getCreator() {
    return ann.getCreator();
  }

  /**
   * Get created.
   *
   * @return created as Date.
   */
  public String getCreated() {
    Date d = ann.getCreated();

    synchronized (fmt) {
      return (d == null) ? null : fmt.format(d);
    }
  }

  /**
   * Get body.
   *
   * @return body as String.
   */
  public Blob getBody() {
    return ann.getBody();
  }

  /**
   * Get supersedes.
   *
   * @return supersedes as String.
   */
  public String getSupersedes() {
    Annotation a = ann.getSupersedes();

    return (a == null) ? null : a.getId().toString();
  }

  /**
   * Get id.
   *
   * @return id as String.
   */
  public String getId() {
    return ann.getId().toString();
  }

  /**
   * Get title.
   *
   * @return title as String.
   */
  public String getTitle() {
    return ann.getTitle();
  }

  /**
   * Get supersededBy.
   *
   * @return supersededBy as String.
   */
  public String getSupersededBy() {
    Annotation a = ann.getSupersededBy();

    return (a == null) ? null : a.getId().toString();
  }

  /**
   * Get mediator.
   *
   * @return mediator as String.
   */
  public String getMediator() {
    return ann.getMediator();
  }

  /**
   * Get state.
   *
   * @return state as int.
   */
  public int getState() {
    return ann.getState();
  }

  public String toString() {

      StringBuffer sb = new StringBuffer();

      sb.append("AnnotationInfo:");
      sb.append(" id=").append(getId());
      sb.append(" annotates=").append(getAnnotates());
      sb.append(" title=").append(getTitle());

      return sb.toString();
  }

  /**
   * Determine the PubApp type name for the wrapped annotation. The PubApp type name is used
   * by the presentation tier (ftl, javascript, actions) to determine how to handle an annotation. 
   * Note: We don't handle annotation types Rating or Reply in this class. 
   * @return
   */
  public String getWebType() {
    // TODO: This type case is ugly - fix this when we fix the type hierarchy for Annotations
    return AnnotationService.getWebType((Annotea)ann);
  }
}
