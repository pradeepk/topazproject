/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.annotation.web;

import com.opensymphony.xwork.ActionSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.annotation.service.AnnotationService;
import org.plos.annotation.service.ApplicationException;
import org.topazproject.ws.annotation.AnnotationInfo;

public class ListAction  extends ActionSupport {
  private AnnotationService annotationService;
  private String target;
  private AnnotationInfo[] annotations;

  private static final Log log = LogFactory.getLog(ListAction.class);

  /**
   * List annotations.
   * @return status
   * @throws Exception
   */
  public String execute() throws Exception {
    try {
      annotations = annotationService.listAnnotations(target);
    } catch (final ApplicationException e) {
      log.error(e, e);
      addActionError("Annotation fetching failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * @return a list of annotations
   */
  public AnnotationInfo[] getAnnotations() {
    return annotations;
  }

  /**
   * TODO: move up to a parent class
   * Set the annotations service.
   * @param annotationService annotationService
   */
  public void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * Set the target that it annotates.
   * @param target target
   */
  public void setTarget(final String target) {
    this.target = target;
  }
}
