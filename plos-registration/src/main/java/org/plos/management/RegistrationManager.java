package org.plos.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.registration.User;
import org.plos.service.UserDAO;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * $HeadURL: $
 *
 * @version: $Id: $
 *
 *  The RegistrationManager is a JMX Bean that allows management of the Plos registration
 */

@ManagedResource(objectName = "bean:name=RegistrationManager", description = "Registration Manager", log = true,
        logFile = "jmx.log", currencyTimeLimit = 15, persistPolicy = "OnUpdate", persistPeriod = 200,
        persistLocation = "foo", persistName = "bar")

public class RegistrationManager implements RegistrationManagerMBean
{
  private static final Log logger = LogFactory.getLog(RegistrationManager.class);

  private UserDAO userDAO;

  /**
    * Set the seesionFactory, used via Spring injection
    *
    * @param userDAO
    */
  public void setUserDAO(final UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  /**
   * @see RegistrationManagerMBean#deleteUser(String)
   */
  @ManagedOperation(description = "Delete User")
  @ManagedOperationParameters({@ManagedOperationParameter(name = "userName", description = "User Name")})
  public String deleteUser(String userName) {

    try {
      User user = userDAO.findUserWithLoginName(userName);
      if (null == user)  return "User " + userName + "NOT found";
      userDAO.delete(user);
    }
    catch (Exception e) {
      logger.error(e.toString());
      e.printStackTrace();
      return "Cannot delete " + userName;
    }
    return "User " + userName + " deleted";
  }

  /**
   * @see RegistrationManagerMBean#changeUserPassword(String, String) )
   */

  @ManagedOperation(description = "Change User Password")
  @ManagedOperationParameters({@ManagedOperationParameter(name = "userName", description = "User Name")})
  public String changeUserPassword(String userName, String password) {

    try {
      User user = userDAO.findUserWithLoginName(userName);
      if (null == user)  return "User " + userName + "NOT found";
      user.setPassword(password);
      userDAO.saveOrUpdate(user);
    }
    catch (Exception e) {
      logger.error(e.toString());
      e.printStackTrace();
      return userName + "Error" + e.toString();
    }
    return "Password for user " + userName + " succesfuly changed";
  }


  /**
   * @see RegistrationManagerMBean#deactivateUser(String)
   */

  @ManagedOperation(description = "Deactivate User")
  @ManagedOperationParameters({@ManagedOperationParameter(name = "userName", description = "User Name")})
  public String deactivateUser(String userName) {
    return setActiveFlag(userName, false);
  }


  /**
   * @see RegistrationManagerMBean#activateUser(String)
   */

  @ManagedOperation(description = "Activate User")
  @ManagedOperationParameters({@ManagedOperationParameter(name = "userName", description = "User Name")})
  public String activateUser(String userName) {
    return setActiveFlag(userName, true);
  }

  //
  //  Private Methods
  //

  private String setActiveFlag(String userName, boolean flag) {
    try {
      User user = userDAO.findUserWithLoginName(userName);
      if (null == user)  return "User " + userName + "NOT found";
      user.setVerified(flag);
      user.setActive(flag);
      userDAO.saveOrUpdate(user);
    }
    catch (Exception e) {
      logger.error(e.toString());
      e.printStackTrace();
      return "Activeflag operation FAILED for User " + userName + " ERROR: " + e.toString();
    }
    return "User " + userName + " succesfuly activated/deactivated";

  }

}





