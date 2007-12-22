/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.criterion.Conjunction;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.criterion.Disjunction;
import org.topazproject.otm.criterion.EQCriterion;
import org.topazproject.otm.criterion.ExistsCriterion;
import org.topazproject.otm.criterion.GECriterion;
import org.topazproject.otm.criterion.GTCriterion;
import org.topazproject.otm.criterion.LECriterion;
import org.topazproject.otm.criterion.LTCriterion;
import org.topazproject.otm.criterion.MinusCriterion;
import org.topazproject.otm.criterion.NECriterion;
import org.topazproject.otm.criterion.NotCriterion;
import org.topazproject.otm.criterion.NotExistsCriterion;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Parameter;
import org.topazproject.otm.criterion.TransCriterion;
import org.topazproject.otm.criterion.WalkCriterion;

import org.topazproject.otm.impl.SessionFactoryImpl;

import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.ClassWithEnum;
import org.topazproject.otm.samples.Grants;
import org.topazproject.otm.samples.NoPredicate;
import org.topazproject.otm.samples.NoRdfType;
import org.topazproject.otm.samples.PrivateAnnotation;
import org.topazproject.otm.samples.PublicAnnotation;
import org.topazproject.otm.samples.ReplyThread;
import org.topazproject.otm.samples.Revokes;
import org.topazproject.otm.samples.SpecialMappers;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.stores.SimpleBlobStore;

/**
 * Base class for all integration tests done using the samples classes.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractOtmTest {
  private static final Log log = LogFactory.getLog(AbstractOtmTest.class);
  /**
   * Shared session factory
   */
  protected SessionFactory factory = new SessionFactoryImpl();

  protected void initFactory() throws OtmException {
    log.info("initializing otm session factory ...");
    ModelConfig[] models = new ModelConfig[] {
      new ModelConfig("ri", URI.create("local:///topazproject#otmtest1"), null),
      new ModelConfig("grants", URI.create("local:///topazproject#otmtest2"), null),
      new ModelConfig("revokes", URI.create("local:///topazproject#otmtest3"), null),
      new ModelConfig("criteria", URI.create("local:///topazproject#otmtest4"), null),
      new ModelConfig("str", URI.create("local:///topazproject#str"),
                                      URI.create("http://topazproject.org/models#StringCompare")),
    };
    URI storeUri = URI.create("http://localhost:9091/mulgara-service/services/ItqlBeanService");
    TripleStore tripleStore = new ItqlStore(storeUri);
    factory.setTripleStore(tripleStore);
    factory.setBlobStore(new SimpleBlobStore("blob-store"));

    for (ModelConfig model : models)
      factory.addModel(model);

    tripleStore.createModel(factory.getModel("str"));

    Class classes [] = new Class[] {Article.class, PublicAnnotation.class, PrivateAnnotation.class,
                                    ReplyThread.class, Grants.class, Revokes.class,
                                    NoRdfType.class, NoPredicate.class, SpecialMappers.class,
                                    ClassWithEnum.class,
                                    // criteria classes
                                    Conjunction.class, Criterion.class, DetachedCriteria.class,
                                    Disjunction.class, EQCriterion.class, ExistsCriterion.class,
                                    GECriterion.class, GTCriterion.class, LECriterion.class,
                                    LTCriterion.class, MinusCriterion.class, NECriterion.class,
                                    NotCriterion.class, NotExistsCriterion.class, Order.class,
                                    Parameter.class, TransCriterion.class, WalkCriterion.class,
    };

    for (Class c : classes)
      factory.preload(c);
  }

  protected void initModels() throws OtmException {
    log.info("initializing mulgara test models ...");
    String names[] = new String[] {"ri", "grants", "revokes", "criteria"};

    for (String name : names) {
      ModelConfig model = factory.getModel(name);
      try {
        factory.getTripleStore().dropModel(model);
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Failed to drop model '" + name + "'", t);
      }
      factory.getTripleStore().createModel(model);
    }
  }

 /**
   * Run the given action within a session.
   *
   * @param s      the otm session to use
   * @param action the action to run
   * @return the value returned by the action
   */
  protected void doInSession(Action action) throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();
      action.run(session);
      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }
  }

  /**
   * The interface actions must implement.
   */
  protected static interface Action {
    /**
     * This is run within the context of a session.
     *
     * @param session the current transaction
     */
    void run(Session session) throws OtmException;
  }

}
