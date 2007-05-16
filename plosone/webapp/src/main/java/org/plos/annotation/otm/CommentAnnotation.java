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
import org.topazproject.otm.annotations.Predicate;


/**
 * Basic comment annoataion where the String body content is stored in mulgara
 * 
 * @author Stephen Cheng
 *
 */

@Entity (type = Annotea.NS + "Comment")
public class  CommentAnnotation extends AbstractAnnotation {

  @Predicate(uri=Annotea.NS + "body")
  private StringBody body;
  
  public CommentAnnotation() {
  }


  public CommentAnnotation(URI id) {
    super(id);
  }
  
  public CommentAnnotation (String comment) {
    setBody(new StringBody(comment));
  }
  
  /**
   * @return Returns the body.
   */
  public StringBody getBody() {
    return body;
  }

  /**
   * @param body The body to set.
   */
  public void setBody(StringBody body) {
    this.body = (StringBody)body;
  }
  
  
  public String retrieveComment() {
    if (body != null) {
      return body.getValue();
    }
    return null;
  }
  
  public void assignComment(String comment){
    if (body == null)
      body = new StringBody(comment);
    else
      body.setValue(comment);
  }

}
