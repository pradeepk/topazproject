/* $HeadURL::                                                                            $
 * $Id$
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
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.annotation.service.ArticleAnnotationService;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

/**
 * Action to delete an annotation
 */
@SuppressWarnings("serial")
public class DeleteAnnotationAction extends BaseActionSupport {
  private String annotationId;
  protected ArticleAnnotationService annotationService;

  private static final Log log = LogFactory.getLog(DeleteAnnotationAction.class);

  /**
   * Delete public annotation.
   * @return status
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String deleteAnnotation() {
    try {
      annotationService.deleteAnnotation(annotationId);
    } catch (final Exception e) {
      log.error("Could not delete annotation: " + annotationId, e);
      addActionError("Annotation deletion failed with error message: " + e.getMessage());
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return ERROR;
    }
    addActionMessage("Annotation marked as deleted with id:" + annotationId);
    return SUCCESS;
  }

  /**
   * Set the annotation Id.
   * @param annotationId annotationId
   */
  public void setAnnotationId(final String annotationId) {
    this.annotationId = annotationId;
  }

  /**
   * @return the annotation id
   */
  @RequiredStringValidator(message="You must specify the id of the annotation that you want to delete")
  public String getAnnotationId() {
    return annotationId;
  }

  @Required
  public void setAnnotationService(final ArticleAnnotationService annotationService) {
    this.annotationService = annotationService;
  }
}
