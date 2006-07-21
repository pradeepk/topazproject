package org.plos.registration;

import java.sql.Date;

/**
 * $HeadURL: $
 * @version: $Id: $
 *
 * Interface for Plos Reistration User
 */
public interface User {

  /**
   * Get the Login Name
   * @return Login Name
   */
  String getLoginName();

  /**
    * Set the Users Login Name
    * @param email  Set User Login Name
    */
  void setLoginName(String email);

  /**
   * Get the User Password
   * @return User Password
   */
  String getPassword();

  /**
    * Set the User Password
    * @param password
    */
  void setPassword(String password);

  /**
    * Set the User to varified
    * @param  verified
    */
  void setVerified(boolean verified);

  /**
   * Is the user verified
   * @return  Boolean true/false
   */
  boolean isVerified();

  /**
    * Set the User to active
    * @param active
    */
  void setActive(boolean active);

  /**
   * Is the user active
   * @return  Boolean true/false
   */
  boolean isActive();

  /**
   * Get the User Id
   * @return User Id
   */
  String getId();

  /**
    * Set the User Id
    * @param id
    */
  void setId(String id);

  /**
   * Get the Email Verification Token
   * @return Email Verification Token
   */
  String getEmailVerificationToken();

  /**
    * Set the Email Verification Token
    * @param emailVerificationToken
    */
  void setEmailVerificationToken(String emailVerificationToken);

  /**
   * Get the Date the User was created
   * @return User Creation Date
   */
  Date getCreatedOn();

  /**
    * Set the Date the User was created
    * @param createdOn
    */
  void setCreatedOn(Date createdOn);

  /**
   * Get the Date of last update
   * @return Date of last update
   */
  Date getUpdatedOn();

  /**
    * Set the Date of last update
    * @param updatedOn
    */
  void setUpdatedOn(Date updatedOn);

  /**
   * Get the Reset Password Token
   * @return Reset Password Token
   */
  String getResetPasswordToken();

  /**
    * Set the Reset Password Token
    * @param resetPasswordToken
    */
  void setResetPasswordToken(String resetPasswordToken);

}
