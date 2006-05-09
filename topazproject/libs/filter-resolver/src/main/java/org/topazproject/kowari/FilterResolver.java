
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
import org.kowari.query.ConstraintImpl;
import org.kowari.query.QueryException;
import org.kowari.query.TuplesException;
import org.kowari.query.rdf.URIReferenceImpl;
import org.kowari.resolver.spi.DummyXAResource;
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
 *   #&lt;modelURI-nofragment&gt;#filter:&lt;modelName&gt;
 * </pre>
 * For example:
 * <pre>
 *   rmi://localhost:/fedora#filter:ri
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
  private final FedoraUpdater   fedoraUpdater;

  private Session sess;

  /** 
   * Create a new FilterResolver instance. 
   * 
   * @param dbURI           the absolute URI of the database; used as the base for creating the
   *                        absolute URI of a model
   * @param sysModelType    the system-model type; used when creating a new model
   * @param systemResolver  the system-resolver; used for creating and modifying models
   * @param resolverSession our environment; used for globalizing and localizing nodes
   * @param fedoraUpdater   the updater to which to queue model modifications
   */
  FilterResolver(URI dbURI, long sysModelType, Resolver systemResolver,
                 ResolverSession resolverSession, FedoraUpdater fedoraUpdater) {
    this.dbURI           = dbURI;
    this.sysModelType    = sysModelType;
    this.systemResolver  = systemResolver;
    this.resolverSession = resolverSession;
    this.fedoraUpdater   = fedoraUpdater;
  }

  public void setSession(SessionView session) {
    this.sess = (Session) session;
  }

  /**
   * @return a {@link DummyXAResource} with a 10 second transaction timeout
   */
  public XAResource getXAResource()
  {
    return new DummyXAResource(10);     // seconds before transaction timeout
  }

  public void createModel(long model, URI modelType) throws ResolverException, LocalizeException {
    // check model type
    if (!modelType.equals(MODEL_TYPE))
      throw new ResolverException("Unknown model-type '" + modelType + "'");

    // get system model type URI
    try {
      Node mtURI = resolverSession.globalize(sysModelType);
      if (mtURI instanceof URIReference) {
        modelType = ((URIReference) mtURI).getURI();
      } else {
        throw new ResolverException("systemModelType '" + mtURI + "' not a URIRef ");
      }
    } catch (GlobalizeException ge) {
      throw new ResolverException("Failed to globalize SystemModel Type", ge);
    }

    // convert filter model uri to real model uri
    URI modelURI = toRealModelURI(model);
    if (logger.isDebugEnabled())
      logger.debug("Creating model '" + modelURI + "'");

    // create the real model if it doesn't exist
    try {
      if (!sess.modelExists(modelURI)) {
        //s.createModel(modelURI, modelType);
        model = resolverSession.localizePersistent(new URIReferenceImpl(modelURI, false));
        systemResolver.createModel(model, modelType);
      }
    } catch (LocalizeException le) {
      throw new ResolverException("Error localizing model uri '" + modelURI + "'", le);
    } catch (QueryException qe) {
      throw new ResolverException("Error creating model " + modelURI, qe);
    }
  }

  public void modifyModel(long model, Statements statements, boolean occurs)
    throws ResolverException {
    if (logger.isDebugEnabled()) {
      URI modelURI = toRealModelURI(model);
      logger.debug("Modifying model '" + modelURI + "'");
    }

    /* Doesn't work (nested transaction)
    try {
      if (occurs)
        sess.insert(modelURI, toSetOfTriples(statements));
      else
        sess.delete(modelURI, toSetOfTriples(statements));
    } catch (QueryException qe) {
      throw new ResolverException("Error modifying model " + modelURI, qe);
    }
    */

    try {
      systemResolver.modifyModel(lookupRealNode(model), statements, occurs);
    } catch (QueryException qe) {
      throw new ResolverException("Failed to look up model", qe);
    }

    fedoraUpdater.queueMod(statements, occurs, resolverSession);
  }

  public void removeModel(long model) throws ResolverException {
    if (logger.isDebugEnabled()) {
      URI modelURI = toRealModelURI(model);
      logger.debug("Removing model '" + modelURI + "'");
    }

    /* TODO: what are the semantics of this, really?
    try {
      sess.removeModel(modelURI);
    } catch (QueryException qe) {
      throw new ResolverException("Error removing model " + modelURI, qe);
    }
    */
  }

  public Resolution resolve(Constraint constraint) throws QueryException {
    LocalNode modelNode = (LocalNode) constraint.getElement(3);
    LocalNode realNode  = lookupRealNode(modelNode);

    if (logger.isDebugEnabled()) {
      logger.debug("Resolving model '" + realNode + "'");
      logger.debug("constraint: subj='" + constraint.getElement(0) + "'");
      logger.debug("constraint: pred='" + constraint.getElement(1) + "'");
      logger.debug("constraint:  obj='" + constraint.getElement(2) + "'");
    }

    constraint = new ConstraintImpl(constraint.getElement(0), constraint.getElement(1),
                                    constraint.getElement(2), realNode);

    Tuples ans = ((SessionView) sess).resolve(constraint);
    if (ans instanceof Resolution)
      return (Resolution) ans;

    //FIXME
    //return new TuplesWrapperResolution(ans);
    return new EmptyResolution(constraint, false);
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

    URI resURI;
    String modelName = modelURI.getRawFragment();
    if (modelName.startsWith("filter:")) {
      try {
        resURI = dbURI.resolve('#' + modelName.substring(7));
        long resId = resolverSession.lookup(new URIReferenceImpl(resURI, false));
        res = new LocalNode(resId);
      } catch (LocalizeException le) {
        throw new QueryException("Couldn't localize model '" + model + "'", le);
      }
    } else {
      resURI = modelURI;
      res    = model;
    }

    // cache and return the result
    if (logger.isDebugEnabled())
      logger.debug("Adding translation for model '" + modelURI + "' -> '" + resURI + "'");

    modelTranslationCache.put(model, res);
    return res;
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
   * <code>rmi://localhost/fedora#filter:ri</code> -&gt; <code>rmi://localhost/fedora#ri</code>
   */
  private URI toRealModelURI(long model) throws ResolverException {
    URI uri = toURI(model);

    String modelName = uri.getRawFragment();
    if (!modelName.startsWith("filter:"))
      throw new ResolverException("Model-name '" + modelName + "' doesn't start with 'filter:'");

    return dbURI.resolve('#' + modelName.substring(7));
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
