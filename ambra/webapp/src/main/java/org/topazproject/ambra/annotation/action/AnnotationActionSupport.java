/* $HeadURL::                                                                            $
 * $Id:AnnotationActionSupport.java 722 2006-10-02 16:42:45Z viru $
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

import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.annotation.service.AnnotationService;

/**
 * To be subclassed by Action classes for Annotations and Replies that can use
 * common stuff among them
 */
@SuppressWarnings("serial")
public abstract class AnnotationActionSupport extends BaseActionSupport {
  private AnnotationService annotationService;

  /**
   * Set the annotations service.
   * @param annotationService annotationService
   */
  public void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * @return the AnnotationService
   */
  protected AnnotationService getAnnotationService() {
    return annotationService;
  }

}
