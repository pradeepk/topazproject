package org.plos.service;

import org.plos.registration.User;

/**
 * $HeadURL$
 * @version: $Id$
 */
public interface RegistrationService {
  User createUser(final String loginName, final String password);

  User getUserWithLoginName(final String loginName);

  void setVerified(final User user);

  void deactivate(final User user);

  void verifyUser(final String loginName, final String emailVerificationToken);

  void sendForgotPasswordMessage(final String loginName);
}
