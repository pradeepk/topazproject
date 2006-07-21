package org.plos.service;

/**
 * $HeadURL$
 * @version: $Id$
 *
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
