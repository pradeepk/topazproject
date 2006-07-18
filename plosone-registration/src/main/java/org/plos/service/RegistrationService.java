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

  /**
   * @param loginName login name
   * @param newPassword new password
   * @param resetPasswordToken reset password token
   */
  void changePassword(final String loginName, final String newPassword, final String resetPasswordToken);

  /**
   * Return the user with the given loginName and resetPasswordToken
   * @param loginName
   * @param resetPasswordToken
   * @return User
   */
  User getUserWithResetPasswordToken(final String loginName, final String resetPasswordToken);

  /**
   * @return UserDAO
   */
  UserDAO getUserDAO();

  /**
   * Sets the UserDAO.
   * @param userDAO
   */
  void setUserDAO(UserDAO userDAO);
}
