/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.auth.web;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Used to fetch the various properties, like guid, for a given user.
 * TODO: This could be managed by a ServletContextListener if required, so that 
 * both the UsernameReplacementWithGuidFilter and GetEmailAddress could use the same instance
 */
public class UserService {
  private PreparedStatement usernameToGuidPreparedStatement;
  private PreparedStatement guidToUsernamePreparedStatement;

  /**
   * Initialize the UserService with the required params
   * @param jdbcDriver jdbcDriver
   * @param jdbcUrl jdbcUrl
   * @param usernameToGuidSql usernameToGuidSql
   * @param guidToUsernameSql guidToUsernameSql
   * @param adminUser adminUser
   * @param adminPassword adminPassword
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public UserService(final String jdbcDriver, final String jdbcUrl, final String usernameToGuidSql, final String guidToUsernameSql, final String adminUser, final String adminPassword) throws ClassNotFoundException, SQLException {
    registerDriver(jdbcDriver);
    final Properties connectionProperties = getConnectionProperties(adminUser, adminPassword);
    final Connection dbConnection = getDBConnection(jdbcUrl, connectionProperties);
    usernameToGuidPreparedStatement = dbConnection.prepareStatement(usernameToGuidSql);
    guidToUsernamePreparedStatement = dbConnection.prepareStatement(guidToUsernameSql);
  }

  /**
   * Provide the constructor the ServletContext and let it initialize itself off of it.
   * @param config ServletContext
   * @throws SQLException
   * @throws ClassNotFoundException
   */
  public UserService(final ServletContext config) throws SQLException, ClassNotFoundException {
    this(
        config.getInitParameter("jdbcDriver"),
        config.getInitParameter("jdbcUrl"),
        config.getInitParameter("usernameToGuidSql"),
        config.getInitParameter("guidToUsernameSql"),
        config.getInitParameter("adminUser"),
        config.getInitParameter("adminPassword")
    );
  }

  /**
   * Given a loginname it will return the guid for it from the database
   * @param loginname loginname
   * @return the guid for the loginname
   * @throws SQLException
   */
  public String getGuid(final String loginname) throws SQLException {
    return getDbValue(usernameToGuidPreparedStatement, loginname);
  }

  /**
   * Given a guid it will return the username for it from the database
   * @param guid guid
   * @return the guid for the guid
   * @throws SQLException
   */
  public String getEmailAddress(final String guid) throws SQLException {
    return getDbValue(guidToUsernamePreparedStatement, guid);
  }

  private String getDbValue(final PreparedStatement preparedStatement, final String whereClauseParam) throws SQLException {
    final String returnValue;

    synchronized (preparedStatement) {
      preparedStatement.setString(1, whereClauseParam);
      final ResultSet resultSet = preparedStatement.executeQuery();
      resultSet.next();
      returnValue = resultSet.getString(1);
      resultSet.close();
    }
    
    return returnValue;
  }

  private Connection getDBConnection(final String jdbcUrl, final Properties connectionProperties) throws ClassNotFoundException, SQLException {
    return DriverManager.getConnection(jdbcUrl, connectionProperties);
  }

  private Properties getConnectionProperties(final String adminUser, final String adminPassword) {
    final Properties props = new Properties();
    props.setProperty("user", adminUser);
    props.setProperty("password", adminPassword);
    return props;
  }

  private void registerDriver(final String jdbcDriver) throws ClassNotFoundException {
    Class.forName(jdbcDriver);
  }
}
