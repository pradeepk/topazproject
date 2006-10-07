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
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.transaction.xa.XAResource;

import org.apache.log4j.Logger;

import org.jrdf.graph.AbstractTriple;
import org.jrdf.graph.Node;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;

import org.kowari.query.Constraint;
import org.kowari.query.ConstraintElement;
import org.kowari.query.ConstraintImpl;
import org.kowari.query.ConstraintIs;
import org.kowari.query.ConstraintNegation;
import org.kowari.query.ConstraintNotOccurs;
import org.kowari.query.ConstraintOccurs;
import org.kowari.query.ConstraintOccursLessThan;
import org.kowari.query.ConstraintOccursMoreThan;
import org.kowari.query.QueryException;
import org.kowari.query.SingleTransitiveConstraint;
import org.kowari.query.TransitiveConstraint;
import org.kowari.query.TuplesException;
import org.kowari.query.WalkConstraint;
import org.kowari.query.rdf.URIReferenceImpl;
import org.kowari.resolver.spi.EmptyResolution;
import org.kowari.resolver.spi.GlobalizeException;
import org.kowari.resolver.spi.LocalizeException;
import org.kowari.resolver.spi.Resolution;
import org.kowari.resolver.spi.Resolver;
import org.kowari.resolver.spi.ResolverException;
import org.kowari.resolver.spi.ResolverSession;
import org.kowari.resolver.spi.Statements;
import org.kowari.resolver.view.SessionView;
import org.kowari.resolver.view.ViewMarker;
import org.kowari.server.Session;
import org.kowari.store.LocalNode;
import org.kowari.store.tuples.Tuples;

/** 
 * The factory for {@link FilterResolver}s. The model URI used for this filter
 * is:
 * <pre>
 *   &lt;dbURI&gt;#filter:model=&lt;modelName&gt;;ds=&lt;datastream&gt;
 * </pre>
 * For example:
 * <pre>
 *   rmi://localhost:/fedora#filter:model=ri;ds=RELS-EXT
 * </pre>
 * 
 * @author Ronald Tschalär
 */
public class FilterResolver implements Resolver, ViewMarker {
  /** the model type we handle */
  public static final URI MODEL_TYPE = URI.create("http://topazproject.org/models#filter");

  private static final Logger logger = Logger.getLogger(FilterResolver.class);
  private static final Map    modelTranslationCache = new HashMap();

  private final URI             dbURI;
  private final long            sysModelType;
  private final ResolverSession resolverSession;
  private final Resolver        systemResolver;
  private final FilterHandler   handler;

  private Session sess;

  /** 
   * Create a new FilterResolver instance. 
   * 
   * @param dbURI           the absolute URI of the database; used as the base for creating the
   *                        absolute URI of a model
   * @param sysModelType    the system-model type; used when creating a new model
   * @param systemResolver  the system-resolver; used for creating and modifying models
   * @param resolverSession our environment; used for globalizing and localizing nodes
   * @param handler         the filter handler to use
   */
  FilterResolver(URI dbURI, long sysModelType, Resolver systemResolver,
                 ResolverSession resolverSession, FilterHandler handler) {
    this.dbURI           = dbURI;
    this.sysModelType    = sysModelType;
    this.systemResolver  = systemResolver;
    this.resolverSession = resolverSession;
    this.handler         = handler;
  }

  public void setSession(SessionView session) {
    this.sess = (Session) session;
  }

  /**
   * @return the updater's XAResource
   */
  public XAResource getXAResource() {
    XAResource res = (handler != null) ? handler.getXAResource() : null;
    return (res != null) ? res : new AbstractFilterHandler.DummyXAResource();
  }

  public void createModel(long model, URI modelType) throws ResolverException, LocalizeException {
    // check model type
    if (!modelType.equals(MODEL_TYPE))
      throw new ResolverException("Unknown model-type '" + modelType + "'");

    // get system model type URI
    try {
      Node mtURI = resolverSession.globalize(sysModelType);
      if (!(mtURI instanceof URIReference))
        throw new ResolverException("systemModelType '" + mtURI + "' not a URIRef ");

      modelType = ((URIReference) mtURI).getURI();
    } catch (GlobalizeException ge) {
      throw new ResolverException("Failed to globalize SystemModel Type", ge);
    }

    // convert filter model uri to real model uri
    URI filterModelURI = toURI(model);
    URI realModelURI   = toRealModelURI(filterModelURI);
    if (logger.isDebugEnabled())
      logger.debug("Creating model '" + realModelURI + "'");

    // create the real model if it doesn't exist
    try {
      if (!sess.modelExists(realModelURI)) {
        //s.createModel(realModelURI, modelType);
        model = resolverSession.localizePersistent(new URIReferenceImpl(realModelURI, false));
        systemResolver.createModel(model, modelType);
      }
    } catch (LocalizeException le) {
      throw new ResolverException("Error localizing model uri '" + realModelURI + "'", le);
    } catch (QueryException qe) {
      throw new ResolverException("Error creating model " + realModelURI, qe);
    }

    if (handler != null)
      handler.modelCreated(filterModelURI, realModelURI);
  }

