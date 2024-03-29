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

header
{
/*
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import antlr.RecognitionException;
import antlr.collections.AST;
}

/**
 * This generates the iTQL string from a iTQL AST. It assumes the where and select clauses have
 * been converted to constraints.
 *
 * @author Ronald Tschalär
 */
class ItqlWriter extends TreeParser("OqlTreeParser");

options {
    importVocab = Constraints;
}

{
    private static String toItqlStr(AST node) {
      if (((OqlAST) node).isVar())
        return "$" + node.getText();
      else
        return node.getText();
    }

    private static class QueryBuilder {
      final StringBuilder            where    = new StringBuilder();
      final List<Object>             prjTypes = new ArrayList<Object>();
      final List<String>             prjVars  = new ArrayList<String>();
      final List<ProjectionFunction> prjFuncs = new ArrayList<ProjectionFunction>();
      final List<String>             prjExprs = new ArrayList<String>();
      final List<String>             ordrVars = new ArrayList<String>();
      final Set<String>              graphs   = new HashSet<String>();
      String                         limit    = null;
      String                         offset   = null;
      int                            kVar     = 0;

      public QueryInfo toQueryInfo(boolean isSubQuery) throws RecognitionException {
        StringBuilder q = new StringBuilder();
        q.append("select ");

        for (String expr : prjExprs)
          q.append(expr).append(" ");

        if (graphs.size() == 0)
          throw new RecognitionException("no graphs found in query");
        q.append("from <").append(graphs.iterator().next()).append("> ");

        q.append("where ").append(where);

        if (ordrVars.size() > 0) {
          q.append("order by ");
          for (String var : ordrVars)
            q.append('$').append(var).append(" ");
        }

        if (limit != null)
          q.append("limit ").append(limit).append(" ");

        if (offset != null)
          q.append("offset ").append(offset).append(" ");

        q.setLength(q.length() - 1);
        if (!isSubQuery)
          q.append(';');

        return new QueryInfo(q.toString(), prjTypes, prjVars, prjFuncs);
      }
    }
}


query returns [QueryInfo qi = null]
{ QueryBuilder qb = new QueryBuilder(); }
    :   iquery[qb] { qi = qb.toQueryInfo(false); }
    ;

iquery[QueryBuilder qb]
    :   #(SELECT #(FROM fclause[qb]) #(WHERE wclause[qb]) #(PROJ sclause[qb])
          (oclause[qb])? (lclause[qb])? (tclause[qb])?)
    ;


fclause[QueryBuilder qb]
    :   #(COMMA fclause[qb] fclause[qb])
    |   cls:ID var:ID {
          String g = ((OqlAST) #var).getGraph();
          if (g != null)
            qb.graphs.add(g);
        }
    ;


sclause[QueryBuilder qb]
    :   #(COMMA sclause[qb] sclause[qb])
    |   v:ID pexpr[qb, #v]
    ;

pexpr[QueryBuilder qb, AST var]
{
  QueryBuilder sqb = new QueryBuilder();
  qb.prjVars.add(var.getText());
}
    : #(SUBQ iquery[sqb]) {
        QueryInfo sqi = sqb.toQueryInfo(true);
        qb.prjExprs.add("subquery(" + sqi.getQuery() + ")");
        qb.prjTypes.add(sqi);
        qb.prjFuncs.add((ProjectionFunction) ((OqlAST) #SUBQ).getFunction());
      }

    | #(COUNT iquery[sqb]) {
        QueryInfo sqi = sqb.toQueryInfo(true);
        qb.prjExprs.add("count(" + sqi.getQuery() + ")");
        qb.prjTypes.add(String.class);
        qb.prjFuncs.add((ProjectionFunction) ((OqlAST) #COUNT).getFunction());
      }

    | (qs:QSTRING|uri:URIREF) {
        qb.prjExprs.add((#qs != null) ? #qs.getText() : #uri.getText());
        qb.prjTypes.add((#qs != null) ? String.class : URI.class);
        qb.prjFuncs.add((ProjectionFunction) ((OqlAST) #qs).getFunction());
      }

    | v:ID {
        qb.prjExprs.add(toItqlStr(#v));
        qb.prjFuncs.add((ProjectionFunction) ((OqlAST) #v).getFunction());

        ExprType type = ((OqlAST) #v).getExprType();
        if (type == null)
          qb.prjTypes.add(null);
        else {
          switch (type.getType()) {
            case CLASS:
              qb.prjTypes.add(type.getExprClass());
              break;

            case EMB_CLASS:
              qb.prjTypes.add(type.getExprClass());
              break;

            case URI:
              qb.prjTypes.add(URI.class);
              break;

            case TYPED_LIT:
            case UNTYPED_LIT:
              qb.prjTypes.add(String.class);
              break;

            default:
              throw new Error("encountered unknown expression type '" + type.getType() + "'");
          }
        }
      }
    ;


wclause[QueryBuilder qb]
    : expr[qb]
    ;

expr[QueryBuilder qb]
{ StringBuilder e = qb.where; int len = e.length(), leno = e.length(); }
    : #(AND
         (expr[qb] {
           if (e.length() > len) {
             e.append("and ");
             len = e.length();
           }
         })*
       ) {
         if (e.length() > leno)
           e.setLength(e.length() - 4);
       }

    | #(OR { e.append('('); len++; }
         (expr[qb] {
           if (e.length() > len) {
             e.append("or ");
             len = e.length();
           }
         })*
       ) {
        if (e.length() > leno + 1) {
          e.setLength(e.length() - 4);
          e.append(") ");
        } else {
          e.setLength(e.length() - 1);
        }
      }

    | constr[qb]

    ;

constr[QueryBuilder qb]
{ StringBuilder e = qb.where; }
    : #(TRIPLE s:ID p:ID o:ID (g:ID)?) {
        if (#p.getText().equals("<mulgara:equals>"))
          throw new RecognitionException("failed to reduce a <mulgara:equals> - " +
                                         "please try a simpler query");

        e.append(toItqlStr(#s)).append(' ').append(toItqlStr(#p)).append(' ').append(toItqlStr(#o));
        if (#g != null) {
          e.append(" in <").append(#g.getText()).append('>');
          qb.graphs.add(#g.getText());
        }
        e.append(' ');
      }

    | #(MINUS { e.append("(("); } expr[qb] { e.append(") minus ("); } expr[qb]) { e.append(")) "); }

    | #(TRANS { e.append("trans("); } expr[qb] { e.append("and "); } (opt:expr[qb])?) {
        if (#opt == null)
          e.setLength(e.length() - 5);
        e.append(") ");
      }

    | #(WALK { e.append("walk("); } expr[qb] { e.append("and "); } expr[qb]) { e.append(") "); }
    ;


oclause[QueryBuilder qb]
    : #(ORDER (oitem[qb])+)
    ;

oitem[QueryBuilder qb]
    : ID (asc:ASC|desc:DESC)? {
        qb.ordrVars.add(#ID.getText() + (#asc != null ? " asc" : #desc != null ? " desc" : ""));
      }
    ;

lclause[QueryBuilder qb]
    : #(LIMIT NUM) { qb.limit = #NUM.getText(); }
    ;

tclause[QueryBuilder qb]
    : #(OFFSET NUM) { qb.offset = #NUM.getText(); }
    ;

