package org.plos.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.registration.User;
import org.plos.registration.UserImpl;
import org.plos.util.TokenGenerator;

/**
 * Plos registration service implementation.
 * $HeadURL$
 * @version: $Id$
 */
public class PlosRegistrationService implements RegistrationService {
  private UserDAO userDAO;
  private RegistrationMessagingService registrationMessagingService;

  private static final Log log = LogFactory.getLog(PlosPersistenceService.class);

  public User createUser(final String loginName, final String password) throws UserAlreadyExistsException {
    if (null == getUserWithLoginName(loginName)) {
      final User user = new UserImpl(loginName, password);

      user.setEmailVerificationToken(TokenGenerator.getUniqueToken());
      user.setVerified(false);
      user.setActive(false);

      saveUser(user);

      return user;
    } else {
      throw new UserAlreadyExistsException(loginName);
    }
  }

  /**
   * @see RegistrationService#getUserWithLoginName(String)
   */
  public User getUserWithLoginName(final String loginName) {
    return getUserDAO().findUserWithLoginName(loginName);
  }

  /**
   * @see RegistrationService#setVerified(org.plos.registration.User)
   */
  public void setVerified(final User user) {
    user.setVerified(true);
    user.setActive(true);

    saveUser(user);
  }

  /**
   * @see RegistrationService#deactivate(org.plos.registration.User)
   */
  public void deactivate(final User user) {
    user.setVerified(false);
    user.setActive(false);

    saveUser(user);
  }

  /**
   * @see RegistrationService#verifyUser(String, String)
   */
  public void verifyUser(final String loginName, final String emailVerificationToken) throws VerificationTokenInvalidException, UserAlreadyVerifiedException {
    final User user = getUserWithLoginName(loginName);
    if (user.isVerified()) {
      throw new UserAlreadyVerifiedException(loginName);
    }

    if (!user.getEmailVerificationToken().equals(emailVerificationToken)) {
      throw new VerificationTokenInvalidException(loginName, emailVerificationToken);
    }

    user.setVerified(true);
    user.setEmailVerificationToken(null);
    user.setActive(true);

    saveUser(user);
  }

  /**
   * @see RegistrationService#sendForgotPasswordMessage(String)
   */
  public void sendForgotPasswordMessage(final String loginName) throws NoUserFoundWithGivenLoginNameException {
    final User user = getUserDAO().findUserWithLoginName(loginName);
    if (null == user) {
      throw new NoUserFoundWithGivenLoginNameException(loginName);
    }

    user.setResetPasswordToken(TokenGenerator.getUniqueToken());
    saveUser(user);

    getRegistrationMessagingService()
            .sendForgotPasswordVerificationEmail(
                    loginName,
                    user.getResetPasswordToken());
  }

  /**
   * @see RegistrationService#changePassword(String, String, String)
   */
  public void changePassword(final String loginName, final String newPassword, final String resetPasswordToken) throws NoUserFoundWithGivenLoginNameException, VerificationTokenInvalidException {
    final User user = getUserWithResetPasswordToken(loginName, resetPasswordToken);

    user.setPassword(newPassword);
    user.setResetPasswordToken(null);
    saveUser(user);
  }

  /**
   * @see RegistrationService#getUserWithResetPasswordToken(String, String)
   */
  public User getUserWithResetPasswordToken(final String loginName, final String resetPasswordToken) throws NoUserFoundWithGivenLoginNameException, VerificationTokenInvalidException {
    final User user = getUserDAO().findUserWithLoginName(loginName);

    if (null == user) {
      throw new NoUserFoundWithGivenLoginNameException(loginName);
    }

    if (user.getResetPasswordToken().equals(resetPasswordToken)) {
      return user;
    } else {
      throw new VerificationTokenInvalidException(loginName, resetPasswordToken);
    }
  }

  /**
   * @see org.plos.service.RegistrationService#getRegistrationMessagingService()
   */
  public RegistrationMessagingService getRegistrationMessagingService() {
    return registrationMessagingService;
  }

  /**
   * Save or update the user.
   * @param user user
   */
  private void saveUser(final User user) {
    getUserDAO().saveOrUpdate(user);
  }

  /**
   * @see org.plos.service.RegistrationService#getUserDAO()
   */
  public UserDAO getUserDAO() {
    return userDAO;
  }

  /**
   * @see RegistrationService#setUserDAO(UserDAO)
   */
  public void setUserDAO(final UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  /**
   * @see RegistrationService#setRegistrationMessagingService(RegistrationMessagingService)
   */
  public void setRegistrationMessagingService (final RegistrationMessagingService registrationMessagingService) {
    this.registrationMessagingService = registrationMessagingService;
  }
}

