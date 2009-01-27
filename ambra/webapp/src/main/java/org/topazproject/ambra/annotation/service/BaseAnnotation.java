/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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
package org.topazproject.ambra.annotation.service;

import java.util.Date;

import org.topazproject.ambra.models.Annotea;
import org.topazproject.ambra.models.Blob;
import org.topazproject.ambra.util.TextUtils;

import com.googlecode.jsonplugin.annotations.JSON;

/**
 * Base class for Annotation and reply.
 *
 * For now it does not bring together all the common attributes as I still prefer delegation for
 * now.  Further uses of these classes on the web layer should clarify the requirements and drive
 * any changes if required.
 *
 * @param <T> the Annotea sub-class being delegated to.
 */
public abstract class BaseAnnotation<T extends Annotea<? extends Blob>> {
  /** An integer constant to indicate a unique value for the  */
  private static final int TRUNCATED_COMMENT_LENGTH = 256;

  protected final T annotea;
  protected final String originalBodyContent;
  protected final String creatorName;

  /**
   * @return the escaped comment.
   */
  public String getComment() {
    return escapeText(getOriginalBodyContent());
  }

  /**
   * @return the url linked and escaped comment.
   */
  public String getCommentWithUrlLinking() {
    return TextUtils.hyperlinkEnclosedWithPTags(getComment());
  }


  /**
   * @return the url linked and escaped comment with a limit of 256 characters.
   */
  public String getEscapedTruncatedComment() {
    String comment = getComment();
    if (comment.length() > TRUNCATED_COMMENT_LENGTH) {
      final String abrsfx = "...";
      final int abrsfxlen = 3;
      // attempt to truncate on a word boundary
      int index = TRUNCATED_COMMENT_LENGTH - 1;

      while (!Character.isWhitespace(comment.charAt(index)) ||
             index > (TRUNCATED_COMMENT_LENGTH - abrsfxlen - 1)) {
        if (--index == 0)
          break;
      }

      if (index == 0)
        index = TRUNCATED_COMMENT_LENGTH - abrsfxlen - 1;

      comment = comment.substring(0, index) + abrsfx;
      assert comment.length() <= TRUNCATED_COMMENT_LENGTH;
    }

    return TextUtils.hyperlinkEnclosedWithPTags(comment, 25);
  }

  /**
   * Escape text so as to avoid any java scripting maliciousness when rendering it on a web page
   * @param text text
   * @return the escaped text
   */
  protected String escapeText(final String text) {
    return TextUtils.escapeHtml(text);
  }

  /**
   * Get created date.
   * @return created as java.util.Date.
   */
  @JSON(serialize = false)
  public Date getCreatedAsDate() {
    return annotea.getCreated();
  }

  /**
   * Get created date.
   * @return created as java.util.Date.
   */
  public long getCreatedAsMillis() {
    Date d = getCreatedAsDate();
    return (d != null) ? d.getTime() : 0;
  }

  /**
   * Get created.
   * @return created as String.
   */
  public String getCreated() {
    return annotea.getCreatedAsString();
  }

  /**
   * Get creator.
   * @return creator as String.
   */
  public String getCreator() {
    return annotea.getCreator();
  }

  /**
   * @return Returns the creatorName.
   * @throws NullPointerException if the creator name was not loaded
   */
  public String getCreatorName() throws NullPointerException {
    if (creatorName == null)
      throw new NullPointerException("Creator name is not looked-up");
    return creatorName;
  }

  /**
   * Get id.
   * @return id as String.
   */
  public String getId() {
    return annotea.getId().toString();
  }

  /**
   * Get mediator.
   * @return mediator as String.
   */
  public String getMediator() {
    return annotea.getMediator();
  }

  /**
   * Get state.
   * @return state as int.
   */
  public int getState() {
    return annotea.getState();
  }

  /**
   * Get annotation type.
   * @return annotation type as String.
   */
  public String getType() {
    return annotea.getType();
  }

  protected String getOriginalBodyContent() throws NullPointerException {
    if (originalBodyContent == null)
      throw new NullPointerException("Body blob is not loaded.");
    return originalBodyContent;
  }

  /**
   * Creates a BaseAnnotation object.
   *
   * @param annotea the annotation
   * @param creatorName the display name of the creator (must be non-null if the view requires it)
   * @param originalBodyContent body as text (must be non-null if the view requires it)
   */
  public BaseAnnotation(T annotea, String creatorName, String originalBodyContent) {
    this.annotea = annotea;
    this.creatorName = creatorName;
    this.originalBodyContent = originalBodyContent;
  }
}
