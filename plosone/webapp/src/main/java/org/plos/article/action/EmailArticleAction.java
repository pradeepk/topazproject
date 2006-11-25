/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.action;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.EmailValidator;
import static org.plos.Constants.PLOS_ONE_USER_KEY;
import org.plos.service.PlosoneMailer;
import org.plos.user.PlosOneUser;
import org.plos.user.action.UserActionSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * Email the article to another user.
 */
public class EmailArticleAction extends UserActionSupport {
  private String articleURI;
  private String emailTo;
  private String emailFrom;
  private String senderName;
  private String note;
  private PlosoneMailer plosoneMailer;

  /**
   * Render the page with the values passed in
   * @return webwork status
   * @throws Exception Exception
   */
  public String executeRender() throws Exception {
    final PlosOneUser plosOneUser = (PlosOneUser) getSessionMap().get(PLOS_ONE_USER_KEY);
    if (null != plosOneUser) {
      senderName = plosOneUser.getDisplayName();
      emailFrom = plosOneUser.getEmail();
    }

    return SUCCESS;
  }

  /**
   * Send the email
   * @return webwork status
   * @throws Exception Exception
   */
  public String executeSend() throws Exception {
    if (!validates()) return INPUT;

    final Map<String, String> mapFields = new HashMap<String, String>();
    mapFields.put("articleURI", articleURI);
    mapFields.put("senderName", senderName);
    mapFields.put("note", note);
    plosoneMailer.sendEmailThisArticleEmail(emailTo, emailFrom, mapFields);

    return SUCCESS;
  }

  private boolean validates() {
    boolean isValid = true;
    if (StringUtils.isBlank(articleURI)) {
      addFieldError("articleURI", "Article URI cannot be empty");
      isValid = false;
    }
    if (StringUtils.isBlank(emailFrom)) {
      addFieldError("emailFrom", "Your email address cannot be empty");
      isValid = false;
    }
    if (!EmailValidator.getInstance().isValid(emailFrom)) {
      addFieldError("emailFrom", "Invalid email address");
      isValid = false;
    }
    if (StringUtils.isBlank(emailTo)) {
      addFieldError("emailTo", "To email address cannot be empty");
      isValid = false;
    }
    if (!EmailValidator.getInstance().isValid(emailTo)) {
      addFieldError("emailTo", "Invalid email address");
      isValid = false;
    }
    if (StringUtils.isBlank(senderName)) {
      addFieldError("senderName", "Your name cannot be empty");
      isValid = false;
    }
    if (StringUtils.isBlank(note)) {
      addFieldError("note", "Note cannot be empty");
      isValid = false;
    }

    return isValid;
  }

  /**
   * Getter for articleURI.
   * @return Value of articleURI.
   */
  public String getArticleURI() {
    return articleURI;
  }

  /**
   * Setter for articleURI.
   * @param articleURI Value to set for articleURI.
   */
  public void setArticleURI(final String articleURI) {
    this.articleURI = articleURI;
  }

  /**
   * Getter for emailFrom.
   * @return Value of emailFrom.
   */
  public String getEmailFrom() {
    return emailFrom;
  }

  /**
   * Setter for emailFrom.
   * @param emailFrom Value to set for emailFrom.
   */
  public void setEmailFrom(final String emailFrom) {
    this.emailFrom = emailFrom;
  }

  /**
   * Getter for emailTo.
   * @return Value of emailTo.
   */
  public String getEmailTo() {
    return emailTo;
  }

  /**
   * Setter for emailTo.
   * @param emailTo Value to set for emailTo.
   */
  public void setEmailTo(final String emailTo) {
    this.emailTo = emailTo;
  }

  /**
   * Getter for note.
   * @return Value of note.
   */
  public String getNote() {
    return note;
  }

  /**
   * Setter for note.
   * @param note Value to set for note.
   */
  public void setNote(final String note) {
    this.note = note;
  }

  /**
   * Getter for senderName.
   * @return Value of senderName.
   */
  public String getSenderName() {
    return senderName;
  }

  /**
   * Setter for senderName.
   * @param senderName Value to set for senderName.
   */
  public void setSenderName(final String senderName) {
    this.senderName = senderName;
  }

  /**
   * Setter for plosoneMailer.
   * @param plosoneMailer Value to set for plosoneMailer.
   */
  public void setPlosoneMailer(final PlosoneMailer plosoneMailer) {
    this.plosoneMailer = plosoneMailer;
  }
}
