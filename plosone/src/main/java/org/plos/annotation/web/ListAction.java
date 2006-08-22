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
import org.topazproject.ws.annotation.AnnotationInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ListAction  extends ActionSupport {
  private AnnotationService annotationService;
  private String target;
  private AnnotationInfo[] annotations;
  private Map<String, String> annotationBodyContent;

  private static final Log log = LogFactory.getLog(ListAction.class);

  /**
   * List annotations.
   * @return status
   * @throws Exception
   */
  public String execute() throws Exception {
    try {
      annotations = annotationService.listAnnotations(target);

      populateAnnotationBodyContent();
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

  /**
   * @return the target of the annotation
   */
  @RequiredStringValidator(type= ValidatorType.FIELD, fieldName= "target", message="You must specify the target that you want to list the annotations for")
  public String getTarget() {
    return target;
  }

  /**
   * Get the body content for a given annotation id.
   * @return content of the annotation
   */
  public Map<String, String> getAnnotationBodyContent() {
    return annotationBodyContent;
  }

  private void populateAnnotationBodyContent() throws IOException {
    annotationBodyContent = new HashMap<String, String>(annotations.length);
    for (AnnotationInfo annotation : annotations) {
      annotationBodyContent.put(annotation.getId(), annotationService.getBody(annotation.getBody()));
    }
  }

}
