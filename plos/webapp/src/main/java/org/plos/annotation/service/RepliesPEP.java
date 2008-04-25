/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.plos.annotation.service;

import java.io.IOException;

import java.util.Set;

import org.plos.xacml.XacmlUtil;
import org.plos.xacml.AbstractSimplePEP;

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
   * The action that represents a listReplies operation in XACML policies. Note that this
   * permission is checked against the base uri of annotations.
   */
  public static final String LIST_REPLIES_IN_STATE = "replies:listRepliesInState";

  /**
   * The action that represents a setReplyState operation in XACML policies.
   */
  public static final String SET_REPLY_STATE = "replies:setReplyState";

  /**
   * The list of all supported actions
   */
  public static final String[] SUPPORTED_ACTIONS =
    new String[] {
                   CREATE_REPLY, DELETE_REPLY, GET_REPLY_INFO, LIST_REPLIES, LIST_ALL_REPLIES,
                   LIST_REPLIES_IN_STATE, SET_REPLY_STATE
    };

  /**
   * The list of all supported obligations
   */
  public static final String[][] SUPPORTED_OBLIGATIONS =
    new String[][] { null, null, null, null, null, null, null };

  static {
    init(RepliesPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
  }

  /*
   *     inherited javadoc
   *
   */
  public RepliesPEP() throws IOException, ParsingException, UnknownIdentifierException {
    this(XacmlUtil.lookupPDP("ambra.services.xacml.replies.pdpName"));
  }

  /*
   *     inherited javadoc
   *
   */
  protected RepliesPEP(PDP pdp)
                throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp);
  }
}
