/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.kowari;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.kowari.resolver.spi.InitializerException;
import org.kowari.resolver.spi.Resolver;
import org.kowari.resolver.spi.ResolverFactory;
import org.kowari.resolver.spi.ResolverFactoryException;
import org.kowari.resolver.spi.ResolverFactoryInitializer;
import org.kowari.resolver.spi.ResolverSession;

/** 
 * The factory for {@link FilterResolver}s.
 * 
 * @author Ronald Tschalär
 */
public class FilterResolverFactory implements ResolverFactory {
  /** the resource under which the configuration is to be stored: {@value} */
  public static final String CONFIG_RSRC = "/conf/topaz-config.properties";

  private static final Logger logger = Logger.getLogger(FilterResolverFactory.class);

  private final URI           dbURI;
  private final long          sysModelType;
  private final FilterHandler handler;

  /** 
   * Create a new filter-resolver-factory instance. 
   * 
   * @param initializer the factory initializer
   * @return the new filter-resolver-factory instance
   */
  public static ResolverFactory newInstance(ResolverFactoryInitializer initializer)
    throws InitializerException
  {
    return new FilterResolverFactory(initializer);
  }

  /**
   * Instantiate a {@link FilterResolverFactory}.
   */
  private FilterResolverFactory(ResolverFactoryInitializer resolverFactoryInitializer)
      throws InitializerException {
    // Validate parameters
    if (resolverFactoryInitializer == null)
      throw new IllegalArgumentException("Null \"resolverFactoryInitializer\" parameter");

    // Claim the filter model type
    resolverFactoryInitializer.addModelType(FilterResolver.MODEL_TYPE, this);

    // remember the database uri
    dbURI = resolverFactoryInitializer.getDatabaseURI();

    // remember the system-model type
    sysModelType = resolverFactoryInitializer.getSystemModelType();

    // Set up the filter handler
    try {
      Properties config = new Properties();
      config.load(getClass().getResourceAsStream(CONFIG_RSRC));

      String handlerClsName = config.getProperty("topaz.fr.filterHandler.class");
      if (handlerClsName != null) {
        Class handlerClass = Class.forName(handlerClsName, true,
                                           Thread.currentThread().getContextClassLoader());
        Constructor c = handlerClass.getConstructor(new Class[] { Properties.class, URI.class });
        handler = (FilterHandler) c.newInstance(new Object[] { config, dbURI });

        logger.info("Handler '" + handlerClsName + "' configured");
      } else {
        logger.info("No handler configured");
        handler = null;
      }
    } catch (Exception e) {
      throw new InitializerException("Error creating handler instance", e);
    }

    // ensure we always close the handler so it can properly flush buffered data.
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          FilterResolverFactory.this.close();
        } catch (ResolverFactoryException rfe) {
          logger.error("Exception while closing handler", rfe);
        }
      }
    });
  }

  /**
   * Close the session factory.
   */
  public void close() throws ResolverFactoryException {
    if (handler != null)
      handler.close();
  }

  /**
   * Delete the session factory.
   */
  public void delete() throws ResolverFactoryException {
  }

  /**
   * Obtain a filter resolver.
   */
  public Resolver newResolver(boolean canWrite, ResolverSession resolverSession,
                              Resolver systemResolver)
                              throws ResolverFactoryException {
    return new FilterResolver(dbURI, sysModelType, systemResolver, resolverSession, handler);
  }
}

