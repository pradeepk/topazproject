/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.service;

/**
 * Messaging service for the registration service.
 */
 public interface RegistrationMessagingService {
  /**
   * Send a forgotPasswordToken to the user
   * @param emailAddress emailAddress
   * @param forgotPasswordToken forgotPasswordToken
   */
  void sendForgotPasswordVerificationEmail(final String emailAddress, final String forgotPasswordToken);
}
