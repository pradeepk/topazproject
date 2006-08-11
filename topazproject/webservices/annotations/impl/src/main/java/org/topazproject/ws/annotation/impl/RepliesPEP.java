/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.annotation.impl;

import java.io.IOException;

import java.util.Set;

import org.topazproject.xacml.AbstractSimplePEP;
import org.topazproject.xacml.Util;

import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for Replies Web Service.
 *
 * @author Pradeep Krishnan
 */
public class RepliesPEP extends AbstractSimplePEP {
  /**
   * The action that represents a createReply operation in XACML policies.
   */
  public static final String CREATE_REPLY = "replies:createReply";

  /**
   * The action that represents a deleteReply operation in XACML policies.
   */
  public static final String DELETE_REPLY = "replies:deleteReply";

  /**
   * The action that represents a getReply operation in XACML policies.
   */
  public static final String GET_REPLY_INFO = "replies:getReplyInfo";

  /**
   * The action that represents a listReplies operation in XACML policies.
   */
  public static final String LIST_REPLIES = "replies:listReplies";

  /**
   * The action that represents a listAllReplies operation in XACML policies.
   */
  public static final String LIST_ALL_REPLIES = "replies:listAllReplies";

  /**
   * The list of all supported actions
   */
  public static final String[] SUPPORTED_ACTIONS =
    new String[] { CREATE_REPLY, DELETE_REPLY, GET_REPLY_INFO, LIST_REPLIES, LIST_ALL_REPLIES };

  /**
   * The list of all supported obligations
   */
  public static final String[][] SUPPORTED_OBLIGATIONS =
    new String[][] { null, null, null, null, null };

  /*
   *    *@see org.topazproject.xacml.AbstractSimplePEP
   *
   */
  protected RepliesPEP(PDP pdp, Set subjAttrs)
                throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }
}
