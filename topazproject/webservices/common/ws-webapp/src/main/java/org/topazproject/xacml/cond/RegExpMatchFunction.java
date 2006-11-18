/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.xacml.cond;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.cond.FunctionBase;

/**
 * A class that implements string-regexp-match. It executes {@link java.util.Pattern#matches
 * Pattern.matches}(pattern, match) and returns the result. If the argument is indeterminate, an
 * indeterminate result is returned.
 *
 * @author Pradeep Krishnan
 */
public class RegExpMatchFunction extends FunctionBase {
  /**
   * Standard identifier for the string-normalize-space function.
   */
  public static final String NAME_STRING_REGEXP_MATCH = FUNCTION_NS + "string-regexp-match";

  /**
   * Creates a new <code>RegExpMatchFunction</code> object.
   */
  public RegExpMatchFunction() {
    super(NAME_STRING_REGEXP_MATCH, 1, StringAttribute.identifier, false, 2,
          BooleanAttribute.identifier, false);
  }

  /**
   * Evaluate the function, using the specified parameters.
   *
   * @param inputs a <code>List</code> of <code>Evaluatable</code> objects representing the
   *        arguments passed to the function
   * @param context an <code>EvaluationCtx</code> so that the <code>Evaluatable</code> objects can
   *        be evaluated
   *
   * @return an <code>EvaluationResult</code> representing the function's result
   */
  public EvaluationResult evaluate(List inputs, EvaluationCtx context) {
    // Evaluate the arguments
    AttributeValue[] argValues = new AttributeValue[inputs.size()];
    EvaluationResult result = evalArgs(inputs, context, argValues);

    if (result != null)
      return result;

    String  pattern = ((StringAttribute) argValues[0]).getValue();
    String  match = ((StringAttribute) argValues[0]).getValue();
    boolean r     = Pattern.matches(pattern, match);

    return new EvaluationResult(BooleanAttribute.getInstance(r));
  }
}
