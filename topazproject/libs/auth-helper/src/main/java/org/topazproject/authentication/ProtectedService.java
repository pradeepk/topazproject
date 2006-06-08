package org.topazproject.authentication;

/**
 * An interface to abstract out the authentication mechanism used to access a service.
 *
 * @author Pradeep Krishnan
 */
public interface ProtectedService {
  /**
   * Gets the Uri of the service.
   *
   * @return Returns the service uri
   */
  public String getServiceUri();

  /**
   * Tests to see if this service requires a username/password pair.
   *
   * @return Returns true if this service requires a user-name and password.
   */
  public boolean requiresUserNamePassword();

  /**
   * Gets the username to use to authenticate with the service.
   *
   * @return Returns the username or null
   */
  public String getUserName();

  /**
   * Gets the password to use to authenticate with the service.
   *
   * @return Returns the password or null
   */
  public String getPassword();
}