  public void modifyModel(long model, Statements statements, boolean occurs)
      throws ResolverException {
    URI filterModelURI = toURI(model);
    URI realModelURI   = toRealModelURI(filterModelURI);
    if (logger.isDebugEnabled())
      logger.debug("Modifying model '" + realModelURI + "'");

    try {
      systemResolver.modifyModel(lookupRealNode(model), statements, occurs);
    } catch (QueryException qe) {
      throw new ResolverException("Failed to look up model", qe);
    }

    if (handler != null)
      handler.modelModified(filterModelURI, realModelURI, statements, occurs, resolverSession);
  }

  public void removeModel(long model) throws ResolverException {
    URI filterModelURI = toURI(model);
    URI realModelURI   = toRealModelURI(filterModelURI);

    if (logger.isDebugEnabled())
      logger.debug("Removing model '" + filterModelURI + "'");

    if (handler != null)
      handler.modelRemoved(filterModelURI, realModelURI);
  }

  public Resolution resolve(Constraint constraint) throws QueryException {
    constraint = translateModel(constraint);

    Tuples ans = ((SessionView) sess).resolve(constraint);
    if (ans instanceof Resolution)
      return (Resolution) ans;

    //FIXME?
    //return new TuplesWrapperResolution(ans);
    logger.error("Unimplemented answer type '" + ans.getClass().getName() + "'");
    return new EmptyResolution(constraint, false);
  }

  /**
   * Translate the model (element 4) of the constraint to the underlying model.
   *
   * According to tests, the only classes we ever see here are ConstraintImpl and
   * ConstraintNegation. However, to be safe, we handle all known implementations of
   * Constraint here.
   */
  private Constraint translateModel(Constraint constraint) throws QueryException {
    if (logger.isDebugEnabled())
      logger.debug("translating constraint class '" + constraint.getClass().getName() + "'");

    // handle the non-leaf constraints first

    if (constraint instanceof WalkConstraint) {
      WalkConstraint wc = (WalkConstraint) constraint;
      return new WalkConstraint(translateModel(wc.getAnchoredConstraint()),
                                translateModel(wc.getUnanchoredConstraint()));
    }

    if (constraint instanceof TransitiveConstraint) {
      TransitiveConstraint tc = (TransitiveConstraint) constraint;
      return new TransitiveConstraint(translateModel(tc.getAnchoredConstraint()),
                                      translateModel(tc.getUnanchoredConstraint()));
    }

    if (constraint instanceof SingleTransitiveConstraint) {
      SingleTransitiveConstraint stc = (SingleTransitiveConstraint) constraint;
      return new SingleTransitiveConstraint(translateModel(stc.getTransConstraint()));
    }

    // is leaf constraint, so get elements and translate model

    ConstraintElement subj = constraint.getElement(0);
    ConstraintElement pred = constraint.getElement(1);
    ConstraintElement obj  = constraint.getElement(2);

    LocalNode model = (LocalNode) constraint.getElement(3);
    model = lookupRealNode(model);

    if (logger.isDebugEnabled()) {
      logger.debug("Resolved model '" + model + "'");
      logger.debug("constraint: subj='" + constraint.getElement(0) + "'");
      logger.debug("constraint: pred='" + constraint.getElement(1) + "'");
      logger.debug("constraint:  obj='" + constraint.getElement(2) + "'");
    }

    // handle each constraint type

    if (constraint instanceof ConstraintImpl)
      return new ConstraintImpl(subj, pred, obj, model);

    if (constraint instanceof ConstraintIs)
      return new ConstraintIs(subj, obj, model);

    if (constraint instanceof ConstraintNegation) {
      ConstraintNegation cn = (ConstraintNegation) constraint;
      Constraint inner;
      if (cn.isInnerConstraintIs())
        inner = new ConstraintIs(subj, obj, model);
      else
        inner = new ConstraintImpl(subj, pred, obj, model);
      return new ConstraintNegation(inner);
    }

    if (constraint instanceof ConstraintOccurs)
      return new ConstraintOccurs(subj, obj, model);
    if (constraint instanceof ConstraintNotOccurs)
      return new ConstraintNotOccurs(subj, obj, model);
    if (constraint instanceof ConstraintOccursLessThan)
      return new ConstraintOccursLessThan(subj, obj, model);
    if (constraint instanceof ConstraintOccursMoreThan)
      return new ConstraintOccursMoreThan(subj, obj, model);

    throw new QueryException("Unknown constraint class '" + constraint.getClass().getName() + "'");
  }


  private long lookupRealNode(long model) throws QueryException {
    return lookupRealNode(new LocalNode(model)).getValue();
  }

