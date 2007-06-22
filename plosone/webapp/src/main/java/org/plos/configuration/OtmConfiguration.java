/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.configuration;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.context.ThreadLocalSessionContext;
import org.topazproject.otm.stores.ItqlStore;

/**
 * Convenience class to manage configuration of OTM Session Factory
 *
 * @author Stephen Cheng
 */
public class OtmConfiguration {
  private SessionFactory          factory;
  private String[]                preloadClasses;
  private ModelConfig[]           models;
  private static final Log        log      = LogFactory.getLog(OtmConfiguration.class);
  private static OtmConfiguration instance;

  /**
   * Creates the singleton OtmConfiguration object.
   *
   * @param tripleStoreUrl the URL for the store
   *
   * @return the newly created instamce
   *
   * @throws OtmException in case of an error
   */
  public static OtmConfiguration createInstance(String tripleStoreUrl)
                                         throws OtmException {
    if (instance != null)
      throw new OtmException("An instance already created");

    instance                               = new OtmConfiguration(tripleStoreUrl);

    return instance;
  }

  /**
   * Gets the singleton OtmConfiguration object.
   *
   * @return the singleton instamce
   */
  public static OtmConfiguration getInstance() {
    return instance;
  }

  private OtmConfiguration(String tripleStoreUrl) throws OtmException {
    factory = new SessionFactory();

    if (log.isDebugEnabled()) {
      log.debug("Creating new triplestore: " + tripleStoreUrl);
    }

    factory.setCurrentSessionContext(new ThreadLocalSessionContext(factory));

    factory.setTripleStore(new ItqlStore(URI.create(tripleStoreUrl)));
  }

  /**
   * Gets the set of classes that we configured the factory with.
   *
   * @return Returns the preloadClasses.
   */
  public String[] getPreloadClasses() {
    return preloadClasses;
  }

  /**
   * Preloads the session factory with the classes that we use.
   *
   * @param preloadClasses The preloadClasses to set.
   */
  public void setPreloadClasses(String[] preloadClasses) {
    this.preloadClasses = preloadClasses;

    for (String className : preloadClasses) {
      try {
        factory.preload(Class.forName(className));
      } catch (ClassNotFoundException ce) {
        log.info("Could not preload class: " + className, ce);
      }
    }
  }

  /**
   * Gets the session factory.
   *
   * @return Returns the factory.
   */
  public SessionFactory getFactory() {
    return factory;
  }

  /**
   * Get the list of configured models.
   *
   * @return Returns the models.
   */
  public ModelConfig[] getModels() {
    return models;
  }

  /**
   * Configures the factory with models that we use and makes sure they exist.
   *
   * @param models The models to set.
   */
  public void setModels(ModelConfig[] models) {
    this.models = models;

    for (ModelConfig model : models) {
      factory.addModel(model);
      factory.getTripleStore().createModel(model);
    }
  }
}
