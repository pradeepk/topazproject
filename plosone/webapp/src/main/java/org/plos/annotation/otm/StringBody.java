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

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * Implmentation of String body that simply stores the content as a string
 * 
 * @author stevec
 *
 */
@Entity (model="ri", type = Rdf.topaz + "RatingContent")
public class StringBody {
  @Id @GeneratedValue(uriPrefix = "info:doi/10.1371/commentContent/")
  private String id;
  
  @Predicate (uri = Rdf.topaz + "CommentValue")
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
  
  
  /**
   * Get id.
   *
   * @return id as String.
   */
  public String getId()
  {
      return id;
  }
  
  /**
   * Set id.
   *
   * @param id the value to set.
   */
  public void setId(String id)
  {
      this.id = id;
  }
}
