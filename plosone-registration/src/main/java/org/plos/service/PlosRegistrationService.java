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

  public User createUser(final String emailAddress, final String password) {
    final User user = new UserImpl(emailAddress, password);

    user.setEmailVerificationToken(UniqueTokenGenerator.getUniqueToken());
    user.setVerified(false);
    user.setActive(false);

    saveUser(user);

    return user;
  }

  public User getUser(final String emailAddress) {
    return getUserDAO().findUserWithEmailAddress(emailAddress);
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

  public void verifyUser(final String emailAddress, final String emailVerificationToken) {
    final User user = getUser(emailAddress);
    if (user.isVerified()) {
      throw new ApplicationException("The email address:" + emailAddress + " has already been verified");
    }

    if (!user.getEmailVerificationToken().equals(emailVerificationToken)) {
      throw new ApplicationException("Invalid token. Please use the same link or reply to the email that was sent to you.");
    }

    user.setVerified(true);
    user.setEmailVerificationToken(null);
    user.setActive(true);

    saveUser(user);
  }

  public void sendForgotPasswordMessage(final String emailAddress) {
    final User user = getUserDAO().findUserWithEmailAddress(emailAddress);
    if (null == user) {
      throw new ApplicationException("No user found for the given email address:" + emailAddress);
    }

    user.setResetPasswordToken(UniqueTokenGenerator.getUniqueToken());
    saveUser(user);

    getMessagingService()
            .sendMessage(
              "emailAddress:" + emailAddress + ";" + "passwordToken:" + user.getResetPasswordToken());
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
