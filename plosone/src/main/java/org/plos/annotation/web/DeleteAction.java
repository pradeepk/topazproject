/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.annotation.web;

import com.opensymphony.xwork.ActionSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.annotation.service.AnnotationService;
import org.plos.annotation.service.ApplicationException;

public class DeleteAction extends ActionSupport {
  private AnnotationService annotationService;
  private String annotationId;
  private boolean deletePreceding;

  private static final Log log = LogFactory.getLog(DeleteAction.class);

  /**
   * Annotation deletion action.
   * @return status
   * @throws Exception
   */
  public String execute() throws Exception {
    try {
      annotationService.deleteAnnotation(annotationId, deletePreceding);
    } catch (final ApplicationException e) {
      log.error(e, e);
      addActionError("Annotation deletion failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Set the annotation Id.
   * @param annotationId annotationId
   */
  public void setAnnotationId(final String annotationId) {
    this.annotationId = annotationId;
  }

  public void setDeletePreceding(final boolean deletePreceding) {
    this.deletePreceding = deletePreceding;
  }

  /**
   * TODO: move up to a parent class
   * Set the annotations service.
   * @param annotationService annotationService
   */
  public void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }
}
