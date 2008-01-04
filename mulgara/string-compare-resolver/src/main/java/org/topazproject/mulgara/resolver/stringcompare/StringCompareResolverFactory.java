/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.resolver.stringcompare;

import java.net.URI;

import org.apache.log4j.Logger;

import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.InitializerException;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverFactoryInitializer;
import org.mulgara.resolver.spi.ResolverSession;

/**
 * The factory for {@link StringCompareResolver StringCompareResolver}s.
 *
 * @author Ronald Tschalär
 */
public class StringCompareResolverFactory implements ResolverFactory {
  /** the model type we handle */
  public static final URI MODEL_TYPE = URI.create("http://topazproject.org/models#StringCompare");

  private StringCompareImpl impls[] = new StringCompareImpl[] {
    new EqualsIgnoreCaseImpl(),
    new LtImpl(),
    new LeImpl(),
    new GtImpl(),
    new GeImpl()
  };

  /**
   * Create a new string-compare-resolver-factory instance.
   *
   * @param initializer the factory initializer
   * @return the new string-compare-resolver-factory instance
   */
  public static ResolverFactory newInstance(ResolverFactoryInitializer initializer)
      throws InitializerException {
    return new StringCompareResolverFactory(initializer);
  }

  /**
   * Instantiate a {@link StringCompareResolverFactory StringCompareResolverFactory}.
   */
  private StringCompareResolverFactory(ResolverFactoryInitializer initializer)
      throws InitializerException {
    // Validate "resolverFactoryInitializer" parameter
    if (initializer == null)
      throw new IllegalArgumentException("Null 'resolverFactoryInitializer' parameter");

    // Claim the filter model type
    initializer.addModelType(MODEL_TYPE, this);

    // set up our data
    for (StringCompareImpl impl: impls) {
      URI uri = URI.create("http://rdf.topazproject.org/RDF/" + impl.getOp());
      impl.setNode(initializer.preallocate(new URIReferenceImpl(uri)));
    }
  }

  public void close() {
  }

  public void delete() {
  }

  /**
   * Obtain a String Compare resolver.
   */
  public Resolver newResolver(boolean canWrite, ResolverSession resolverSession,
                              Resolver systemResolver)
      throws ResolverFactoryException {
    return new StringCompareResolver(resolverSession, impls, MODEL_TYPE);
  }
}
