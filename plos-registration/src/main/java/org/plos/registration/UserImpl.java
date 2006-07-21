package org.plos.registration;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;

/**
 * $HeadURL: $
 * @version: $Id: $
 *
 * Implementation for Plos Reistration User
 */

@Entity
@Table (name = "plos_user")
public class UserImpl implements User {
  // TODO: Make this field a GUID generated value like the tokens
  @Id @GeneratedValue(strategy= GenerationType.AUTO)
  private String id;

  @Column  (unique = true)
  private String loginName;

  @Column (nullable = false)
  private String password;

  @Column (nullable = false)
  private boolean verified;

  @Column (nullable = false)
  private boolean active;
  private String emailVerificationToken;

// TODO:  @Column (nullable = false)
  private Date createdOn;

// TODO: @Column (nullable = false)
  private Date updatedOn;

  private String resetPasswordToken;

  public UserImpl() {
  }

  /**
    * Creates a new UserImpl object.
    *
    * @param loginName The user login name
    * @param password The user password
    */
  public UserImpl(final String loginName, final String password) {
    this.loginName = loginName;
    this.password = password;
  }

  /**
   * @see org.plos.registration.User#getLoginName()
   */
  @Transactional(readOnly=true)
  public String getLoginName() {
    return loginName;
  }

  /**
   * @see User#setLoginName(String)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setLoginName(final String loginName) {
    this.loginName = loginName;
  }

  /**
   * @see org.plos.registration.User#getPassword()
   */
  @Transactional(readOnly=true)
  public String getPassword() {
    return password;
  }

  /**
   * @see User#setPassword(String)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setPassword(final String password) {
    this.password = password;
  }

  /**
   * @see User#setVerified(boolean)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setVerified(final boolean verified) {
    this.verified = verified;
  }

  /**
   * @see org.plos.registration.User#isVerified()
   */
  @Transactional(readOnly=true)
  public boolean isVerified() {
    return verified;
  }

  /**
   * @see User#setActive(boolean)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setActive(final boolean active) {
    this.active = active;

  }

  /**
   * @see org.plos.registration.User#isActive()
   */
  @Transactional(readOnly=true)
  public boolean isActive() {
    return active;
  }

  /**
   * @see org.plos.registration.User#getId()
   */
  @Transactional(readOnly=true)
  public String getId() {
    return id;
  }

  /**
   * @see User#setId(String)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setId(final String id) {
    this.id = id;
  }

  /**
   * @see org.plos.registration.User#getEmailVerificationToken()
   */
  @Transactional(readOnly=true)
  public String getEmailVerificationToken() {
    return emailVerificationToken;
  }

  /**
   * @see User#setEmailVerificationToken(String)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setEmailVerificationToken(final String emailVerificationToken) {
    this.emailVerificationToken = emailVerificationToken;
  }

  /**
   * @see org.plos.registration.User#getCreatedOn()
   */
  @Transactional(readOnly=true)
  public Date getCreatedOn() {
    return createdOn;
  }

  /**
   * @see User#setCreatedOn(java.sql.Date)
   */
  // TODO: set the date
  @Transactional(propagation= Propagation.MANDATORY)
  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  /**
   * @see org.plos.registration.User#getUpdatedOn()
   */
  @Transactional(readOnly=true)
  public Date getUpdatedOn() {
    return updatedOn;
  }

  /**
   * @see User#setUpdatedOn(java.sql.Date)
   */
  // TODO: set the date
  @Transactional(propagation= Propagation.MANDATORY)
  public void setUpdatedOn(Date updatedOn) {
    this.updatedOn = updatedOn;
  }

  /**
   * @see org.plos.registration.User#getResetPasswordToken()
   */
  @Transactional(readOnly=true)
  public String getResetPasswordToken() {
    return resetPasswordToken;
  }

  /**
   * @see User#setResetPasswordToken(String)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setResetPasswordToken(String resetPasswordToken) {
    this.resetPasswordToken = resetPasswordToken;
  }
}