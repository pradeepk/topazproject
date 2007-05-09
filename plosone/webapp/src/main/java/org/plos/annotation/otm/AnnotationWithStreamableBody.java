/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.annotation.otm;

import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;
/**
 * Class that will represent an annotation with a streamable body
 * (such as to get content from Fedora)
 * 
 * 
 * @author Stephen Cheng
 *
 */
public class AnnotationWithStreamableBody extends Annotation {

  private StreamableBody body;

  public AnnotationWithStreamableBody() {
  }

  public AnnotationWithStreamableBody(URI id) {
    super(id);
  }

  /**
   * @return Returns the body.
   */
  public AnnotationBody getBody() {
    return body;
  }

  /**
   * @param body The body to set.
   */
  public void setBody(AnnotationBody body) {
    this.body = (StreamableBody)body;
  }
}
