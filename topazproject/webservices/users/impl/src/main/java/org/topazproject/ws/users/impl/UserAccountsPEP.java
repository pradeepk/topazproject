/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.users.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.topazproject.xacml.AbstractSimplePEP;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;

import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.ws.users.UserAccounts;

/**
 * The XACML PEP for the user accounts manager.
 *
 * @author Ronald Tschalär
 */
public abstract class UserAccountsPEP extends AbstractSimplePEP implements UserAccounts.Permissions {
  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
                                                           CREATE_USER,
                                                           DELETE_USER,
                                                           GET_STATE,
                                                           SET_STATE,
                                                           GET_AUTH_IDS,
                                                           SET_AUTH_IDS,
                                                           LOOKUP_USER,
                                                         };

  /** The list of all supported obligations */
  protected static final String[][] SUPPORTED_OBLIGATIONS = new String[][] {
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                         };

  protected UserAccountsPEP(PDP pdp, Set subjAttrs)
      throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }

  /** 
   * Check if the curent user may perform the requested action.
   * 
   * @param action one of the actions defined above
   * @param userId the user's internal account id
   */
  protected void checkUserAccess(String action, String userId)
      throws SecurityException, NoSuchUserIdException {
    URI userURI;
    try {
      userURI = new URI(userId);
      if (!userURI.isAbsolute())
        throw new NoSuchUserIdException(userId);
    } catch (URISyntaxException use) {
      throw (NoSuchUserIdException) new NoSuchUserIdException(userId).initCause(use);
    }

    checkAccess(action, userURI);
  }
}
