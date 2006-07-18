package org.plos.management;

/**
 * $HeadURL: $
 *
 * @version: $Id: $
 */

public interface RegistrationManagerMBean{
  public String deleteUser( String userName );
  public String changeUserPassword( String userName, String password);
  public String activateUser( String userName);
  public String deactivateUser( String userName);
}

