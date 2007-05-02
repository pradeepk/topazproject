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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.Session;
import org.topazproject.otm.mapping.Mapper;

import antlr.ASTPair;
import antlr.RecognitionException;
import antlr.collections.AST;
}

/**
 * This is an AST transformer for OQL that replaces field references by their predicate
 * URI's. It also removes any '.id' elements, resolves away casts, does some checks the
 * variables (duplicate declarations, order by not referencing projections, etc), and
 * creates dummy variables for projections where no variable was specified. And finally
 * it associates types and models with the various nodes.
 *
 * @author Ronald Tschalär 
 */
class FieldTranslator extends TreeParser("OqlTreeParser");

options {
    importVocab = Query;
    buildAST    = true;
}

{
    private static final String TMP_VAR_PFX = "oqltmp1_";

    private Map<String, ExprType> vars = new HashMap<String, ExprType>();
    private Set<String>           prjs = new HashSet<String>();
    private Session               sess;
    private int                   varCnt = 0;

    public FieldTranslator(Session session) {
      this();
      sess = session;
      astFactory.setASTNodeClass(OqlAST.class);
    }

    private ExprType getTypeForVar(AST id) throws RecognitionException {
      if (!vars.containsKey(id.getText()))
        throw new RecognitionException("no variable '" + id.getText() +
                                       "' defined in from or where clauses");
      return vars.get(id.getText());
    }

    private ExprType getTypeForClass(AST clazz, String loc) throws RecognitionException {
      ClassMetadata md = sess.getSessionFactory().getClassMetadata(#clazz.getText());
      if (md == null && loc != null)
        throw new RecognitionException("unknown class '" + #clazz.getText() + "' in " + loc);
      return ExprType.classType(md);
    }

    private ExprType resolveField(ASTPair cur, ExprType type, AST field)
        throws RecognitionException {
      if (type == null || type.getType() != ExprType.Type.CLASS)
        throw new RecognitionException("can't dereference type '" + type + "'; " +
                                       "current node: '" + cur.root + "', field: '" + field +
                                       "'");

      Mapper m = type.getMeta().getMapperByName(field.getText());
      if (m != null) {
        String uri = "<" + m.getUri() + ">";
        AST ref = #([URIREF, uri]);
        updateAST(ref, type, m, false);
        astFactory.addASTChild(cur, ref);

        return getTypeForMapper(m);
      }

      m = type.getMeta().getIdField();
      if (m != null && field.getText().equals(m.getName()))     // ignore id fields
        return type;

      throw new RecognitionException("no field '" + field.getText() + "' in " +
                                     type.getMeta().getSourceClass());
    }

    private ExprType handlePredicate(ASTPair cur, ExprType type, AST ref)
        throws RecognitionException {
      if (type != null && type.getType() != ExprType.Type.CLASS &&
          type.getType() != ExprType.Type.URI)
        throw new RecognitionException("can't dereference type '" + type + "'; " +
                                       "current node: '" + cur.root + "', reference: '" + ref +
                                       "'");

      astFactory.addASTChild(cur, ref);

      String uri = ref.getText().substring(1, ref.getText().length() - 1);
      Mapper m = type.getMeta().getMapperByUri(uri, false);
      updateAST(ref, type, m, false);

      return getTypeForMapper(m);
    }

    private ExprType getTypeForMapper(Mapper m) {
      if (m == null)
        return null;

      ClassMetadata md;
      if ((md = sess.getSessionFactory().getClassMetadata(m.getComponentType())) != null)
        return ExprType.classType(md);

      if (m.typeIsUri())
        return ExprType.uriType();

      if (m.getDataType() != null)
        return ExprType.literalType(m.getDataType());

      return ExprType.literalType();
    }

    private void updateAST(AST ast, ExprType type, Mapper m, boolean isVar)
        throws RecognitionException {
      OqlAST a = (OqlAST) ast;
      if (type != null)
        a.setExprType(type);

      if (type != null && type.getType() == ExprType.Type.CLASS)
        a.setModel(getModelUri(type.getMeta().getModel()));     // FIXME: should come from Mapper

      if (m != null && m.hasInverseUri()) {
        a.setIsInverse(true);
        if (m.getModel() != null)
          a.setModel(getModelUri(m.getModel()));
      }

      a.setIsVar(isVar);
    }

    private String getModelUri(String modelId) throws RecognitionException {
      ModelConfig mc = sess.getSessionFactory().getModel(modelId);
      if (mc == null)
        throw new RecognitionException("Unable to find model '" + modelId + "'");
      return mc.getUri().toString();
    }

    private void addVar(AST var, AST clazz) throws RecognitionException {
      ExprType type = (clazz != null) ? getTypeForClass(clazz, "from clause") : null;
      if (vars.containsKey(var.getText()))
        throw new RecognitionException("Duplicate variable declaration: var='" + var.getText() +
                                       "', prev type='" + vars.get(var.getText()) +
                                       "', new type='" + type + "'");
      vars.put(var.getText(), type);
      updateAST(var, type, null, true);
    }

    private AST nextVar() {
      String v = TMP_VAR_PFX + varCnt++;
      return #([ID, v]);
    }

    private void checkProjVar(String var) throws RecognitionException {
      if (prjs.contains(var))
        throw new RecognitionException("Duplicate projection variable declaration: var='" + var +
                                       "'");
      prjs.add(var);
    }

    private void checkTypeCompatibility(ExprType et1, ExprType et2, AST expr) {
      // assume unknown type is compatible with anything
      if (et1 == null || et2 == null)
        return;

      ExprType.Type t1 = et1.getType();
      ExprType.Type t2 = et2.getType();

      // same type (TODO: we should probably check class compatibility too)
      if (t1 == t2 &&
            (t1 == ExprType.Type.URI || t1 == ExprType.Type.UNTYPED_LIT ||
             t1 == ExprType.Type.CLASS && et1.getMeta().equals(et2.getMeta()) ||
             t1 == ExprType.Type.TYPED_LIT && et1.getDataType().equals(et2.getDataType())))
        return;

      // compatible type
      if (t1 == ExprType.Type.URI   && t2 == ExprType.Type.CLASS ||
          t1 == ExprType.Type.CLASS && t2 == ExprType.Type.URI)
        return;

      // nope, they won't match
      reportWarning("type mismatch in expression '" + expr.toStringTree() + "': " + et1 +
                    " is not comparable to " + et2);
    }
}


query
    :   #(SELECT #(FROM fclause) #(WHERE wclause) #(PROJ sclause) (oclause)? (lclause)? (tclause)?)
    ;


fclause
    :   #(COMMA fclause fclause)
    |   cls:ID var:ID  { addVar(#var, #cls); }
    ;


sclause
{ ExprType type; }
    :   #(COMMA sclause sclause)
    |   (var:ID)? type=e:pexpr! {
          // check variable exists, or create one
          if (#var != null)
            checkProjVar(#var.getText());
          else
            astFactory.addASTChild(currentAST, #var = nextVar());

          // remember the variable's type
          updateAST(#var, type, null, true);

          // don't forget our expression
          astFactory.addASTChild(currentAST, #e);
        }
    ;

pexpr returns [ExprType type = null]
    :   #(SUBQ query)
    |   type=factor
    ;


wclause
    : (expr)?
    ;

expr
{ ExprType type, type2; }
    : #(AND (expr)+)
    | #(OR  (expr)+)
    | #(ASGN ID type=factor) { vars.put(#ID.getText(), type); updateAST(#ID, type, null, true); }
    | #(EQ type=factor type2=factor) { checkTypeCompatibility(type, type2, #expr); }
    | #(NE type=factor type2=factor) { checkTypeCompatibility(type, type2, #expr); }
    | factor
    ;

factor returns [ExprType type = null]
    : QSTRING ((DHAT t:URIREF) | (AT ID))? {
        type = (#t != null) ? ExprType.literalType(#t.getText()) : ExprType.literalType();
      }
    | URIREF                               { type = ExprType.uriType(); }
    | #(FUNC ID (COLON ID)? (factor)*)
    | #(REF (   v:ID         { updateAST(#v, type = getTypeForVar(#v), null, true); }
              | type=c:cast  { updateAST(#c, type, null, ((OqlAST) #c).isVar()); }
            )
            (   ! ID         { type = resolveField(currentAST, type, #ID); }
              | ! URIREF     { type = handlePredicate(currentAST, type, #URIREF); }
              | #(EXPR pv:ID { addVar(#pv, null); } (expr)?) { type = null; }
            )*
            (STAR)?
      )
    ;

cast returns [ExprType type = null]
    : ! #(CAST f:factor t:ID) {
        type = getTypeForClass(#t, "cast");
        #cast = #f;     // replace CAST node by casted expression
      }
    ;

oclause
    : #(ORDER (oitem)+)
    ;

oitem
    : var:ID (ASC|DESC)? {
        if (!prjs.contains(#var.getText()))
          throw new RecognitionException("Order item '" + #var.getText() + "' is not defined in " +
                                         "the select clause");
        ((OqlAST) #var).setIsVar(true);
      }
    ;

lclause
    : #(LIMIT NUM)
    ;

tclause
    : #(OFFSET NUM)
    ;

