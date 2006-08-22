/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.annotation.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
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
    addActionMessage("Annotation deleted with id:" + annotationId);
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
   * @return the annotation id
   */
  @RequiredStringValidator(type= ValidatorType.FIELD, fieldName= "annotationId", message="You must specify the id of the annotation that you want to delete")
  public String getAnnotationId() {
    return annotationId;
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
