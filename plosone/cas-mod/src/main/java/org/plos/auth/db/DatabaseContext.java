/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/cas-mod/src/main/java/org#$
 * $Id: DatabaseContext.java 649 2006-09-20 21:49:15Z viru $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.auth.db;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Provides a database connection pool including prepared statement connection pooling.
 * Initializes DBCP environment and provides access for creating JDBC connections
 * and prepared Statements.
 *
 * Massive code lifting from http://www.devx.com/Java/Article/29795/0/page/2
 */
public class DatabaseContext {
  private PoolingDataSource dataSource;
  private GenericObjectPool connectionPool;
  private String jdbcDriver;
  private int initialSize;
  private int maxActive;
  private Properties dbProperties;
  private String validationQuery;

  /**
   * Construct a db context
   * @param jdbcDriver jdbcDriver
   * @param dbProperties dbProperties including url, user, password
   * @param initialSize initialSize of the pool
   * @param maxActive maxActive number of connections, after which it will block until a connection is released
   * @param validationQuery to validate that the connection is still valid
   * @throws DatabaseException DatabaseException
   */
  public DatabaseContext(final String jdbcDriver, final Properties dbProperties, final int initialSize, final int maxActive, final String validationQuery) throws DatabaseException {
    this.dbProperties = dbProperties;
    this.jdbcDriver = jdbcDriver;
    this.maxActive = maxActive;
    this.initialSize = initialSize;
    this.validationQuery = validationQuery;
    init();
  }

  private void init() throws DatabaseException {
    try {
      Class.forName(jdbcDriver);
    } catch (final ClassNotFoundException e) {
      throw new DatabaseException("Unable to load the db driver:" + jdbcDriver, e);
    }

    final ConnectionFactory connectionFactory =
            new DriverManagerConnectionFactory(
                    dbProperties.getProperty("url"),
                    dbProperties.getProperty("user"),
                    dbProperties.getProperty("password"));

    connectionPool = new GenericObjectPool();
    connectionPool.setTestOnReturn(true);
    connectionPool.setMaxActive(maxActive);
    connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
    connectionPool.getMaxActive();
    
    final KeyedObjectPoolFactory stmtPool = new GenericKeyedObjectPoolFactory(null);

    /* During instantiation, the PoolableConnectionFactory class registers itself to the
    GenericObjectPool instance passed in its constructor. This factory class is used to create new
    instances of the JDBC connections.
     */
    new PoolableConnectionFactory(connectionFactory, connectionPool, stmtPool, validationQuery, false, true);

    for (int i = 0; i < initialSize; i++) {
      try {
        connectionPool.addObject();
      } catch (final Exception e) {
        throw new DatabaseException("Error initlaizing initial number of connections", e);
      }
    }
    dataSource = new PoolingDataSource(connectionPool);
  }

  public Connection getConnection() throws DatabaseException {
    try {
      return dataSource.getConnection();
    } catch (final SQLException e) {
      throw new DatabaseException("Unable to get a connection from context", e);
    }
  }

  /**
   * Return a prepared statement from the pool
   * @param connection connection
   * @param query query
   * @return a prepared statement from the pool
   * @throws DatabaseException DatabaseException
   */
  public PreparedStatement getPreparedStatement(final Connection connection, final String query) throws DatabaseException {
    try {
      return connection.prepareStatement(query);
    } catch (final SQLException e) {
      throw new DatabaseException("Unable to prepare statement", e);
    }
  }

  /**
   * Returns the status of the connection pool.
   * @return the status of the connection pool
   */
  public String getStatus() {
    return "[Active:" + connectionPool.getNumActive() + ", Idle:" + connectionPool.getNumIdle() + "]";
  }

  public void close() throws DatabaseException {
    if (connectionPool != null) {
      connectionPool.clear();
      try {
        connectionPool.close();
      } catch (final Exception e) {
        throw new DatabaseException("Unable to shutdown Database context");
      }
    }
  }

}
