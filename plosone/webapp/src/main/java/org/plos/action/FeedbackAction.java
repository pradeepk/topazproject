/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.action;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.EmailValidator;
import static org.plos.Constants.PLOS_ONE_USER_KEY;
import org.plos.service.PlosoneMailer;
import org.plos.user.PlosOneUser;
import org.plos.user.action.UserActionSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * Send a feedback fromEmail to PLoS ONE
 */
public class FeedbackAction extends UserActionSupport {
  private String page;
  private String fromEmailAddress;
  private String note;
  private String subject;
  private String name;
  private PlosoneMailer plosoneMailer;

  /**
   * Render the page with the values passed in
   * @return webwork status
   * @throws Exception Exception
   */
  public String executeRender() throws Exception {
    final PlosOneUser plosOneUser = (PlosOneUser) getSessionMap().get(PLOS_ONE_USER_KEY);
    if (null != plosOneUser) {
      name = plosOneUser.getDisplayName();
      fromEmailAddress = plosOneUser.getEmail();
    }

    return SUCCESS;
  }

  /**
   * @return webwork status
   * @throws Exception Exception
   */
  public String executeSend() throws Exception {
    if (!validates()) return INPUT;

    final Map<String, String> mapFields = new HashMap<String, String>();
    mapFields.put("page", page);
    mapFields.put("subject", subject);
    mapFields.put("name", name);
    mapFields.put("fromEmailAddress", fromEmailAddress);
    mapFields.put("note", note);
    plosoneMailer.sendFeedback(fromEmailAddress, mapFields);
    return SUCCESS;
  }

  private boolean validates() {
    boolean isValid = true;
    if (StringUtils.isBlank(subject)) {
      addFieldError("subject", "Subject cannot be empty");
      isValid = false;
    }
    if (StringUtils.isBlank(name)) {
      addFieldError("name", "Name cannot be empty");
      isValid = false;
    }
    if (StringUtils.isBlank(fromEmailAddress)) {
      addFieldError("fromEmailAddress", "Email address cannot be empty");
      isValid = false;
    } else if (!EmailValidator.getInstance().isValid(fromEmailAddress)) {
      addFieldError("fromEmailAddress", "Invalid email address");
      isValid = false;
    }
    if (StringUtils.isBlank(note)) {
      addFieldError("note", "Message cannot be empty");
      isValid = false;
    }

    return isValid;
  }

  /**
   * Setter for plosoneMailer.
   * @param plosoneMailer Value to set for plosoneMailer.
   */
  public void setPlosoneMailer(final PlosoneMailer plosoneMailer) {
    this.plosoneMailer = plosoneMailer;
  }

  /**
   * Getter for page.
   * @return Value of page.
   */
  public String getPage() {
    return page;
  }

  /**
   * Setter for page.
   * @param page Value to set for page.
   */
  public void setPage(final String page) {
    this.page = page;
  }

  /**
   * Getter for fromEmailAddress.
   * @return Value of fromEmailAddress.
   */
  public String getFromEmailAddress() {
    return fromEmailAddress;
  }

  /**
   * Setter for fromEmailAddress.
   * @param fromEmailAddress Value to set for fromEmailAddress.
   */
  public void setFromEmailAddress(final String fromEmailAddress) {
    this.fromEmailAddress = fromEmailAddress;
  }

  /**
   * Getter for name.
   * @return Value of name.
   */
  public String getName() {
    return name;
  }

  /**
   * Setter for name.
   * @param name Value to set for name.
   */
  public void setName(final String name) {
    this.name = name;
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
   * Getter for subject.
   * @return Value of subject.
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Setter for subject.
   * @param subject Value to set for subject.
   */
  public void setSubject(final String subject) {
    this.subject = subject;
  }
}
