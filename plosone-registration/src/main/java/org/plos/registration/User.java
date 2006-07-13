package org.plos.registration;

import java.sql.Date;

/**
 * $HeadURL: $
 *
 * @version: $Id: $
 */
public interface User {

  String getEmailAddress();

  void setEmailAddress(String emailAddress);

  String getPassword();

  void setPassword(String password);

  void setVerified(boolean verified);

  boolean isVerified();

  void setActive(boolean active);

  boolean isActive();

  String getId();

  void setId(String id);

  String getEmailVerificationToken();

  void setEmailVerificationToken(String emailVerificationToken);

  Date getCreatedOn();

  void setCreatedOn(Date createdOn);

  Date getUpdatedOn();

  void setUpdatedOn(Date updatedOn);

  String getResetPasswordToken();

  void setResetPasswordToken(String resetPasswordToken);
}
