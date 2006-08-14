/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article.impl;

import java.io.IOException;
import java.util.Set;

import org.topazproject.xacml.AbstractSimplePEP;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for Articles.
 *
 * @author Ronald Tschalär
 */
public abstract class ArticlePEP extends AbstractSimplePEP {
  /** The action that represents an ingest operation in XACML policies. */
  public static final String INGEST_ARTICLE = "articles:ingestArticle";

  /** The action that represents a delete operation in XACML policies. */
  public static final String DELETE_ARTICLE = "articles:deleteArticle";

  /** The action that represents a set-state operation in XACML policies. */
  public static final String SET_ARTICLE_STATE = "articles:setArticleState";

  /** The action that represents a get-object-url operation in XACML policies. */
  public static final String GET_OBJECT_URL = "articles:getObjectURL";

  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
                                                           INGEST_ARTICLE,
                                                           DELETE_ARTICLE,
                                                           SET_ARTICLE_STATE,
                                                           GET_OBJECT_URL,
                                                         };

  /** The list of all supported obligations */
  protected static final String[][] SUPPORTED_OBLIGATIONS = new String[][] {
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                         };

  protected ArticlePEP(PDP pdp, Set subjAttrs)
      throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }
}
