/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.owl;

import java.net.URI;
import java.util.Arrays;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.annotations.Rdf;

/**
 * Utility to add otm classes to #metadata in owl form.
 *
 * @author Eric Brown
 */
public class OwlHelper {
  private static final Log log = LogFactory.getLog(OwlHelper.class);

  public static final String METADATA_MODEL  = "metadata";
  public static final URI METADATA_MODEL_URI = URI.create("local:///topazproject#" + METADATA_MODEL);
  public static final URI HAS_ALIAS_PRED     = URI.create(Rdf.topaz + "hasAlias");

  /**
   * Traverse MetaClassdata stored in the supplied SessionFactory and store that in
   * #metadata in owl form.
   *
   * @param factory where MetaClassdata exists
   */
  public static void addFactory(SessionFactory factory) throws OtmException {
    SessionFactory metaFactory = createMetaSessionFactory(factory);

    Session session = metaFactory.openSession();
    Transaction tx = null;

    try {
      tx = session.beginTransaction();

      createRdfAliases(session);

      // Loop over all classes in factory
      for (ClassMetadata cm: factory.listClassMetadata()) {
        String type = cm.getType();
        OwlClass oc = null;

        // Ignore anonymous classes
        if (type != null) {
          // Create OwlClass
          oc = new OwlClass();
          oc.setOwlClass(URI.create(type));

          // Add sub-classes
          for (String t: cm.getTypes())
            if (t != type)
              oc.addSuperClass(URI.create(t));

          // Set model
          if (cm.getModel() != null) {
            /* TODO: Get following to work (but models need to be added to factory)
             *   Problem is that OwlHelper doesn't know what models to add --
             *   maybe make the prefix a parameter
             * ModelConfig mc = metaFactory.getModel(cm.getModel());
             * oc.setModel(mc.getUri());
             */
            oc.setModel(URI.create("local:///topazproject#" + cm.getModel()));
          }

          session.saveOrUpdate(oc);
        }

        // Now let's iterate over the fields
        for (Mapper m: cm.getFields()) {
          // See if this field really belongs to our class/type
          Field f = m.getField();
          if (f.getDeclaringClass() != cm.getSourceClass()) {
            continue;
          }

          // See if we've already created this property
          ObjectProperty op = session.get(ObjectProperty.class, m.getUri());
          if (op == null)
            op = new ObjectProperty();

          // Set the property name
          op.setProperty(URI.create(m.getUri()));

          // Add to
          if (oc != null) {
//            op.setDomains(new URI[] { URI.create(type) });

            DomainUnion union = op.getDomains();
            if (union == null)
              union = new DomainUnion();
            union.id = URI.create(m.getUri().toString() + "Union"); // fake id for now
            union.domains.add(oc);

            if (m.getDataType() != null)
              op.setRanges(new URI[] { URI.create(m.getDataType()) });
            else {
              ClassMetadata cm2 = factory.getClassMetadata(m.getComponentType());
              if (cm2 != null)
                op.setRanges(new URI[] { URI.create(cm2.getType()) });
            }

            op.setDomains(union);
          }

          session.saveOrUpdate(op);
        }
      }

      tx.commit();
      tx = null;
    } finally {
      if (tx != null) {
        try {
          tx.rollback();
        } catch (OtmException oe) {
          log.warn("Rollback failed", oe);
        }
      }

      try {
        session.close();
      } catch (OtmException oe) {
        log.warn("Closing session failed", oe);
      }
    }
  }

  /**
   * Create topaz:hasAlias entries into #metadata.
   *
   * TODO: Update this when this info is available in the SessionFactory or ClassMetadata
   */
  private static void createRdfAliases(Session session) {
    for (Field f: Rdf.class.getFields()) {
      if (Modifier.isStatic(f.getModifiers())) {
        try {
          Alias a = session.get(Alias.class, f.get(Rdf.class).toString());
          if (a == null)
            a = new Alias();

          a.setPrefix(URI.create(f.get(Rdf.class).toString()));
          a.addAlias(f.getName());
          session.saveOrUpdate(a);
        } catch (IllegalAccessException iae) {
          log.debug("Unable to create alias for field 'Rdf." + f.getName() + "'", iae);
        }
      }
    }
  }

  /**
   * Setup our model and session. DON'T forget to close the session!
   */
  private static SessionFactory createMetaSessionFactory(SessionFactory factory)
      throws OtmException {
    SessionFactory metaFactory = new SessionFactory();
    metaFactory.setTripleStore(factory.getTripleStore());

    ModelConfig mc = new ModelConfig(METADATA_MODEL, METADATA_MODEL_URI, null);
    metaFactory.addModel(mc);
    metaFactory.getTripleStore().createModel(mc);

    metaFactory.preload(OwlClass.class);
    metaFactory.preload(ObjectProperty.class);
    metaFactory.preload(Alias.class);
    metaFactory.preload(DomainUnion.class);

    return metaFactory;
  }
}
