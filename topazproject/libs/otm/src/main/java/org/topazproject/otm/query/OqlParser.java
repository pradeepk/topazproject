/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.query;

import java.util.ArrayList;
import java.util.List;

import antlr.LLkParser;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.TokenBuffer;
import antlr.TokenStream;

import org.apache.commons.lang.StringUtils;

/** 
 * This holds some common stuff for Oql parsers, such as collecting error and warning
 * messages.
 * 
 * @author Ronald Tschalär
 */
public abstract class OqlParser extends LLkParser {
  private final List<String> errs = new ArrayList<String>();
  private final List<String> wrns = new ArrayList<String>();

  protected OqlParser(ParserSharedInputState state, int k) {
    super(state, k);
  }

  protected OqlParser(TokenBuffer tokenBuf, int k) {
    super(tokenBuf, k);
  }

  protected OqlParser(TokenStream lexer, int k) {
    super(lexer, k);
  }

  @Override
  public void reportError(RecognitionException ex) {
    errs.add(ex.toString());
  }

  @Override
  public void reportError(String err) {
    errs.add(err);
  }

  @Override
  public void reportWarning(String wrn) {
    wrns.add(wrn);
  }

  public List<String> getErrors() {
    return errs;
  }

  public List<String> getWarnings() {
    return wrns;
  }

  public String getErrors(String join) {
    return StringUtils.join(errs, (join != null ? join : System.getProperty("line.separator")));
  }

  public String getWarnings(String join) {
    return StringUtils.join(wrns, (join != null ? join : System.getProperty("line.separator")));
  }
}
