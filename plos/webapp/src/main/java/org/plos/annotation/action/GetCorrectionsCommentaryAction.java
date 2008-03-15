/* $$HeadURL:: $$
 * $$Id: $$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.annotation.action;

import java.util.ArrayList;
import java.util.List;

import org.plos.ApplicationException;
import org.plos.annotation.service.WebAnnotation;

/**
 * Action class to get a list of all commentary for an article and the threads associated
 * with each base comment.
 * 
 * @author jkirton
 */
@SuppressWarnings("serial")
public class GetCorrectionsCommentaryAction extends AbstractCommentaryAction {

  @Override
  protected WebAnnotation[] getAnnotations() throws ApplicationException {
    WebAnnotation[] arr = getAnnotationService().listAnnotations(getTarget());
    // for now just filter them here
    // TODO push this filtering into the service layer
    final List<WebAnnotation> list = new ArrayList<WebAnnotation>(arr.length);
    for(WebAnnotation wa : arr) {
      assert wa.getType() != null;
      if(wa.getType().toLowerCase().indexOf("correction") >= 0) {
        list.add(wa);
      }
    }
    return list.toArray( new WebAnnotation[ list.size() ] );
  }

  @Override
  protected String useCaseDescriptor() {
    return "Corrections Commentary";
  }
}
