/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.permissions.impl;

import java.io.IOException;

import java.util.Set;

import org.topazproject.ws.permissions.Permissions;

import org.topazproject.xacml.AbstractSimplePEP;

import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for the permission accounts manager.
 *
 * @author Pradeep Krishnan
 */
public abstract class PermissionsPEP extends AbstractSimplePEP
  implements Permissions.ServicePermissions {
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
