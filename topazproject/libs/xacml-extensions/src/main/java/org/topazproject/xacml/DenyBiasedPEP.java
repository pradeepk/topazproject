/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.xacml;

import java.io.ByteArrayOutputStream;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.xacml.Obligation;
import com.sun.xacml.PDP;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;

/**
 * A base class for Deny-Biased-PEP implementations.
 *
 * @author Pradeep Krishnan
 */
public class DenyBiasedPEP {
  private PDP pdp;

  /**
   * Constructs a DenyBiasedPEP with the given PDP.
   *
   * @param pdp The PDP to evaluate requests against
   */
  public DenyBiasedPEP(PDP pdp) {
    this.pdp = pdp;
  }

  /**
   * Constructs a DenyBiasedPEP.
   */
  public DenyBiasedPEP() {
    this(null);
  }

  /**
   * Returns the PDP used for request evaluations.
   *
   * @return the pdp
   */
  public PDP getPDP() {
    return pdp;
  }

  /**
   * Sets the PDP used for request evaluations.
   *
   * @param pdp the PDP
   */
  public void setPDP(PDP pdp) {
    this.pdp = pdp;
  }

  /**
   * Evaluates a request. The steps are as follows:
   * 
   * <ul>
   * <li>
   * Evaluates the request against the PDP. Any result other than PERMIT is considered a failure.
   * Any <code>Obligation</code> other than what is known to the PEP is a failure. No explicit
   * PERMIT in the response is considered a failure.
   * </li>
   * <li>
   * Throws a <code>SecurityException</code> on failure.
   * </li>
   * <li>
   * Otherwise collects <code>Obligation</code> objects from all <code>Result</code> objects and
   * returns the Set, ignoring the resource of the <code>Result</code>
   * </li>
   * </ul>
   * 
   *
   * @param request The requet to evaluate.
   * @param knownObligations The Set of known obligation URIs that the PEP is prepared to fulfill.
   *
   * @return The Set of Obligations that the PEP must fulfill.
   *
   * @throws SecurityException to indicate evaluation results other than an explicit PERMIT.
   */
  public Set evaluate(RequestCtx request, Set knownObligations) {
    ResponseCtx response = pdp.evaluate(request);
    Set         results = response.getResults();

    Set         obligations = new HashSet();
    int         permit      = 0;

    Iterator    it = results.iterator();

    while (it.hasNext()) {
      Result result = (Result) it.next();

      switch (result.getDecision()) {
      case Result.DECISION_PERMIT:
        permit++; // Great.

        break;

      case Result.DECISION_DENY:
        throw new SecurityException("A XACML policy denied acess to " + result.getResource());

      case Result.DECISION_NOT_APPLICABLE:
        throw new SecurityException("No applicable XACML policies for " + result.getResource());

      case Result.DECISION_INDETERMINATE:

        Status                status = result.getStatus();
        ByteArrayOutputStream out = new ByteArrayOutputStream(512);
        status.encode(out);
        throw new SecurityException("XACML policy evaluation error:\n" + out.toString());
      }

      Iterator oit = result.getObligations().iterator();

      while (oit.hasNext()) {
        Obligation o = (Obligation) oit.next();

        if (!knownObligations.contains(o.getId()))
          throw new SecurityException("XACML policy contains an obligation that this PEP cannot"
                                      + " fulfill. The obligation id is " + o.getId());

        obligations.add(o);
      }
    }

    if (permit == 0)
      throw new SecurityException("No explicit permissions");

    return obligations;
  }
}
