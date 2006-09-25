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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.topazproject.ws.pap.Profiles;

import org.topazproject.xacml.AbstractSimplePEP;
import org.topazproject.xacml.Util;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.ctx.Attribute;

import org.topazproject.ws.users.NoSuchUserIdException;

/**
 * The XACML PEP for profiles.
 *
 * @author Ronald Tschalär
 */
public abstract class ProfilesPEP extends AbstractSimplePEP implements Profiles.Permissions {
  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
                                                           GET_DISP_NAME,
                                                           GET_REAL_NAME,
                                                           GET_TITLE,
                                                           GET_GENDER,
                                                           GET_EMAIL,
                                                           GET_HOME_PAGE,
                                                           GET_WEBLOG,
                                                           GET_BIOGRAPHY,
                                                           GET_INTERESTS,
                                                           GET_PUBLICATIONS,
                                                           SET_PROFILE,
                                                         };

  /** The list of all supported obligations */
  protected static final String[][] SUPPORTED_OBLIGATIONS = null;

  protected ProfilesPEP(PDP pdp, Set subjAttrs)
      throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }

  /** 
   * Check if the curent user may perform the requested action. Not used for read-access checks;
   * see {@link @checkReadAccess checkReadAccess} for that instead.
   * 
   * @param action one of the actions defined above
   * @param userId the profile owner's internal id
   * @throws NoSuchUserIdException if the userId is not a valid URL
   * @throws SecurityException if access is denied
   */
  protected void checkUserAccess(String action, String userId)
      throws NoSuchUserIdException, SecurityException {
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
