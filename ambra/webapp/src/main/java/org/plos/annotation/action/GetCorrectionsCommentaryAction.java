/* $$HeadURL:: $$
 * $$Id: $$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
 * Action class to get a list of all corrections for an article and the threads
 * associated with them.
 * 
 * @author jkirton
 */
@SuppressWarnings("serial")
public class GetCorrectionsCommentaryAction extends AbstractCommentaryAction {

  @Override
  protected WebAnnotation[] getAnnotations() throws ApplicationException {
    return getAnnotationService().listCorrections(getTarget());
  }

  @Override
  protected String useCaseDescriptor() {
    return "Corrections Commentary";
  }
}
