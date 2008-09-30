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
package org.topazproject.ambra.user.action;

import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.ApplicationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Search a user based on a criteria
 */
public class SearchUserAction extends UserActionSupport {
  private String authId;
  private String accountId;
  private String emailAddress;
  private String name;
  private String[] topazUserIdList;

  private static final Log log = LogFactory.getLog(SearchUserAction.class);

  /**
   * Find user with a given auth id
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String executeFindUserByAuthId() throws Exception {
    try {
      if (log.isDebugEnabled()) {
        log.debug("Finding user with AuthID: " + authId);
      }
      final String topazUserId = getUserService().lookUpUserByAuthId(authId);
      if (null == topazUserId) {
        throw new ApplicationException("No user found with the authid:" + authId);
      }
      topazUserIdList = new String[]{topazUserId};
    } catch (final ApplicationException ex) {
      addFieldError("authId", ex.getMessage());
      return INPUT;
    }

    return SUCCESS;
  }

  /**
   * Find user with a given account id
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String executeFindUserByAccountId() throws Exception {
    try {
      if (log.isDebugEnabled()) {
        log.debug("Finding user with AccountID: " + accountId);
      }
      final String userId = getUserService().lookUpUserByAccountId(accountId);
      if (null == userId) {
        throw new ApplicationException("No user found with the accounid:" + accountId);
      }
      topazUserIdList = new String[]{userId};
    } catch (final ApplicationException ex) {
      addFieldError("accountId", ex.getMessage());
      return INPUT;
    }

    return SUCCESS;
  }

  /**
   * Find user with a given name
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String executeFindUserByName() throws Exception {
    try {
      if (log.isDebugEnabled()) {
        log.debug("Finding user with name: " + name);
      }
      final String userId = getUserService().lookUpUserByDisplayName(name);
      if (null == userId) {
        throw new ApplicationException("No user found with the name:" + name);
      }
      topazUserIdList = new String[]{userId};
    } catch (final ApplicationException ex) {
      addFieldError("name", ex.getMessage());
      return INPUT;
    }

    return SUCCESS;
  }

  /**
   * Find user with a given email address
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String executeFindUserByEmailAddress() throws Exception {
    try {
      if (log.isDebugEnabled()) {
        log.debug("Finding user with email: " + emailAddress);
      }
      final String topazUserId = getUserService().lookUpUserByEmailAddress(emailAddress);
      if (null == topazUserId) {
        throw new ApplicationException("No user found with the email address:" + emailAddress);
      }
      topazUserIdList = new String[]{topazUserId};
    } catch (final ApplicationException ex) {
      addFieldError("emailAddress", ex.getMessage());
      return INPUT;
    }

    return SUCCESS;
  }


  /**
   * Getter for authId.
   * @return Value of authId.
   */
  public String getAuthId() {
    return authId;
  }

  /**
   * Setter for authId.
   * @param authId Value to set for authId.
   */
  public void setAuthId(final String authId) {
    this.authId = authId;
  }

  /**
   * Getter for accountId.
   * @return Value of accountId.
   */
  public String getAccountId() {
    return accountId;
  }

  /**
   * Setter for accountId.
   * @param accountId Value to set for accountId.
   */
  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  /**
   * Getter for emailAddress.
   * @return Value of emailAddress.
   */
  public String getEmailAddress() {
    return emailAddress;
  }

  /**
   * Setter for emailAddress.
   * @param emailAddress Value to set for emailAddress.
   */
  public void setEmailAddress(final String emailAddress) {
    this.emailAddress = emailAddress;
  }

  /**
   * Getter for displayName.
   * @return Value of displayName.
   */
  public String getName() {
    return name;
  }

  /**
   * Setter for displayName
   * @param name Value to set for displayName.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Getter for topazUserIdList.
   * @return Value of topazUserIdList.
   */
  public String[] getTopazUserIdList() {
    return topazUserIdList;
  }
}
