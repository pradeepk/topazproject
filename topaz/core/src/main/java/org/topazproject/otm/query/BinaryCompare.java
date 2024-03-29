/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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

package org.topazproject.otm.query;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import antlr.ASTFactory;
import antlr.RecognitionException;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.SessionFactory;

/**
 * Binary compare functions. This currently implements gt, ge, lt, and le.
 *
 * @author Ronald Tschalär
 */
class BinaryCompare implements BooleanConditionFunction, ConstraintsTokenTypes {
  private static final String[] DATE_TYPES = {
      Rdf.xsd + "date", Rdf.xsd + "dateTime", Rdf.xsd + "gYear", Rdf.xsd + "gYearMonth",
  };
  private static final String[] NUM_TYPES = {
      Rdf.xsd + "decimal", Rdf.xsd + "float", Rdf.xsd + "double", Rdf.xsd + "integer",
      Rdf.xsd + "nonPositiveInteger", Rdf.xsd + "negativeInteger", Rdf.xsd + "long",
      Rdf.xsd + "int", Rdf.xsd + "short", Rdf.xsd + "byte", Rdf.xsd + "nonNegativeInteger",
      Rdf.xsd + "unsignedLong", Rdf.xsd + "unsignedInt", Rdf.xsd + "unsignedShort",
      Rdf.xsd + "unsignedByte", Rdf.xsd + "positiveInteger",
  };
  private static final URI      XSD_GRAPH_TYPE =
                                        URI.create("http://mulgara.org/mulgara#XMLSchemaModel");
  private static final URI      STR_GRAPH_TYPE =
                                        URI.create("http://topazproject.org/graphs#StringCompare");

  static {
    Arrays.sort(DATE_TYPES);
    Arrays.sort(NUM_TYPES);
  }

  /** the function name */
  protected final String         name;
  /** the current session-factory */
  protected final SessionFactory sf;

  /**
   * Create a new binary-compare function instance.
   *
   * @param name  the function name
   * @param args  the arguments to the function
   * @param types the argument types
   * @param sf    the current session-factory
   * @throws RecognitionException if the number of arguments is not 2 or if the argument's types
   *                              are not identical
   */
  protected BinaryCompare(String name, List<OqlAST> args, List<ExprType> types, SessionFactory sf)
      throws RecognitionException {
    this.name = name;
    this.sf   = sf;

    // check argument type compatiblity
    if (args.size() != 2)
      throw new RecognitionException(name + "() must have exactly 2 arguments, but found " +
                                     args.size());

    // check argument type compatiblity
    ExprType ltype = types.get(0);
    ExprType rtype = types.get(1);

    if (ltype != null && rtype != null) {
      ExprType.Type lt = ltype.getType();
      ExprType.Type rt = rtype.getType();

      if (lt == ExprType.Type.CLASS || lt == ExprType.Type.EMB_CLASS ||
          rt == ExprType.Type.CLASS || rt == ExprType.Type.EMB_CLASS)
        throw new RecognitionException(name + "(): can't compare class types");

      if (lt != rt)
        throw new RecognitionException(name + "(): left and right side are not of same type: " +
                                       lt + " != " + rt);
      if (lt == ExprType.Type.TYPED_LIT && !ltype.getDataType().equals(rtype.getDataType()))
        throw new RecognitionException(name + "(): left and right side are not of same " +
                                       "data-type: " + ltype.getDataType() + " != " +
                                       rtype.getDataType());
    }
  }

  public String getName() {
    return name;
  }

  public ExprType getReturnType() {
    return null;
  }

  public boolean isBinaryCompare() {
    return true;
  }

  public ExprType getOutputVarType(int arg) throws RecognitionException {
    throw new RecognitionException("Argument " + arg + " to " + name + "() is not an output");
  }

  public void postPredicatesHook(OqlAST pre, OqlAST post) {
  }

  public OqlAST toItql(List<OqlAST> args, List<OqlAST> vars, OqlAST resVar, ASTFactory af,
                       String locVarPfx)
      throws RecognitionException {
    return toItql(args.get(0), vars.get(0), args.get(1), vars.get(1), af);
  }

