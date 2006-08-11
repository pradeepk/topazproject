/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap;

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

/**
 * The XACML PEP for profiles.
 *
 * @author Ronald Tschalär
 */
public abstract class ProfilesPEP extends AbstractSimplePEP {
  /** The action that represents a set-profile operation in XACML policies. */
  public static final String SET_PROFILE = "profiles:setProfile";

  /** The action that represents a get-profile operation in XACML policies. */
  public static final String GET_PROFILE = "profiles:getProfile";

  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
                                                           SET_PROFILE,
                                                           GET_PROFILE,
                                                         };

  /** The list of all supported obligations */
  protected static final String[][] SUPPORTED_OBLIGATIONS = new String[][] {
                                                           null,
                                                           null,
                                                         };

  /** The id of the attribute that represents the list allowed readers. */
  public static final URI ALLOWED_READERS_ID =
      URI.create("urn:topazproject:names:tc:xacml:1.0:attribute:profile-readers");

  /** The attribute type of an xs:anyURI attribute. */
  public static final URI ANY_URI_TYPE = URI.create(AnyURIAttribute.identifier);


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

  /**
   * Check if the current user is allowed read access according to the given list of readers.
   *
   * @param owner    the owner of the profile
   * @param readers  the list of allowed readers 
   * @throws SecurityException if the operation is not permitted
   */
  void checkReadAccess(String owner, String[] readers) throws SecurityException {
    Set resourceAttrs = new HashSet();

    resourceAttrs.add(new Attribute(Util.RESOURCE_ID, null, null,
                      new AnyURIAttribute(URI.create(owner))));

    Collection ras = new ArrayList(readers.length);
    for (int idx = 0; idx < readers.length; idx++)
      ras.add(new AnyURIAttribute(URI.create(readers[idx])));

    resourceAttrs.add(
        new Attribute(ALLOWED_READERS_ID, null, null, new BagAttribute(ANY_URI_TYPE, ras)));

    checkAccess(GET_PROFILE, resourceAttrs);
  }
}
