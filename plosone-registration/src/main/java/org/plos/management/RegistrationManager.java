package org.plos.management;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.plos.registration.User;
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

  private SessionFactory sessionFactory;
  private Session session;

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /**
   * Deletes user from registry
   *
   * @param userName The user name
   * @return  Success or failure message
   *
   */
  @ManagedOperation(description = "Delete User")
  @ManagedOperationParameters({@ManagedOperationParameter(name = "userName", description = "User Name")})
//  @Transactional(propagation = Propagation.REQUIRED)
  public String deleteUser(String userName) {
    org.hibernate.Query userQuery;

    try {
      userQuery = getUserNameQuery(userName);
      //user.setPassword(password);
      java.util.List results = userQuery.list();
      if (results.isEmpty()) return "User" + userName + " Not Found";
      User user = (User) results.get(0);
      //Transaction tx = session.beginTransaction();
      //session.delete(user);
      //tx.commit();

    }
    catch (Exception e) {
      System.out.println(e.toString());
      return "Cannot delete " + userName;
    }
    return "User " + userName + " deleted";
  }

  /**
   * Changes users password
   *
   * @param userName The user name
   * @return  Success or failure message
   *
   */

  @ManagedOperation(description = "Change User Password")
  @ManagedOperationParameters({@ManagedOperationParameter(name = "userName", description = "User Name")})
//  @Transactional(propagation = Propagation.REQUIRED)
  public String changeUserPassword(String userName, String password) {
    org.hibernate.Query userQuery;

    try {
      userQuery = getUserNameQuery(userName);
      java.util.List results = userQuery.list();
      User user = (User) results.get(0);
      user.setPassword(password);
      saveUser(user);
    }
    catch (Exception e) {
      System.out.println(e.toString());
      return userName + "Error" + e.toString();
    }
    return "Password for user " + userName + " succesfuly changed";
  }


  /**
   * Deactivate user
   *
   * @param userName The user name
   * @return  Success or failure message
   *
   */

  @ManagedOperation(description = "Deactivate User")
  @ManagedOperationParameters({@ManagedOperationParameter(name = "userName", description = "User Name")})
//  @Transactional(propagation = Propagation.REQUIRED)
  public String deactivateUser(String userName) {
    return setActiveFlag(userName, false);
  }


  /**
   * Activate user
   *
   * @param userName The user name
   * @return  Success or failure message
   *
   */

  @ManagedOperation(description = "Activate User")
  @ManagedOperationParameters({@ManagedOperationParameter(name = "userName", description = "User Name")})
//  @Transactional(propagation = Propagation.REQUIRED)
  public String activateUser(String userName) {
    return setActiveFlag(userName, true);
  }

  //
  //  Private Methods
  //

  private org.hibernate.Query getUserNameQuery(String userName) {
    if (sessionFactory == null) {
      System.out.println("sessionFactory is NULL");
      //session = HibernateUtil.getSession();
    }
    session = sessionFactory.getCurrentSession();

    if (session == null) System.out.println("session NULL!!");
    org.hibernate.Query q = session.getNamedQuery("userName");
    q.setString("userName", userName);
    return q;
  }


  private String setActiveFlag(String userName, boolean flag) {
    org.hibernate.Query userQuery;
    try {
      userQuery = getUserNameQuery(userName);
      java.util.List results = userQuery.list();
      if (results.isEmpty()) return "User" + userName + " Not Found";
      User user = (User) results.get(0);
      user.setActive(flag);
      saveUser(user);
    }
    catch (Exception e) {
      System.out.println(e.toString());
      return "ERROR: " + e.toString();
    }
    return "User " + userName + " succesfuly activated/deactivated";

  }

  private void saveUser(User user) {
    Transaction tx = session.beginTransaction();
    session.save(user);
    tx.commit();
  }


}





