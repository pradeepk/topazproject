/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.ratings.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.topazproject.ws.ratings.NoSuchIdException;
import org.topazproject.xacml.AbstractSimplePEP;
import org.topazproject.xacml.Util;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.ctx.Attribute;

/**
 * The XACML PEP for ratings.
 *
 * @author Ronald Tschalär
 */
public abstract class RatingsPEP extends AbstractSimplePEP {
  // Note: these operations must be referenced in the policy/Ratings.xml policy file
  /** The action that represents the set-ratings operation in XACML policies. */
  public static final String SET_RATINGS = "ratings:setRatings";
  /** The action that represents the get-ratings operation in XACML policies. */
  public static final String GET_RATINGS = "ratings:getRatings";
  /** The action that represents the get-stats operation in XACML policies. */
  public static final String GET_STATS = "ratings:getStats";

  /** The id of the attribute containing the URI of the object: {@value} */
  public static final URI OBJ_ID =
      URI.create("urn:topazproject:names:tc:xacml:1.0:resource:object-uri");

  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
                                                           SET_RATINGS,
                                                           GET_RATINGS,
                                                           GET_STATS,
                                                         };

  /** The list of all supported obligations */
  protected static final String[][] SUPPORTED_OBLIGATIONS = new String[][] {
                                                           null,
                                                           null,
                                                           null,
                                                         };

  protected RatingsPEP(PDP pdp, Set subjAttrs)
      throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }

  /** 
   * Check if the curent user may perform the requested action.
   * 
   * @param action one of the actions defined above
   * @param userId the ratings owner's internal id
   * @param object the object for which to get the ratings
   */
  protected void checkUserAccess(String action, String userId, String object)
      throws NoSuchIdException, SecurityException {
    Set resourceAttrs = new HashSet();

    resourceAttrs.add(
        new Attribute(Util.RESOURCE_ID, null, null, new AnyURIAttribute(toURI(userId))));
    if (object != null)
      resourceAttrs.add(new Attribute(OBJ_ID, null, null, new AnyURIAttribute(toURI(object))));

    checkAccess(action, resourceAttrs);
  }

  /** 
   * Check if the curent user may perform the requested action.
   * 
   * @param action one of the actions defined above
   * @param object the object for which to get the stats
   */
  protected void checkObjectAccess(String action, String object) throws SecurityException {
    try {
      checkAccess(action, toURI(object));
    } catch (NoSuchIdException nsie) {
      IllegalArgumentException iae = new IllegalArgumentException(nsie.getId());
      iae.initCause(nsie);
      throw iae;
    }
  }

  private static URI toURI(String uri) throws NoSuchIdException {
    try {
      URI res = new URI(uri);

      if (!res.isAbsolute())
        throw new NoSuchIdException(uri);

      return res;
    } catch (URISyntaxException use) {
      NoSuchIdException nsie = new NoSuchIdException(uri);
      nsie.initCause(use);
      throw nsie;
    }
  }
}
