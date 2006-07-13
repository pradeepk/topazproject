package org.plos.registration;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.Date;


@Entity
@Table (name = "plos_user")
public class UserImpl implements User {
  private String emailAddress;

  @Id @GeneratedValue(strategy= GenerationType.AUTO)
  private String id;

  @Column (nullable = false)
  private String password;

  @Column (nullable = false)
  private boolean verified;

  @Column (nullable = false)
  private boolean active;
  private String emailVerificationToken;

//  @Column (nullable = false)
  private Date createdOn;

//  @Column (nullable = false)
  private Date updatedOn;

  private String resetPasswordToken;

  public UserImpl() {
  }

  public UserImpl(final String emailAddress, final String password) {
    this.emailAddress = emailAddress;
    this.password = password;
  }

  @Transactional(readOnly=true)
  public String getEmailAddress() {
    return emailAddress;
  }

  @Transactional(propagation= Propagation.MANDATORY)
  public void setEmailAddress(final String emailAddress) {
    this.emailAddress = emailAddress;
  }

  @Transactional(readOnly=true)
  public String getPassword() {
    return password;
  }

  @Transactional(propagation= Propagation.MANDATORY)
  public void setPassword(final String password) {
    this.password = password;
  }

  @Transactional(propagation= Propagation.MANDATORY)
  public void setVerified(final boolean verified) {
    this.verified = verified;
  }

  @Transactional(readOnly=true)
  public boolean isVerified() {
    return verified;
  }

  @Transactional(propagation= Propagation.MANDATORY)
  public void setActive(final boolean active) {
    this.active = active;

  }

  @Transactional(readOnly=true)
  public boolean isActive() {
    return active;
  }

  @Transactional(readOnly=true)
  public String getId() {
    return id;
  }

  @Transactional(propagation= Propagation.MANDATORY)
  public void setId(final String id) {
    this.id = id;
  }

  @Transactional(readOnly=true)
  public String getEmailVerificationToken() {
    return emailVerificationToken;
  }

  @Transactional(propagation= Propagation.MANDATORY)
  public void setEmailVerificationToken(final String emailVerificationToken) {
    this.emailVerificationToken = emailVerificationToken;
  }

  @Transactional(readOnly=true)
  public Date getCreatedOn() {
    return createdOn;
  }

  @Transactional(propagation= Propagation.MANDATORY)
  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  @Transactional(readOnly=true)
  public Date getUpdatedOn() {
    return updatedOn;
  }

  @Transactional(propagation= Propagation.MANDATORY)
  public void setUpdatedOn(Date updatedOn) {
    this.updatedOn = updatedOn;
  }

  @Transactional(readOnly=true)
  public String getResetPasswordToken() {
    return resetPasswordToken;
  }

  @Transactional(propagation= Propagation.MANDATORY)
  public void setResetPasswordToken(String resetPasswordToken) {
    this.resetPasswordToken = resetPasswordToken;
  }
}