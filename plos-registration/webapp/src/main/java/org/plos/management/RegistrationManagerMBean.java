/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.management;

/**
 * Management Bean for Registration
 */

public interface RegistrationManagerMBean {

  /**
   * Deletes user from registry
   *
   * @param userName the user name
   */
  public String deleteUser(String userName);


  /**
   * Changes users password
   *
   * @param userName The user name
   * @return Success or failure message
   */
  public String changeUserPassword(String userName, String password);


  /**
   * Activate user
   *
   * @param userName The user name
   * @return Success or failure message
   */
  public String activateUser(String userName);


  /**
   * Deactivate user
   *
   * @param userName The user name
   * @return Success or failure message
   */
  public String deactivateUser(String userName);

}

