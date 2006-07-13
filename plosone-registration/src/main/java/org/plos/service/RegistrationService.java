package org.plos.service;

import org.plos.User;

/**
 * $HeadURL$
 * @version: $Id$
 */
public interface RegistrationService {
  User createUser(final String emailAddress, final String password);

  User getUser(final String emailAddress);

  void setVerified(final User user);

  void deactivate(final User user);

  void verifyUser(final String emailAddress, final String emailVerificationToken);

  void sendForgotPasswordMessage(final String emailAddress);
}
