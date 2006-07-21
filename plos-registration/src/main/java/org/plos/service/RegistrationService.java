package org.plos.service;

import org.plos.registration.User;

/**
 * $HeadURL$
 * @version: $Id$
 */
public interface RegistrationService {
  /**
   * Create user.
   * @param loginName
   * @param password
   * @return created user
   * @throws UserAlreadyExistsException
   */
  User createUser(final String loginName, final String password) throws UserAlreadyExistsException;

  /**
   * Get user with loginName
   * @param loginName
   * @return user
   */
  User getUserWithLoginName(final String loginName);

  /**
   * Set the user as verified.
   * @param user user
   */
  void setVerified(final User user);

  /**
   * Deactivate the user.
   * @param user user
   */
  void deactivate(final User user);

  /**
   * Verify the users account
   * @param loginName
   * @param emailVerificationToken
   * @throws VerificationTokenInvalidException
   * @throws UserAlreadyVerifiedException
   */
  void verifyUser(final String loginName, final String emailVerificationToken) throws VerificationTokenInvalidException, UserAlreadyVerifiedException;

  /**
   * Send a forgot password message.
   * @param loginName
   * @throws NoUserFoundWithGivenLoginNameException
   */
  void sendForgotPasswordMessage(final String loginName) throws NoUserFoundWithGivenLoginNameException;

  /**
   * Change password request.
   * @param loginName login name
   * @param newPassword new password
   * @param resetPasswordToken reset password token
   * @throws NoUserFoundWithGivenLoginNameException
   */
  void changePassword(final String loginName, final String newPassword, final String resetPasswordToken) throws NoUserFoundWithGivenLoginNameException, VerificationTokenInvalidException;

  /**
   * Return the user with the given loginName and resetPasswordToken
   * @param loginName
   * @param resetPasswordToken
   * @return User
   * @throws NoUserFoundWithGivenLoginNameException
   */
  User getUserWithResetPasswordToken(final String loginName, final String resetPasswordToken) throws NoUserFoundWithGivenLoginNameException, VerificationTokenInvalidException;

  /**
   * Get a UserDAO.
   * @return UserDAO
   */
  UserDAO getUserDAO();

  /**
   * Sets the UserDAO.
   * @param userDAO userDAO
   */
  void setUserDAO(UserDAO userDAO);

  /**
   * Get the messaging service
   * @return RegistrationMessagingService
   */
  RegistrationMessagingService getRegistrationMessagingService();

  /**
   * Set the messaging service
   * @param registrationMessagingService registrationMessagingService
   */
  void setRegistrationMessagingService (RegistrationMessagingService registrationMessagingService);
}
