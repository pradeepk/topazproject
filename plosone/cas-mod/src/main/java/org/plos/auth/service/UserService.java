/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.auth.service;

import org.plos.auth.db.DatabaseException;
import org.plos.auth.db.DatabaseContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Used to fetch the various properties, like guid, for a given user.
 * TODO: This could be managed by a ServletContextListener if required, so that 
 * both the UsernameReplacementWithGuidFilter and GetEmailAddress could use the same instance
 */
public class UserService {
  private final DatabaseContext context;
  private final String usernameToGuidSql;
  private final String guidToUsernameSql;

  public UserService(final DatabaseContext context, final String usernameToGuidSql, final String guidToUsernameSql) {
    this.usernameToGuidSql = usernameToGuidSql;
    this.guidToUsernameSql = guidToUsernameSql;
    this.context = context;
  }

  /**
   * Given a loginname it will return the guid for it from the database
   * @param loginname loginname
   * @return the guid for the loginname
   * @throws DatabaseException DatabaseException
   */
  public String getGuid(final String loginname) throws DatabaseException {
    try {
      return getDbValue(usernameToGuidSql, loginname);
    } catch (SQLException e) {
      throw new DatabaseException("Unable to get loginame from db", e);
    }
  }

  /**
   * Given a guid it will return the username for it from the database
   * @param guid guid
   * @return the guid for the guid
   * @throws DatabaseException DatabaseException
   */
  public String getEmailAddress(final String guid) throws DatabaseException {
    try {
      return getDbValue(guidToUsernameSql, guid);
    } catch (SQLException e) {
      throw new DatabaseException("Unable to get email address from db", e);
    }
  }

  private String getDbValue(final String sqlQuery, final String whereClauseParam) throws DatabaseException, SQLException {
    String returnValue = null;

    Connection connection = null;
    PreparedStatement preparedStatement = null;

    try {
      connection = context.getConnection();
      preparedStatement = context.getPreparedStatement(connection, sqlQuery);
      preparedStatement.setString(1, whereClauseParam);
      final ResultSet resultSet = preparedStatement.executeQuery();
      resultSet.next();
      returnValue = resultSet.getString(1);
      resultSet.close();
    } finally {
        if (preparedStatement != null) {
          preparedStatement.close();
        }
        if (connection != null) {
          connection.close();
        }
    }

    return returnValue;
  }
}
