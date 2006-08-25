/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.alerts.impl;

import java.io.IOException;
import java.util.Set;

import org.topazproject.xacml.AbstractSimplePEP;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for alerts.
 *
 * @author foo
 */
public abstract class AlertsPEP extends AbstractSimplePEP {
  // Note: these operations must be referenced in the policy/Alerts.xml policy file
  /** The action that represents the create-a-foo operation in XACML policies. */
  public static final String CREATE_ALERT = "alerts:createAlert";
  /** The action that represents the delete-a-foo operation in XACML policies. */
  public static final String DELETE_ALERT = "alerts:deleteAlert";

  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
                                                           CREATE_ALERT,
                                                           DELETE_ALERT,
                                                         };

  /** The list of all supported obligations */
  protected static final String[][] SUPPORTED_OBLIGATIONS = new String[][] {
                                                           null,
                                                           null,
                                                         };

  protected AlertsPEP(PDP pdp, Set subjAttrs)
      throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }
}
