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

import org.topazproject.otm.annotations.Embeddable;
import org.topazproject.otm.annotations.Predicate;

/**
 * AnnotationBody implmentation that will get the content from the URL
 * 
 * @author Stephen Cheng
 *
 */
@Embeddable
public class StreamableBody implements AnnotationBody {
  @Predicate(uri=Annotea.NS + "body")
  private URI bodyUrl;  
  
  public StreamableBody(){
    
    
  }
  
  public StreamableBody(URI body) {
    this.bodyUrl = body;
  }
  
  /**
   * @return Returns the bodyUrl.
   */
  public URI getBodyUrl() {
    return bodyUrl;
  }

  /**
   * @param bodyUrl The bodyUrl to set.
   */
  public void setBodyUrl(URI bodyUrl) {
    this.bodyUrl = bodyUrl;
  }

}