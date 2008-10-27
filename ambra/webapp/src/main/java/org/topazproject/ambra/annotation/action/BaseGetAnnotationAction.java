/* $HeadURL::                                                                            $
 * $Id:GetAnnotationAction.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.ambra.annotation.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.annotation.service.AnnotationConverter;
import org.topazproject.ambra.annotation.service.ArticleAnnotationService;
import org.topazproject.ambra.annotation.service.WebAnnotation;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

/**
 * Used to fetch an annotation given an id.
 *
 */
@SuppressWarnings("serial")
public abstract class BaseGetAnnotationAction extends BaseActionSupport {
  private String annotationId;
  private WebAnnotation annotation;
  protected ArticleAnnotationService annotationService;
  protected AnnotationConverter converter;

  private static final Log log = LogFactory.getLog(BaseGetAnnotationAction.class);

  @Transactional(readOnly = true)
  @Override
  public String execute() throws Exception {
    try {
      annotation = converter.convert(annotationService.getAnnotation(annotationId), true, true);
    } catch (Exception e) {
      log.error("Could not retreive annotation with id: " + annotationId, e);
      addActionError("Annotation fetching failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Set the annotationId for the annotation to fetch
   * @param annotationId annotationId
   */
  public void setAnnotationId(final String annotationId) {
    this.annotationId = annotationId;
  }

  @RequiredStringValidator(message = "Annotation Id is a required field")
  public String getAnnotationId() {
    return annotationId;
  }

  public WebAnnotation getAnnotation() {
    return annotation;
  }

  /**
   * @return Returns the creatorUserName.
   * @throws ApplicationException if user-name not loaded
   */
  public String getCreatorUserName() throws ApplicationException {
    return annotation.getCreatorName();
  }


  @Required
  public void setAnnotationService(final ArticleAnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  @Required
  public void setAnnotationConverter(AnnotationConverter converter) {
    this.converter = converter;
  }

}