  private LocalNode lookupRealNode(LocalNode model) throws QueryException {
    // check cache
    LocalNode res = (LocalNode) modelTranslationCache.get(model);
    if (res != null)
      return res;

    // nope, so convert to URI (globalize), rewrite URI, and convert back (localize)
    URI modelURI;
    try {
      modelURI = toURI(model.getValue());
    } catch (ResolverException re) {
      throw new QueryException("Failed to get model URI", re);
    }

    URI resURI = null;
    try {
      resURI = toRealModelURI(modelURI);
      long resId = resolverSession.lookup(new URIReferenceImpl(resURI, false));
      res = new LocalNode(resId);
    } catch (ResolverException re) {
      throw new QueryException("Failed to parse model '" + modelURI + "'", re);
    } catch (LocalizeException le) {
      throw new QueryException("Couldn't localize model '" + resURI + "'", le);
    }

    // cache and return the result
    if (logger.isDebugEnabled())
      logger.debug("Adding translation for model '" + modelURI + "' -> '" + resURI + "'");

    modelTranslationCache.put(model, res);
    return res;
  }

  /** 
   * Get the model name from the uri, checking that the uri is properly formed.
   * 
   * @param uri the filter uri; must be of the form 
   *            &lt;dbURI&gt;#filter:model=&lt;modelName&gt;;&lt;p2&gt;=&lt;value2&gt;
   * @return the modelName
   * @throws ResolverException if the uri is not properly formed
   */
  static String getModelName(URI uri) throws ResolverException {
    String modelName = uri.getRawFragment();
    if (!modelName.startsWith("filter:"))
      throw new ResolverException("Model-name '" + modelName + "' doesn't start with 'filter:'");

    String[] params = modelName.substring(7).split(";");
    for (int idx = 0; idx < params.length; idx++) {
      if (params[idx].startsWith("model="))
        return params[idx].substring(6);
    }

    throw new ResolverException("invalid model name encountered: '" + uri + "' - must be of " +
                                "the form <dbURI>#filter:model=<model>");
  }

  private URI toURI(long model) throws ResolverException {
    Node globalModel = null;

    // Globalise the model
    try {
      globalModel = resolverSession.globalize(model);
    } catch (GlobalizeException ge) {
      throw new ResolverException("Couldn't globalize model", ge);
    }

    // Check that our node is a URIReference
    if (!(globalModel instanceof URIReference))
      throw new ResolverException("Model parameter " + globalModel + " isn't a URI reference");

    // Get the URI from the globalised node
    return ((URIReference) globalModel).getURI();
  }

  /**
   * <code>rmi://localhost/fedora#filter:model=ri;ds=RELS-EXT</code> -&gt; <code>rmi://localhost/fedora#ri</code>
   */
  private URI toRealModelURI(URI model) throws ResolverException {
    return dbURI.resolve('#' + getModelName(model));
  }

  private Set toSetOfTriples(final Statements stmts) {
    return new StatementTrippleSet(stmts, resolverSession);
  }

  /** 
   * This implements a read-only {@link java.util.Set Set} of {@link org.jrdf.graph.Triple Triple}s
   * based on on a list of statements.
   */
  private static class StatementTrippleSet extends AbstractSet {
    private final Statements      stmts;
    private final ResolverSession resolverSession;

    /** 
     * Create a new instance. 
     * 
     * @param resolverSession the resolver-session to use for globalizing nodes
     * @param stmts           the underlying statements to represent
     */
    public StatementTrippleSet(Statements stmts, ResolverSession resolverSession) {
      this.stmts           = stmts;
      this.resolverSession = resolverSession;
    }

    public int size() {
      try {
        return (int) stmts.getRowCount();
      } catch (TuplesException te) {
        throw new RuntimeException(te);
      }
    }

    public Iterator iterator() {
      return new StatementTrippleIterator();
    }

    private class StatementTrippleIterator implements Iterator {
      private Triple  curr;
      private boolean haveMore;

      public StatementTrippleIterator() {
        try {
          stmts.beforeFirst();
          stmts.next();
          nextTriple();
        } catch (TuplesException te) {
          throw new RuntimeException(te);
        }
      }

      public boolean hasNext() {
        return haveMore;
      }

      public Object next() {
        if (!haveMore)
          throw new NoSuchElementException();

        Triple res = curr;
        nextTriple();
        return res;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

      private void nextTriple() {
        curr = new AbstractTriple() {
          {
            try {
              subjectNode   = (SubjectNode) globalize(stmts.getSubject());
              predicateNode = (PredicateNode) globalize(stmts.getPredicate());
              objectNode    = (ObjectNode) globalize(stmts.getObject());
            } catch (TuplesException te) {
              throw new RuntimeException(te);
            }
          }
        };

        try {
          haveMore = stmts.next();
        } catch (TuplesException te) {
          throw new RuntimeException(te);
        }
      }

      private Node globalize(long node) {
        try {
          return resolverSession.globalize(node);
        } catch (GlobalizeException ge) {
          throw new RuntimeException("Couldn't globalize node " + node, ge);
        }
      }
    }
  }
}
