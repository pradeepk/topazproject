package org.plos.service;

import com.opensymphony.util.GUID;
import org.plos.ApplicationException;
import org.plos.registration.User;
import org.plos.registration.UserImpl;

/**
 * $HeadURL$
 * @version: $Id$
 */
public class PlosRegistrationService implements RegistrationService {
  private UserDAO userDAO;

  public User createUser(final String loginName, final String password) {
    if (null == getUserWithLoginName(loginName)) {
      final User user = new UserImpl(loginName, password);

      user.setEmailVerificationToken(UniqueTokenGenerator.getUniqueToken());
      user.setVerified(false);
      user.setActive(false);

      saveUser(user);

      return user;
    } else {
      throw new ApplicationException("User already exists for the loginName address: " + loginName);
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

    user.setResetPasswordToken(UniqueTokenGenerator.getUniqueToken());
    saveUser(user);

    getMessagingService()
            .sendMessage(
              "email:" + loginName + ";" + "passwordToken:" + user.getResetPasswordToken());
  }

  public void changePassword(final String loginName, final String password) {
    final User user = getUserDAO().findUserWithLoginName(loginName);
    user.setPassword(password);
    saveUser(user);
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

  private UserDAO getUserDAO() {
    return userDAO;
  }

  public void setUserDAO(final UserDAO userDAO) {
    this.userDAO = userDAO;
  }
}

class UniqueTokenGenerator {

  public static String getUniqueToken() {
    return GUID.generateGUID();
  }
}
