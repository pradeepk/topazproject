/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.service.RegistrationMessagingService;

/**
 * Plos implementation of the messaging service.
 */
public class PlosRegistrationMessagingService implements RegistrationMessagingService {
  private static final Log log = LogFactory.getLog(PlosRegistrationMessagingService.class);

  /**
   * @see RegistrationMessagingService#sendForgotPasswordVerificationEmail(String, String) 
   */
  public void sendForgotPasswordVerificationEmail(final String emailAddress, final String forgotPasswordToken) {
    log.debug("Message should be sent to emailAddress:" + emailAddress +", forgotPasswordToken:" + forgotPasswordToken);
  }
}
