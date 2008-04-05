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

package org.plos.admin.service;

import java.net.URI;
/**
 * This is the exception used by the ImageResizeService to indicate the
 * failure of an operation.
 *
 * @author stevec
 */
public class ImageResizeException extends Exception {
  private URI articleURI;

  public ImageResizeException (final Throwable cause) {
    super(cause);
  }

  public ImageResizeException (final URI inArticleURI, final Throwable cause) {
    super(cause);
    this.articleURI = inArticleURI;
  }

  public ImageResizeException (final URI inArticleURI) {
    this.articleURI = inArticleURI;
    //this.imageURI = inImageURI;
  }

  public ImageResizeException (String message) {
    super(message);
  }

  public ImageResizeException (String message, Throwable cause) {
    super (message, cause);
  }

  /**
   * @return Returns the articleURI.
   */
  public URI getArticleURI() {
    return articleURI;
  }

  /**
   * @param articleURI The articleURI to set.
   */
  public void setArticleURI(URI articleURI) {
    this.articleURI = articleURI;
  }

}
