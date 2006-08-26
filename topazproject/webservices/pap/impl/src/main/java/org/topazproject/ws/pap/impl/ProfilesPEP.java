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

import org.topazproject.xacml.AbstractSimplePEP;
import org.topazproject.xacml.Util;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.ctx.Attribute;

import org.topazproject.ws.pap.NoSuchIdException;

/**
 * The XACML PEP for profiles.
 *
 * @author Ronald Tschalär
 */
public abstract class ProfilesPEP extends AbstractSimplePEP {
  /** The action that represents a get-display-name operation in XACML policies: {@value}. */
  public static final String GET_DISP_NAME = "profiles:getDisplayName";

  /** The action that represents a get-real-name operation in XACML policies: {@value}. */
  public static final String GET_REAL_NAME = "profiles:getRealName";

  /** The action that represents a get-title operation in XACML policies: {@value}. */
  public static final String GET_TITLE = "profiles:getTitle";

  /** The action that represents a get-gender operation in XACML policies: {@value}. */
  public static final String GET_GENDER = "profiles:getGender";

  /** The action that represents a get-email operation in XACML policies: {@value}. */
  public static final String GET_EMAIL = "profiles:getEmail";

  /** The action that represents a get-home-page operation in XACML policies: {@value}. */
  public static final String GET_HOME_PAGE = "profiles:getHomePage";

  /** The action that represents a get-weblog operation in XACML policies: {@value}. */
  public static final String GET_WEBLOG = "profiles:getWeblog";

  /** The action that represents a get-biography operation in XACML policies: {@value}. */
  public static final String GET_BIOGRAPHY = "profiles:getBiography";

  /** The action that represents a get-interests operation in XACML policies: {@value}. */
  public static final String GET_INTERESTS = "profiles:getInterests";

  /** The action that represents a get-publications operation in XACML policies: {@value}. */
  public static final String GET_PUBLICATIONS = "profiles:getPublications";

  /** The action that represents a set-profile operation in XACML policies. */
  public static final String SET_PROFILE = "profiles:setProfile";

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
   * @throws NoSuchIdException if the userId is not a valid URL
   * @throws SecurityException if access is denied
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
