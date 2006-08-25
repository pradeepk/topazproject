/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.topazproject.xacml.AbstractSimplePEP;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;

import org.topazproject.ws.pap.NoSuchIdException;

/**
 * The XACML PEP for preferences.
 *
 * @author Ronald Tschalär
 */
public abstract class PreferencesPEP extends AbstractSimplePEP {
  /** The action that represents a write operation in XACML policies. */
  public static final String SET_PREFERENCES = "preferences:setPreferences";

  /** The action that represents a read operation in XACML policies. */
  public static final String GET_PREFERENCES = "preferences:getPreferences";

  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
                                                           SET_PREFERENCES,
                                                           GET_PREFERENCES,
                                                         };

  /** The list of all supported obligations */
  protected static final String[][] SUPPORTED_OBLIGATIONS = new String[][] {
                                                           null,
                                                           null,
                                                         };

  protected PreferencesPEP(PDP pdp, Set subjAttrs)
      throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }

  /** 
   * Check if the curent user may perform the requested action.
   * 
   * @param action one of the actions defined above
   * @param userId the preferences owner's internal id
   */
  protected void checkUserAccess(String action, String userId)
      throws NoSuchIdException, SecurityException {
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
