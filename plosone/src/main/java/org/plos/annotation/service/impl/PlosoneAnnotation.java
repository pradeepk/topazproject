/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service.impl;

import org.plos.annotation.service.Annotation;
import org.topazproject.ws.annotation.AnnotationInfo;
import com.opensymphony.util.TextUtils;

/**
 * Plosone wrapper around the AnnotationsInfo from topaz service. It provides
 * - A way to escape title/body text when returning the result to the web layer
 * - a separation from any topaz changes
 */
public class PlosoneAnnotation implements Annotation {
  private final AnnotationInfo annotation;

  /** {@inheritDoc} */
  public String getAnnotates() {
    return annotation.getAnnotates();
  }

  /** {@inheritDoc} */
  public void setAnnotates(final String s) {
    annotation.setAnnotates(s);
  }

  /** {@inheritDoc} */
  public String getBody() {
    // TODO: fetch the body content right away and escape it
//    return TextUtils.htmlEncode(annotation.getTitle());
    
    return annotation.getBody();
  }

  /** {@inheritDoc} */
  public void setBody(final String s) {
    annotation.setBody(s);
  }

  /** {@inheritDoc} */
  public String getContext() {
    return annotation.getContext();
  }

  /** {@inheritDoc} */
  public void setContext(final String s) {
    annotation.setContext(s);
  }

  /** {@inheritDoc} */
  public String getCreated() {
    return annotation.getCreated();
  }

  /** {@inheritDoc} */
  public void setCreated(final String s) {
    annotation.setCreated(s);
  }

  /** {@inheritDoc} */
  public String getCreator() {
    return annotation.getCreator();
  }

  /** {@inheritDoc} */
  public void setCreator(final String s) {
    annotation.setCreator(s);
  }

  /** {@inheritDoc} */
  public String getId() {
    return annotation.getId();
  }

  /** {@inheritDoc} */
  public void setId(final String s) {
    annotation.setId(s);
  }

  /** {@inheritDoc} */
  public String getMediator() {
    return annotation.getMediator();
  }

  /** {@inheritDoc} */
  public void setMediator(final String s) {
    annotation.setMediator(s);
  }

  /** {@inheritDoc} */
  public int getState() {
    return annotation.getState();
  }

  /** {@inheritDoc} */
  public void setState(final int i) {
    annotation.setState(i);
  }

  /** {@inheritDoc} */
  public String getSupersededBy() {
    return annotation.getSupersededBy();
  }

  /** {@inheritDoc} */
  public void setSupersededBy(final String s) {
    annotation.setSupersededBy(s);
  }

  /** {@inheritDoc} */
  public String getSupersedes() {
    return annotation.getSupersedes();
  }

  /** {@inheritDoc} */
  public void setSupersedes(final String s) {
    annotation.setSupersedes(s);
  }

  /** {@inheritDoc} */
  public String getTitle() {
    return TextUtils.htmlEncode(annotation.getTitle());
  }

  /** {@inheritDoc} */
  public void setTitle(final String s) {
    annotation.setTitle(s);
  }

  /** {@inheritDoc} */
  public String getType() {
    return annotation.getType();
  }

  /** {@inheritDoc} */
  public void setType(final String s) {
    annotation.setType(s);
  }

  /** {@inheritDoc} */
  public boolean equals(final Object o) {
    return annotation.equals(o);
  }

  /** {@inheritDoc} */
  public int hashCode() {
    return annotation.hashCode();
  }

  public PlosoneAnnotation(final AnnotationInfo annotation) {
    this.annotation = annotation;
  }
}
