/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.users;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.topazproject.xacml.AbstractSimplePEP;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for the user accounts manager.
 *
 * @author Ronald Tschalär
 */
public abstract class UserAccountsPEP extends AbstractSimplePEP {
  /** The action that represents a user account creation operation in XACML policies. */
  public static final String CREATE_USER = "userAccounts:createUser";

  /** The action that represents a delete user account operation in XACML policies. */
  public static final String DELETE_USER = "userAccounts:deleteUser";

  /** The action that represents a get-authentication-ids operation in XACML policies. */
  public static final String GET_AUTH_IDS = "userAccounts:getAuthIds";

  /** The action that represents a set-authentication-ids operation in XACML policies. */
  public static final String SET_AUTH_IDS = "userAccounts:setAuthIds";

  /** The action that represents a look-up-user operation in XACML policies. */
  public static final String LOOKUP_USER = "userAccounts:lookUpUser";

  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
                                                           CREATE_USER,
                                                           DELETE_USER,
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
      throws SecurityException, NoSuchIdException {
    URI userURI;
    try {
      userURI = new URI(userId);
      if (!userURI.isAbsolute())
        throw new NoSuchIdException(userId);
    } catch (URISyntaxException use) {
      NoSuchIdException nsie = new NoSuchIdException(userId);
      nsie.initCause(use);
      throw nsie;
    }

    checkAccess(action, userURI);
  }
}
