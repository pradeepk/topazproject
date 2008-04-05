/* $HeadURL::                                                                            $
 * $Id$
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
package org.plos.annotation.service;

/**
 * Factory to return instances of lazy loaders as required by annotations and replies
 */
public class AnnotationLazyLoaderFactory {

  /**
   * Create an instance of the AnnotationLazyLoader as required for each annotation
   * @param annotation annotation
   * @return an instance of a lazy loader
   */
  public AnnotationLazyLoader create(final AnnotationInfo annotation) {
    return new AnnotationLazyLoader(annotation.getBody());
  }

  /**
   * Create an instance of the AnnotationLazyLoader as required for each reply
   * @param reply reply
   * @return an instance of a lazy loader
   */
  public AnnotationLazyLoader create(final ReplyInfo reply) {
    return new AnnotationLazyLoader(reply.getBody());
  }
}
