package org.plos.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Plos implementation of the messaging service.
 * $HeadURL: $
 * @version: $Id: $
 */
public class PlosRegistrationMessagingService implements RegistrationMessagingService {
  private static final Log log = LogFactory.getLog(PlosRegistrationMessagingService.class);

  public void sendForgotPasswordVerificationEmail(final String emailAddress, final String forgotPasswordToken) {
    log.debug("Message should be sent to emailAddress:" + emailAddress +", forgotPasswordToken:" + forgotPasswordToken);
  }
}
