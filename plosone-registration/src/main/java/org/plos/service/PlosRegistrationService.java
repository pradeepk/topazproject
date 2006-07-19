package org.plos.service;

import org.plos.ApplicationException;
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

  public User createUser(final String loginName, final String password) throws UserAlreadyExistsException {
    if (null == getUserWithLoginName(loginName)) {
      final User user = new UserImpl(loginName, password);

      user.setEmailVerificationToken(TokenGenerator.getUniqueToken());
      user.setVerified(false);
      user.setActive(false);

      saveUser(user);

      return user;
    } else {
      throw new UserAlreadyExistsException();
    }
  }

  public User getUserWithLoginName(final String loginName) {
    return getUserDAO().findUserWithLoginName(loginName);
  }

  public void setVerified(final User user) {
    user.setVerified(true);
    user.setActive(true);

    saveUser(user);
  }

  public void deactivate(final User user) {
    user.setVerified(false);
    user.setActive(false);

    saveUser(user);
  }

  public void verifyUser(final String loginName, final String emailVerificationToken) {
    final User user = getUserWithLoginName(loginName);
    if (user.isVerified()) {
      throw new ApplicationException("The login name:" + loginName + " has already been verified");
    }

    if (!user.getEmailVerificationToken().equals(emailVerificationToken)) {
      throw new ApplicationException("Invalid token. Please use the same link or reply to the email that was sent to you.");
    }

    user.setVerified(true);
    user.setEmailVerificationToken(null);
    user.setActive(true);

    saveUser(user);
  }

  public void sendForgotPasswordMessage(final String loginName) {
    final User user = getUserDAO().findUserWithLoginName(loginName);
    if (null == user) {
      throw new NoUserFoundWithGivenLoginNameException();
    }

    user.setResetPasswordToken(TokenGenerator.getUniqueToken());
    saveUser(user);

    getMessagingService()
            .sendMessage(
              "email:" + loginName + ";" + "passwordToken:" + user.getResetPasswordToken());
  }

  public void changePassword(final String loginName, final String newPassword, final String resetPasswordToken) {
    final User user = getUserWithResetPasswordToken(loginName, resetPasswordToken);

    user.setPassword(newPassword);
    user.setResetPasswordToken(null);
    saveUser(user);
  }

  public User getUserWithResetPasswordToken(final String loginName, final String resetPasswordToken) {
    final User user = getUserDAO().findUserWithLoginName(loginName);

    if (null == user) {
      throw new NoUserFoundWithGivenLoginNameException();
    }

    if (user.getResetPasswordToken().equals(resetPasswordToken)) {
      return user;
    } else {
      throw new ResetPasswordTokenInvalidException();
    }
  }

  private EmailMessagingService getMessagingService() {
    return new EmailMessagingService() {
      public void sendMessage(final String message) {
        log("message sent");
      }
    };
  }

  private void log(final String logMessage) {
    System.out.println(logMessage);
  }

  private void saveUser(final User user) {
    getUserDAO().saveOrUpdate(user);
  }

  public UserDAO getUserDAO() {
    return userDAO;
  }

  public void setUserDAO(final UserDAO userDAO) {
    this.userDAO = userDAO;
  }
}

