/* $HeadURL::
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.permissions;

import java.io.IOException;

import java.util.Set;

import org.topazproject.xacml.AbstractSimplePEP;

import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for the permission accounts manager.
 *
 * @author Pradeep Krishnan
 */
public abstract class PermissionsPEP extends AbstractSimplePEP {
  /**
   * The action that represents a permission grant operation in XACML policies.
   */
  public static final String GRANT = "permissions:grant";

  /**
   * The action that represents a permission revoke operation in XACML policies.
   */
  public static final String REVOKE = "permissions:revoke";

  /**
   * The action that represents canceling a permission grant operation in XACML policies.
   */
  public static final String CANCEL_GRANTS = "permissions:cancelGrants";

  /**
   * The action that represents cancelling a permission revoke operation in XACML policies.
   */
  public static final String CANCEL_REVOKES = "permissions:cancelRevokes";

  /**
   * The action that represents a list permission grants operation in XACML policies.
   */
  public static final String LIST_GRANTS = "permissions:listGrants";

  /**
   * The action that represents a list permission revokes operation in XACML policies.
   */
  public static final String LIST_REVOKES = "permissions:listRevokes";

  /**
   * The list of all supported actions
   */
  protected static final String[] SUPPORTED_ACTIONS =
    new String[] { GRANT, REVOKE, CANCEL_GRANTS, CANCEL_REVOKES, LIST_GRANTS, LIST_REVOKES };

  /**
   * The list of all supported obligations
   */
  protected static final String[][] SUPPORTED_OBLIGATIONS =
    new String[][] { null, null, null, null, null, null };

  /*
   *@see org.topazproject.xacml.AbstractSimplePEP
   *
   */
  protected PermissionsPEP(PDP pdp, Set subjAttrs)
                    throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }
}
