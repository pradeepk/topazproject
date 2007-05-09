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

import org.topazproject.otm.annotations.Embeddable;
import org.topazproject.otm.annotations.Predicate;

/**
 * Implmentation of String body that simply stores the content as a string
 * 
 * @author stevec
 *
 */
@Embeddable
public class StringBody implements AnnotationBody{
  @Predicate(uri=Annotea.NS + "body")
  private String value;
  
  public StringBody () {
    
  }
  
  public StringBody(String body) {
    this.value = body;
  }

  /**
   * @return Returns the body.
   */
  public String getValue() {
    return value;
  }

  /**
   * @param body The body to set.
   */
  public void setValue(String body) {
    this.value = body;
  }
  
}
