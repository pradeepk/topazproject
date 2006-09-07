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
  /** The action that represents starting alerts for a user. */
  public static final String START_USER = "alerts:startUser";

  /** The action that represents clearing/stopping alerts for a user. */
  public static final String CLEAR_USER = "alerts:clearUser";

  /** The action that represents sending alerts. */
  public static final String SEND_ALERTS = "alerts:sendAlerts";

  /** The action that represents reading meta data for a specific article. */
  public static final String READ_META_DATA = "alerts:readMetaData";
  
  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
                                                           START_USER,
                                                           CLEAR_USER,
                                                           SEND_ALERTS,
                                                           READ_META_DATA,
                                                         };

  /** The list of all supported obligations */
  protected static final String[][] SUPPORTED_OBLIGATIONS = new String[][] {
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                         };

  protected AlertsPEP(PDP pdp, Set subjAttrs)
      throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }
}
