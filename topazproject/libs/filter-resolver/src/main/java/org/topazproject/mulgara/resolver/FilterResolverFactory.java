/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.mulgara.resolver;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.mulgara.resolver.spi.InitializerException;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverFactoryInitializer;
import org.mulgara.resolver.spi.ResolverSession;

/** 
 * The factory for {@link FilterResolver}s.
 * 
 * @author Ronald Tschalär
 */
public class FilterResolverFactory implements ResolverFactory {
  /** the resource under which the configuration is to be stored: {@value} */
  public static final String CONFIG_RSRC = "/conf/topaz-config.properties";

  private static final Logger logger = Logger.getLogger(FilterResolverFactory.class);

  private final URI             dbURI;
  private final long            sysModelType;
  private final FilterHandler[] handlers;

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

    // Set up the filter handlers
    Properties config = new Properties();
    try {
      config.load(getClass().getResourceAsStream(CONFIG_RSRC));
    } catch (IOException ioe) {
      throw new InitializerException("Error reading '" + CONFIG_RSRC + "'", ioe);
    }

    List hList = new ArrayList();
    for (int idx = 0; ; idx++) {
      String handlerClsName = config.getProperty("topaz.fr.filterHandler.class." + idx);
      if (handlerClsName == null)
        break;

      hList.add(instantiateHandler(handlerClsName, config, dbURI));
      logger.info("Loaded handler '" + handlerClsName + "'");
    }

    if (hList.size() == 0)
      logger.info("No handlers configured");

    handlers = (FilterHandler[]) hList.toArray(new FilterHandler[hList.size()]);

    // ensure we always close the handler so it can properly flush buffered data.
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          FilterResolverFactory.this.close();
        } catch (ResolverFactoryException rfe) {
          logger.error("Exception while closing handlers", rfe);
        }
      }
    });
  }

  private static FilterHandler instantiateHandler(String clsName, Properties config, URI dbURI)
      throws InitializerException {
    try {
      Class clazz = Class.forName(clsName, true, Thread.currentThread().getContextClassLoader());
      Constructor c = clazz.getConstructor(new Class[] { Properties.class, URI.class });
      return (FilterHandler) c.newInstance(new Object[] { config, dbURI });
    } catch (Exception e) {
      throw new InitializerException("Error creating handler instance for '" + clsName + "'", e);
    }
  }


  /**
   * Close the session factory.
   */
  public void close() throws ResolverFactoryException {
    for (int idx = 0; idx < handlers.length; idx++)
      handlers[idx].close();
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
    return new FilterResolver(dbURI, sysModelType, systemResolver, resolverSession, handlers);
  }
}

