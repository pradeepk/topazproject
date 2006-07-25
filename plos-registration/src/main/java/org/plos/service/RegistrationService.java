/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.service;

import org.plos.registration.User;

/**
 *
 */
public interface RegistrationService {
  /**
   * Create user.
   * @param loginName loginName
   * @param password password
   * @return created user
   * @throws UserAlreadyExistsException
   */
  User createUser(final String loginName, final String password) throws UserAlreadyExistsException;

  /**
   * Get user with loginName
   * @param loginName loginName
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
   * @param loginName loginName
   * @param emailVerificationToken emailVerificationToken
   * @throws VerificationTokenInvalidException
   * @throws UserAlreadyVerifiedException
   * @throws NoUserFoundWithGivenLoginNameException
   */
  void verifyUser(final String loginName, final String emailVerificationToken) throws VerificationTokenInvalidException, UserAlreadyVerifiedException, NoUserFoundWithGivenLoginNameException;

  /**
   * Send a forgot password message.
   * @param loginName loginName
   * @throws NoUserFoundWithGivenLoginNameException
   */
  void sendForgotPasswordMessage(final String loginName) throws NoUserFoundWithGivenLoginNameException;

  /**
   * Change password request.
   * @param loginName login name
   * @param newPassword new password
   * @param resetPasswordToken reset password token
   * @throws NoUserFoundWithGivenLoginNameException
   * @throws VerificationTokenInvalidException
   */
  void changePassword(final String loginName, final String newPassword, final String resetPasswordToken) throws NoUserFoundWithGivenLoginNameException, VerificationTokenInvalidException;

  /**
   * Return the user with the given loginName and resetPasswordToken
   * @param loginName loginName
   * @param resetPasswordToken resetPasswordToken
   * @return User
   * @throws NoUserFoundWithGivenLoginNameException
   * @throws VerificationTokenInvalidException
   */
  User getUserWithResetPasswordToken(final String loginName, final String resetPasswordToken) throws NoUserFoundWithGivenLoginNameException, VerificationTokenInvalidException;

}
