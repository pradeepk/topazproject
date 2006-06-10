
package org.topazproject.mulgara.itql;

import java.net.URISyntaxException;

import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphElementFactoryException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** 
 * This provides some common help in parsing an answer to a set of iTQL commands.
 * 
 * @author Ronald Tschalär
 */
public abstract class AbstractQueryAnswer {
  protected static final String VARS       = "variables";
  protected static final String SOLUTION   = "solution";
  protected static final String RSRC_ATTR  = "resource";
  protected static final String BNODE_ATTR = "blank-node";

  protected String[] variables;

  protected void parseQueryAnswer(Element query, GraphElementFactory gef)
      throws URISyntaxException, GraphElementFactoryException, AnswerException {
    Element varsElem = XmlHelper.getFirstChild(query, VARS);
    if (varsElem == null)
      throw new IllegalArgumentException("could not parse query element - no variables found");

    NodeList varElems = XmlHelper.getChildren(varsElem, "*");
    String[] vars = new String[varElems.getLength()];
    for (int idx2 = 0; idx2 < varElems.getLength(); idx2++)
      vars[idx2] = varElems.item(idx2).getNodeName();

    setVariables(vars);

    for (Element sol = XmlHelper.getFirstChild(query, SOLUTION); sol != null;
         sol = XmlHelper.getNextSibling(sol, SOLUTION)) {
      parseRow(sol, gef);
    }
  }

  protected void setVariables(String[] vars) throws AnswerException {
    variables = vars;
  }

  protected void parseRow(Element solution, GraphElementFactory gef)
      throws URISyntaxException, GraphElementFactoryException, AnswerException {
    Object[] row = new Object[variables.length];

    for (int idx3 = 0; idx3 < row.length; idx3++)
      row[idx3] = parseVariable(XmlHelper.getOnlyChild(solution, variables[idx3], null), gef);

    addRow(row);
  }

  protected abstract Object parseVariable(Element v, GraphElementFactory gef)
      throws URISyntaxException, GraphElementFactoryException, AnswerException;

  protected abstract void addRow(Object[] row) throws AnswerException;
}
