/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.auth.handler;

import org.dom4j.Element;
import org.esupportail.cas.server.util.BasicHandler;
import org.esupportail.cas.server.util.MisconfiguredHandlerException;
import org.plos.service.password.PasswordDigestService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Plos user authentication handler that verifies that the user with a given userid/adminPassword exists
 * in the database
 */
public class PlosAuthDatabaseHandler extends BasicHandler {
  private PreparedStatement preparedStatement;
  private PasswordDigestService passwordService;

  /**
	 * Analyse the XML configuration to set netId and adminPassword attributes (constructor).
	 * 
	 * @param handlerElement the XML element that declares the handler in the configuration file
	 * @param configDebug debugging mode of the global configuration (set by default to the handler)
	 *
	 * @throws Exception when the handler not configured correctly
	 */
	public PlosAuthDatabaseHandler(final Element handlerElement, final Boolean configDebug)
          throws Exception
  {
		super(handlerElement, configDebug);
		traceBegin();
    trace("PlosAuthDatabaseHandler constructor called");

    // check that a config element is present
		checkConfigElement(true);

    init();
		traceEnd();
	}

  private void init() throws Exception {
    passwordService = getPasswordService();
    initDb();
  }

  private PasswordDigestService getPasswordService() throws Exception {
    final String encryption = getConfigSubElementContent("encryption");
    final String encodingCharset = getConfigSubElementContent("encoding_charset");
    PasswordDigestService passwordDigestService = new PasswordDigestService();
    passwordDigestService.setAlgorithm(encryption);
    passwordDigestService.setEncodingCharset(encodingCharset);
    return passwordDigestService;
  }

  private void initDb() throws Exception {
    // get the configuration parameters
    final String adminUser = getConfigSubElementContent("bind_username");
    final String adminPassword = getConfigSubElementContent("bind_password");
    final Element serverElement = getConfigSubElement("server");
    final String jdbcDriver = getElementContent(serverElement, "jdbc_driver");
    final String jdbcUrl = getElementContent(serverElement, "jdbc_url");
    try {
      Class.forName(jdbcDriver);
    } catch (final ClassNotFoundException ex) {
      throw new Exception("Unable to load the db driver:" + jdbcDriver, ex);
    }

    final Connection connection = DriverManager.getConnection(jdbcUrl, adminUser, adminPassword);
    preparedStatement = connection.prepareStatement(getSqlQuery());
  }

  private Element getConfigSubElement(final String elementName) {
    return getConfigElement().element(elementName);
  }

  private String getElementContent(final Element configElement, final String elementName)
                    throws Exception
  {
    final Element element = configElement.element(elementName);
    String str;
    if (null == element) {
      str = "";
    } else {
      str = element.getTextTrim();
    }
    if (str.length() == 0) {
      traceThrow(new MisconfiguredHandlerException(
          "A non empty nested '" + elementName + "' element is needed to configure the '"
          + getClass().getName() + "' handler."
          ));
    }
    return str;
  }

  /**
	 * Try to authenticate a user (compare with the db password).
	 *
	 * @param userLogin the user's adminUser
	 * @param userPassword the user's adminPassword
	 *
	 * @return BasicHandler.SUCCEDED on success,
	 * BasicHandler.FAILED_CONTINUE or BasicHandler.FAILED_STOP otherwise.
	 */
	public int authenticate(final String userLogin, final String userPassword) {
		traceBegin();

		trace("Checking user's password...");

    try {
      final String savedDigestPassword;

      synchronized(preparedStatement) {
        preparedStatement.setString(1, userLogin);
        final ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        savedDigestPassword = resultSet.getString(1);
        resultSet.close();
      }

      final boolean verified = passwordService.verifyPassword(userPassword, savedDigestPassword);

      if (verified) {
        trace("User's password matches.");
        return SUCCEEDED;
      }
      trace("User: " + userLogin + "'s password does not match");
      return FAILED_STOP;
    } catch (final Exception ex) {
      trace("User's password does not match " + ex.getMessage());
      return FAILED_STOP;
    } finally {
      traceEnd();
    }
	}

  /**
   * Read the SQL query from the configuration (deduces it from other parameters).
   * @return a String.
   * @throws Exception Exception
   */
  private String getSqlQuery() throws Exception {
    traceBegin();

    final String table = getConfigSubElementContent("table");
    final String loginColumn = getConfigSubElementContent("login_column");
    final String passwordColumn = getConfigSubElementContent("password_column");

    final String query = "SELECT " + passwordColumn
                        + " FROM " + table
                        + " WHERE " + loginColumn + " = ?";

    traceEnd(query);
    return query;
  }

  private String getConfigSubElementContent(final String elementName) throws Exception {
    final String value = getConfigSubElementContent(elementName, true/*needed*/);
    trace(elementName + " = " + value);
    return value;
  }

}
