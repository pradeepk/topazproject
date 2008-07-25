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
package org.topazproject.ambra.annotation.service;

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.Constants;
import org.topazproject.ambra.util.TextUtils;

/**
 * Base class for Annotation and reply.
 * For now it does not bring together all the common attributes as I still prefer delegation for now.
 * Further uses of these classes on the web layer should clarify the requirements and drive any changes
 * if required.
 */
public abstract class BaseAnnotation {
  /** An integer constant to indicate a unique value for the  */
  public static final int PUBLIC_MASK = Constants.StateMask.PUBLIC;
  public static final int FLAG_MASK = Constants.StateMask.FLAG;
  public static final int DELETE_MASK = Constants.StateMask.DELETE;
  private static final int TRUNCATED_COMMENT_LENGTH = 256;

  /**
   * @return the escaped comment.
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  public String getComment() throws ApplicationException {
    return escapeText(getOriginalBodyContent());
  }

  /**
   * @return the url linked and escaped comment.
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  public String getCommentWithUrlLinking() throws ApplicationException {
    return TextUtils.hyperlinkEnclosedWithPTags(getComment());
  }


  /**
   * @return the url linked and escaped comment with a limit of 256 characters.
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  public String getEscapedTruncatedComment() throws ApplicationException {
    String comment = getComment();
    if (comment.length() > TRUNCATED_COMMENT_LENGTH) {
      final String abrsfx = "...";
      final int abrsfxlen = 3;
      // attempt to truncate on a word boundary
      int index = TRUNCATED_COMMENT_LENGTH - 1;
      while(!Character.isWhitespace(comment.charAt(index)) || index > (TRUNCATED_COMMENT_LENGTH - abrsfxlen - 1)) {
        if(--index == 0) break;
      }
      if(index == 0) index = TRUNCATED_COMMENT_LENGTH - abrsfxlen - 1;
      comment = comment.substring(0, index) + abrsfx;
      assert comment.length() <= TRUNCATED_COMMENT_LENGTH;
    }
    return TextUtils.hyperlinkEnclosedWithPTags(comment);
  }

  /**
   * @return the original content of the annotation body
   * @throws ApplicationException ApplicationException
   */
  protected abstract String getOriginalBodyContent() throws ApplicationException;

  /**
   * Escape text so as to avoid any java scripting maliciousness when rendering it on a web page
   * @param text text
   * @return the escaped text
   */
  protected String escapeText(final String text) {
    return TextUtils.escapeHtml(text);
  }

  /**
   * Is the Annotation public?
   * @return true if the annotation/reply is public, false if private
   */
  public boolean isPublic() {
    return (getState() & PUBLIC_MASK) == PUBLIC_MASK;
  }

  /**
   * Get state.
   * @return state as int.
   */
  public abstract int getState();

  /**
   * Is the annotation flagged?
   * @return true if the annotation is flagged, false otherwise
   */
  public boolean isFlagged() {
    return (getState() & FLAG_MASK) == FLAG_MASK;
  }

  /**
   * Is the annotation deleted?
   * @return true if the annotation has been deleted.
   */
  public boolean isDeleted() {
    return (getState() & DELETE_MASK) == DELETE_MASK;
  }

}
