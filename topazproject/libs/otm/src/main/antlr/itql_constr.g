/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

header
{
/*
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.query;

import antlr.RecognitionException;
import antlr.collections.AST;
}

/**
 * This generates an iTQL AST from an OQL AST. It is assumed that field-name-to-predicate
 * expansion has already been done.
 *
 * <p>Only the select and where clauses are transformed; the resulting AST has the following
 * form:
 * <pre>
 *   pexpr:   clist
 *          | #(SUBQ query)
 *          | #(COUNT query)
 *   where: oexpr
 *   oexpr: #(OR  (aexpr)+)
 *   aexpr: #(AND (dterm)+)
 *   dterm:   cfact
 *          | #(MINUS dterm oexpr)
 *   cfact:   clist
 *          | oexpr
 *          | #(TRANS cnstr cnstr? )
 *          | #(WALK  cnstr cnstr )
 *   clist:   #(AND (cnstr)+)
 *          | cnstr
 *   cnstr: #(TRIPLE ID ID ID ID?)
 * </pre>
 *
 * @author Ronald Tschalär 
 */
class ItqlConstraintGenerator extends TreeParser("OqlTreeParser");

options {
    importVocab = Query;
    exportVocab = Constraints;
    buildAST    = true;
}

tokens {
    TRIPLE  = "triple";
    MINUS   = "minus";
    TRANS   = "trans";
    WALK    = "walk";
    COUNT   = "count";
}

{
    private static final String TMP_VAR_PFX = "oqltmp2_";
    private              int    varCnt = 0;

    private AST nextVar() {
      String v = TMP_VAR_PFX + varCnt++;
      OqlAST res = (OqlAST) #([ID, v]);
      res.setIsVar(true);
      return res;
    }

    private AST addTriple(AST list, AST prevVar, AST prevPred) {
      if (prevPred != null) {
        AST obj = nextVar();
        list.addChild(makeTriple(prevVar, prevPred, obj));
        prevVar = obj;
      }
      return prevVar;
    }

    private OqlAST makeID(Object obj) {
      if (obj instanceof OqlAST) {
        OqlAST ast = (OqlAST) astFactory.dup((OqlAST) obj);
        if (ast.getType() != ID)
          ast.setType(ID);
        return ast;
      }

      String str = (String) obj;
      return (OqlAST) #([ID, str]);
    }

    private AST makeTriple(Object s, Object p, Object o) {
      OqlAST sa = makeID(s);
      OqlAST pa = makeID(p);
      OqlAST oa = makeID(o);

      String m = (p instanceof OqlAST) ? ((OqlAST) p).getModel() : null;
      OqlAST ma = (m != null) ? makeID(m) : null;

      if (pa.isInverse())
        return #([TRIPLE,"triple"], oa, pa, sa, ma);
      else
        return #([TRIPLE,"triple"], sa, pa, oa, ma);
    }

    private AST handleFunction(AST ns, AST fname, AST args, AST resVar, boolean isProj)
        throws RecognitionException {
      if (isProj && ns == null && fname.getText().equals("count"))
        return handleCount(args, resVar);

      throw new RecognitionException("unrecognized function call '" +
                                     (ns != null ? ns.getText() + ":" : "") + fname.getText() +
                                     "'");
    }

    private AST handleCount(AST args, AST resVar) throws RecognitionException {
      // parse args
      if (args.getNumberOfChildren() != 2)
        throw new RecognitionException("count() must have exactly 1 argument, but found " +
                                       (args.getNumberOfChildren() / 2));
      AST var = args.getFirstChild();
      AST arg = var.getNextSibling();

      // handle count(<constant>)
      if (arg.getType() == TRIPLE)
        return #([QSTRING, "'1'"]);

      assert arg.getType() == AND;

      // need to equate the expression's end var with the expected result var
      AST pexpr = astFactory.dupTree(arg);
      if (!var.equals(resVar))
        pexpr.addChild(makeTriple(var, "<mulgara:equals>", resVar));

      // create subquery
      AST from  = #([FROM, "from"], #([ID, "dummy"]), #([ID, "dummy"]));
      AST where = #([WHERE, "where"], pexpr);
      AST proj  = #([PROJ, "projection"], astFactory.dup(resVar), astFactory.dup(resVar));

      return #([COUNT, "count"], #([SELECT, "select"], from, where, proj));
    }
}


query
    :   #(SELECT #(FROM fclause) #(WHERE w:wclause) #(PROJ sclause[#w]) (oclause)? (lclause)? (tclause)?)
    ;


fclause
    :   #(COMMA fclause fclause)
    |   ID ID
    ;


sclause[AST w]
    :   #(COMMA sclause[w] sclause[w])
    |   v:ID pexpr[#v, w]
    ;

pexpr[AST var, AST where]
    :   #(SUBQ query)
    |   ! f:factor[var, true] {
          // all expressions except count (which really is a subquery) get added to the where clause
          if (#f.getType() == COUNT)
            #pexpr = #f;
          else {
            where.addChild(#f);
            #pexpr = astFactory.dup(var);
          }
        }
    ;   // pexpr is now either a subquery or the variable


wclause
    : ! (e:expr)? { #wclause = #([AND, "and"], #e); /* so we can add projections */ }
    ;

expr
{ AST var; }
    : #(AND (expr)+)
    | #(OR  (expr)+)

    | ! #(ASGN ID af:factor[#ID, false]) { #expr = #af; }

    | ! #(EQ { var = nextVar(); } ef1:factor[var, false] ef2:factor[var, false]) {
        #expr = #([AND,"and"], #ef1, #ef2);
      }

    | ! #(NE { var = nextVar(); } nf1:factor[var, false] nf2:factor[var, false]) {
        #expr = #([MINUS,"minus"], #nf1, #nf2);
      }

    | fcall[nextVar(), false]
    ;

factor[AST var, boolean isProj]
    : constant[var]
    | fcall[var, isProj]
    | deref[var]
    ;

constant[AST var]
    : ! QSTRING ((DHAT type:URIREF) | (AT lang:ID))? {
          String lit = #QSTRING.getText();
          if (#type != null)
            lit += "^^" + #type.getText();
          else if (#lang != null)
            lit += "@" + #lang.getText();
          #constant = makeTriple(var, "<mulgara:is>", lit);
        }

    | ! URIREF {
          #constant = makeTriple(var, "<mulgara:is>", #URIREF);
        }
    ;

fcall[AST var, boolean isProj]
{ AST args = #([COMMA]), va; }
    : ! #(FUNC ns:ID (COLON fn:ID)?
               (arg:factor[va = nextVar(), isProj] { args.addChild(#va); args.addChild(#arg); })*) {
          if (#fn == null) {
            #fn = #ns;
            #ns = null;
          }

          #fcall = handleFunction(#ns, #fn, args, var, isProj);
        }
    ;

deref[AST var]
{ AST prevVar, prevPred = null, res = #([AND,"and"]); boolean wantPred = false; }
    :! #(REF (   v:ID { prevVar = #v; }
               | r:deref[prevVar = nextVar()] { res.addChild(#r); }
             )
             (   p:URIREF {
                   prevVar  = addTriple(res, prevVar, prevPred);
                   prevPred = #p;
                 }
               | #(EXPR pv:ID (e:expr)?) {
                   prevVar  = addTriple(res, prevVar, prevPred);
                   prevPred = #pv;
                   res.addChild(#e);
                 }
             )*
             (STAR {
                 prevVar  = addTriple(res, prevVar, prevPred);
                 wantPred = true;
             }
             )?
       ) {
         if (wantPred)
           res.addChild(makeTriple(prevVar, var, nextVar()));
         else if (prevPred != null)
           res.addChild(makeTriple(prevVar, prevPred, var));
         else if (!prevVar.equals(var))         // XXX
           res.addChild(makeTriple(prevVar, "<mulgara:equals>", var));

         #deref = res;
      }
    ;


oclause
    : #(ORDER (oitem)+)
    ;

oitem
    : ID (ASC|DESC)?
    ;

lclause
    : #(LIMIT NUM)
    ;

tclause
    : #(OFFSET NUM)
    ;

