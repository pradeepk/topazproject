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

import java.net.URI;
import java.util.Properties;

import org.kowari.resolver.spi.InitializerException;
import org.kowari.resolver.spi.Resolver;
import org.kowari.resolver.spi.ResolverFactory;
import org.kowari.resolver.spi.ResolverFactoryException;
import org.kowari.resolver.spi.ResolverFactoryInitializer;
import org.kowari.resolver.spi.ResolverSession;
import org.kowari.server.NonRemoteSessionException;
import org.kowari.server.driver.SessionFactoryFinder;
import org.kowari.server.driver.SessionFactoryFinderException;
import org.kowari.server.local.LocalSessionFactory;

/** 
 * The factory for {@link FilterResolver}s.
 * 
 * @author Ronald Tschalär
 */
public class FilterResolverFactory implements ResolverFactory {
  /** the resource under which the configuration is to be stored: {@value} */
  public static final String CONFIG_RSRC = "/conf/topaz-config.properties";

  private final URI           dbURI;
  private final long          sysModelType;
  private final FedoraUpdater fedoraUpdater;

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

    // set up the session factory
    dbURI = resolverFactoryInitializer.getDatabaseURI();

    LocalSessionFactory sessFactory;
    try {
      sessFactory = (LocalSessionFactory) SessionFactoryFinder.newSessionFactory(dbURI, false);
      //sessFactory.setDirectory(resolverFactoryInitializer.getDirectory());
    } catch (SessionFactoryFinderException sffe) {
      throw new InitializerException("Error creating session factory", sffe);
    } catch (NonRemoteSessionException nrse) {
      throw new InitializerException("Error creating session factory", nrse);
    }

    // remember the system-model type
    sysModelType = resolverFactoryInitializer.getSystemModelType();

    // Set up the fedora updater.
    try {
      Properties config = new Properties();
      config.load(getClass().getResourceAsStream(CONFIG_RSRC));

      Class filterClass = Class.forName(config.getProperty("topaz.fr.updateFilter.class"), true,
                                        Thread.currentThread().getContextClassLoader());
      UpdateFilter filter = (UpdateFilter) filterClass.newInstance();

      fedoraUpdater =
        new FedoraUpdater(new URI(config.getProperty("topaz.fr.fedora.url")),
                          config.getProperty("topaz.fr.fedora.user"),
                          config.getProperty("topaz.fr.fedora.pass"),
                          new URI(config.getProperty("topaz.fr.kowari.dbUrl")),
                          sessFactory,
                          Long.parseLong(config.getProperty("topaz.fr.updateInterval")),
                          filter);
    } catch (Exception e) {
      throw new InitializerException("Error creating fedora-updater", e);
    }
  }

  /**
   * Close the session factory.
   */
  public void close() throws ResolverFactoryException {
    fedoraUpdater.close();
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
    return new FilterResolver(dbURI, sysModelType, systemResolver, resolverSession, fedoraUpdater);
  }
}

