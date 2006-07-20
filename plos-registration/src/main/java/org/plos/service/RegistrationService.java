package org.plos.service;

import org.plos.registration.User;

/**
 * $HeadURL$
 * @version: $Id$
 */
public interface RegistrationService {
  User createUser(final String loginName, final String password) throws UserAlreadyExistsException;

  User getUserWithLoginName(final String loginName);

  void setVerified(final User user);

  void deactivate(final User user);

  void verifyUser(final String loginName, final String emailVerificationToken) throws VerificationTokenInvalidException, UserAlreadyVerifiedException;

  /**
   * Send a forgot password message.
   * @param loginName
   * @throws NoUserFoundWithGivenLoginNameException
   */
  void sendForgotPasswordMessage(final String loginName) throws NoUserFoundWithGivenLoginNameException;

  /**
   * @param loginName login name
   * @param newPassword new password
   * @param resetPasswordToken reset password token
   */
  void changePassword(final String loginName, final String newPassword, final String resetPasswordToken) throws NoUserFoundWithGivenLoginNameException, VerificationTokenInvalidException;

  /**
   * Return the user with the given loginName and resetPasswordToken
   * @param loginName
   * @param resetPasswordToken
   * @return User
   */
  User getUserWithResetPasswordToken(final String loginName, final String resetPasswordToken) throws NoUserFoundWithGivenLoginNameException, VerificationTokenInvalidException;

  /**
   * Get a UserDAO.
   * @return UserDAO
   */
  UserDAO getUserDAO();

  /**
   * Sets the UserDAO.
   * @param userDAO
   */
  void setUserDAO(UserDAO userDAO);

  /** Get the messaging service */
  RegistrationMessagingService getRegistrationMessagingService();

  /**
   * Set the messaging service
   * @param registrationMessagingService
   */
  void setRegistrationMessagingService (RegistrationMessagingService registrationMessagingService);
}
