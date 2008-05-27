/* $$HeadURL::                                                                            $$
 * $$Id$$
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

package org.plos.annotation.action;

import org.plos.ApplicationException;
import org.plos.annotation.service.WebAnnotation;

/**
 * Action class to get a list of all commentary for an article and the threads
 * associated with each base comment.
 * 
 * @author Stephen Cheng
 * @author jkirton
 */
@SuppressWarnings("serial")
public class GetCommentaryAction extends AbstractCommentaryAction {

  /**
   * For this use case, we provide only comment (non-correction) related
   * annotations
   */
  @Override
  protected WebAnnotation[] getAnnotations() throws ApplicationException {
    return getAnnotationService().listComments(getTarget());
  }

  @Override
  protected String useCaseDescriptor() {
    return "all Commentary";
  }
}