  /**
   * Implements gt, ge, lt, and le.
   *
   * @param larg the left argument
   * @param lvar the left argument's result variable
   * @param rarg the right argument
   * @param rvar the right argument's result variable
   * @param af   the ast-factory to use
   */
  protected OqlAST toItql(OqlAST larg, OqlAST lvar, OqlAST rarg, OqlAST rvar, ASTFactory af)
      throws RecognitionException {
    // create expression
    String pred, iprd;
    URI    graphType;
    if (isDate(lvar)) {
      pred  = (name.charAt(0) == 'l') ? "<mulgara:before>" : "<mulgara:after>";
      iprd  = (name.charAt(0) != 'l') ? "<mulgara:before>" : "<mulgara:after>";
      graphType = XSD_GRAPH_TYPE;
    } else if (isNum(lvar)) {
      pred  = (name.charAt(0) == 'l') ? "<mulgara:lt>" : "<mulgara:gt>";
      iprd  = (name.charAt(0) != 'l') ? "<mulgara:lt>" : "<mulgara:gt>";
      graphType = XSD_GRAPH_TYPE;
    } else {
      pred  = (name.charAt(0) == 'l') ? "<topaz:lt>" : "<topaz:gt>";
      iprd  = (name.charAt(0) != 'l') ? "<topaz:lt>" : "<topaz:gt>";
      graphType = STR_GRAPH_TYPE;
    }

    String graphUri = ASTUtil.getGraphUri(graphType, sf);

    OqlAST res = ASTUtil.makeTree(af, AND, "and", af.dupTree(larg), af.dupTree(rarg));
    if (name.equals("ge") || name.equals("le")) {
      if (isConstant(rarg)) {
        // this relies on itql-redux to eliminate the equals and replace it with <mulgara:is>
        res.addChild(ASTUtil.makeTree(af, OR, "or",
                                      ASTUtil.makeTriple(lvar, pred, rvar, graphUri, af),
                                      ASTUtil.makeTriple(lvar, "<mulgara:equals>", rvar, af)));
      } else if (isConstant(larg)) {
        // this relies on itql-redux to eliminate the equals and replace it with <mulgara:is>
        res.addChild(ASTUtil.makeTree(af, OR, "or",
                                      ASTUtil.makeTriple(lvar, pred, rvar, graphUri, af),
                                      ASTUtil.makeTriple(rvar, "<mulgara:equals>", lvar, af)));
      } else {
        /* do this when mulgara supports <mulgara:equals>
        res.addChild(ASTUtil.makeTree(af, OR, "or",
                                      ASTUtil.makeTriple(lvar, pred, rvar, graphUri, af),
                                      ASTUtil.makeTriple(lvar, "<mulgara:equals>", rvar, af)));
        */
        /* this requires the functions (minus) to support variables on both sides
        res = ASTUtil.makeTree(af, MINUS, "minus", res,
                               ASTUtil.makeTriple(lvar, iprd, rvar, graphUri, af));
        */
        res = ASTUtil.makeTree(af, MINUS, "minus", af.dupTree(res),
                ASTUtil.makeTree(af, AND, "and", res,
                                 ASTUtil.makeTriple(lvar, iprd, rvar, graphUri, af)));
      }
    } else {
      res.addChild(ASTUtil.makeTriple(lvar, pred, rvar, graphUri, af));
    }

    return res;
  }

  protected static boolean isDate(OqlAST var) {
    ExprType type = var.getExprType();
    return (type != null) && (type.getType() == ExprType.Type.TYPED_LIT) &&
           isDt(type.getDataType(), DATE_TYPES);
  }

  protected static boolean isNum(OqlAST var) {
    ExprType type = var.getExprType();
    return (type != null) && (type.getType() == ExprType.Type.TYPED_LIT) &&
           isDt(type.getDataType(), NUM_TYPES);
  }

  private static boolean isDt(String v, String[] list) {
    return Arrays.binarySearch(list, v) >= 0;
  }

  private static boolean isConstant(OqlAST node) {
    return node.getType() == TRIPLE &&
           node.getFirstChild().getNextSibling().getText().equals("<mulgara:is>");
  }
}
