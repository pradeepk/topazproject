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
import javax.transaction.xa.XAResource;

import org.apache.log4j.Logger;

import org.jrdf.graph.Node;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.LocalNode;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.resolver.spi.DummyXAResource;
import org.mulgara.resolver.spi.EmptyResolution;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.resolver.spi.TuplesWrapperResolution;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPObjectFactory;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.store.stringpool.xa.SPObjectFactoryImpl;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;

/** 
 * A resolver that provides string-comparison operations. Currently only case-insensitive literal
 * comparisons are supported.
 * 
 * @author Ronald Tschalär
 */
public class StringCompareResolver implements Resolver {
  private static final Logger logger = Logger.getLogger(StringCompareResolver.class);

  /** The session that this resolver is associated with. */
  private final ResolverSession resolverSession;

  /** The URI of the type describing string compare models. */
  private URI modelTypeURI;

  private StringCompareImpl[] impls;

  /**
   * Construct a resolver.
   *
   * @param resolverSession      the session this resolver is associated with
   * @param impls                the StringCompareImpl-s we support
   * @param modelTypeURI         the URI of the model type we're handling
   * @throws IllegalArgumentException  if <var>resolverSession</var> is <code>null</code>
   */
  StringCompareResolver(ResolverSession resolverSession, StringCompareImpl[] impls,
                        URI modelTypeURI)
      throws IllegalArgumentException {
    if (resolverSession == null)
      throw new IllegalArgumentException("Null 'resolverSession' parameter");

    this.resolverSession      = resolverSession;
    this.modelTypeURI         = modelTypeURI;
    this.impls                = impls;
  }

  /**
   * Create a model for string comparisons.
   */
  public void createModel(long model, URI modelType) throws ResolverException {
    if (!modelType.equals(modelTypeURI))
      throw new ResolverException("Wrong model type provided as a String Compare model");

    if (logger.isDebugEnabled())
      logger.debug("Creating type model " + model);
  }

  /**
   * Expose a callback object for enlistment by a transaction manager.
   */
  public XAResource getXAResource() {
    return new DummyXAResource(10);
  }

  /**
   * We don't support modifications.
   */
  public void modifyModel(long model, Statements statements, boolean occurs)
      throws ResolverException {
    throw new ResolverException("String Compare models are read only");
  }


  /**
   * Remove the cached model containing the contents of a URL.
   */
  public void removeModel(long model) throws ResolverException {
    if (logger.isDebugEnabled())
      logger.debug("Removing String Compare model " + model);
  }

