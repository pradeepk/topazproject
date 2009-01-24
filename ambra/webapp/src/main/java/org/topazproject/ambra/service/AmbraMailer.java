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
package org.topazproject.ambra.service;

import org.topazproject.ambra.email.impl.FreemarkerTemplateMailer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AmbraMailer extends FreemarkerTemplateMailer {
  private Map<String, String> emailThisArticleMap;
  private Map<String, String> feedbackEmailMap;

  /**
   * Setter for emailThisArticleMap.
   * @param emailThisArticleMap emailThisArticleMap
   */
  public void setEmailThisArticleMap(final Map<String, String> emailThisArticleMap) {
    this.emailThisArticleMap = Collections.unmodifiableMap(emailThisArticleMap);
  }

  /**
   * Setter for feedbackEmailMap.
   * @param feedbackEmailMap Value to set for feedbackEmailMap.
   */
  public void setFeedbackEmailMap(final Map<String, String> feedbackEmailMap) {
    this.feedbackEmailMap = feedbackEmailMap;
  }

  /**
   * Send an email when the user selects to email an article to a friend
   * @param toEmailAddress toEmailAddress
   * @param fromEmailAddress fromEmailAddress
   * @param mapFields mapFields to fill up the template with the right values
   */
  public void sendEmailThisArticleEmail(final String toEmailAddress, final String fromEmailAddress,
      final Map<String, String> mapFields) {
    final HashMap<String, Object> newMapFields = new HashMap<String, Object>();
    newMapFields.putAll(emailThisArticleMap);
    newMapFields.putAll(mapFields);
    sendEmail(toEmailAddress, fromEmailAddress, newMapFields);
  }

  public void sendFeedback(final String fromEmailAddress, final Map<String, Object> mapFields) {
    final Map<String, Object> newMapFields = new HashMap<String, Object>();
    newMapFields.putAll(feedbackEmailMap);
    newMapFields.putAll(mapFields);
    sendEmail(feedbackEmailMap.get(TO_EMAIL_ADDRESS), fromEmailAddress, newMapFields);
  }
}