  /**
   * Resolve a constraint.
   */
  public Resolution resolve(Constraint constraint) throws QueryException {
    if (logger.isDebugEnabled())
      logger.debug("Resolving " + constraint);

    // Validate "constraint" parameter
    if (constraint == null)
      throw new IllegalArgumentException("Null 'constraint' parameter");

    if (!(constraint.getModel() instanceof LocalNode)) {
      logger.warn("Ignoring solutions for non-local model " + constraint);
      return new EmptyResolution(constraint, false);
    }

    // doit
    try {
      // check the constraint for consistency and get predicate and object
      long predicate = ((LocalNode) constraint.getElement(1)).getValue();
      StringCompareImpl impl = null;
      for (int i = 0; i < impls.length; i++)
        if (predicate == impls[i].getNode()) {
          impl = impls[i];
          break;
        }
      if (impl == null)
        throw new QueryException("Predicate '" + resolverSession.globalize(predicate) + "' not " +
                                 "supported by String Compare resolver");

      if (constraint.getElement(2) instanceof Variable)
        throw new QueryException("Compare resolver does not support variables as the object: '" +
                                 constraint.getElement(2) + "'");

      LocalNode object    = (LocalNode) constraint.getElement(2);
      Node      valueNode = resolverSession.globalize(object.getValue());

      String  comp;
      URI     type;
      boolean compIsURI;
      if (valueNode instanceof Literal) {
        comp = ((Literal) valueNode).getLexicalForm();
        type = ((Literal) valueNode).getDatatypeURI();
        compIsURI = false;
      } else if (valueNode instanceof URIReference) {
        comp = ((URIReference) valueNode).getURI().toString();
        type = null;
        compIsURI = true;
      } else
        throw new QueryException("Compare resolver only supports literals and URI's as the " +
                                 "object: '" + valueNode + "'");

      // Evalute
      ConstraintElement subj = constraint.getElement(0);
      assert subj != null;

      if (logger.isDebugEnabled())
        logger.debug("Evaluating " + subj + " " + impl.getOp() + " '" + comp + "'");

      Tuples tuples;

      if (subj instanceof Variable) {
        // extract the variable from the constraint
        Variable variable = (Variable) subj;

        // convert the comparison string into a range
        SPObjectFactory spoFact = resolverSession.getSPObjectFactory();
        String   lowValue  = impl.lowValue(comp);
        String   highValue = impl.highValue(comp);
        SPObject begRangeObj;
        SPObject endRangeObj;
        if (compIsURI) {
          begRangeObj = (lowValue == null ? null : spoFact.newSPURI(URI.create(lowValue)));
          endRangeObj = (highValue == null ? null : spoFact.newSPURI(URI.create(highValue)));
        } else if (type != null) {
          begRangeObj = (lowValue == null ? null : spoFact.newSPTypedLiteral(lowValue, type));
          endRangeObj = (highValue == null ? null : spoFact.newSPTypedLiteral(highValue, type));
        } else {
          begRangeObj = (lowValue == null ? null : spoFact.newSPString(lowValue));
          endRangeObj = (highValue == null ? null : spoFact.newSPString(highValue));
        }

        // find the matching strings
        if (logger.isDebugEnabled())
          logger.debug("Finding string-pool-range from " + begRangeObj + " to " + endRangeObj);

        tuples = resolverSession.findStringPoolRange(begRangeObj, impl.incLowValue(),
                                                     endRangeObj, impl.incHighValue());

        tuples.renameVariables(constraint);
      } else {
        // subj must therefore be an instanceof LocalNode
        // we can shortcut the process here
        LocalNode n   = (LocalNode) subj;
        SPObject  spo = resolverSession.findStringPoolObject(n.getValue());

        if (logger.isDebugEnabled())
          logger.debug("Comparing '" + spo + "' to '" + comp + "'");

        if (spo == null)
          tuples = TuplesOperations.empty(); // false
        else if (impl.test(spo, comp))
          tuples = TuplesOperations.unconstrained(); // true
        else
          tuples = TuplesOperations.empty(); // false
      }

      if (logger.isDebugEnabled())
        logger.debug("Evaluated " + subj + " " + impl.getOp() + " '" + comp +
                     "': " + toString(tuples));

      // convert the tuples to a resolution
      if (tuples.isUnconstrained() || tuples.getRowCardinality() == tuples.ZERO)
        return new TuplesWrapperResolution(tuples, constraint);
      else
        return new FilteredTuplesResolution(tuples, comp, constraint, resolverSession, impl);

    } catch (GlobalizeException ge) {
      throw new QueryException("Couldn't convert internal data into a string", ge);
    } catch (StringPoolException spe) {
      throw new QueryException("Couldn't query constraint", spe);
    } catch (TuplesException te) {
      throw new QueryException("Error accessing results tuples", te);
    }
  }

  private String toString(Tuples t) throws GlobalizeException, TuplesException {
    StringBuffer res = new StringBuffer(50);
    res.append("{ ");

    t.beforeFirst();
    while (t.next())
      res.append(resolverSession.globalize(t.getColumnValue(0))).append(", ");

    if (res.length() > 2)
      res.setLength(res.length() - 2);
    res.append(" }");

    return res.toString();
  }

  /**
   * Filter the tuples, allowing only those that pass the test.
   */
  private static class FilteredTuplesResolution extends TuplesWrapperResolution {
    private final String            comp;
    private final ResolverSession   rslvrSess;
    private final StringCompareImpl impl;

    FilteredTuplesResolution(Tuples t, String comp, Constraint c,
                             ResolverSession resolverSession, StringCompareImpl impl) {
      super(t, c);
      this.comp      = comp;
      this.rslvrSess = resolverSession;
      this.impl      = impl;
    }

    public boolean equals(Object o) {
      if (!(o instanceof FilteredTuplesResolution))
        return false;

      FilteredTuplesResolution t = (FilteredTuplesResolution) o;
      return comp.equals(t.comp) && super.equals(o);
    }

    public boolean isMaterialized() {
      return false;
    }

    public int getRowCardinality() throws TuplesException {
      beforeFirst();

      if (!next())
        return ZERO;

      if (!next())
        return ONE;

      return MANY;
    }

    public boolean next() throws TuplesException {
      while (super.next()) {
        SPObject spo;
        try {
          spo = rslvrSess.findStringPoolObject(getColumnValue(0));
        } catch (StringPoolException spe) {
          throw new TuplesException("Error getting object from string pool", spe);
        }

        if (logger.isDebugEnabled())
          logger.debug(impl.getOp() + " Comparing '" + spo + "' to '" + comp + "'");

        if (impl.test(spo, comp))
          return true;
      }

      return false;
    }
  }
}
