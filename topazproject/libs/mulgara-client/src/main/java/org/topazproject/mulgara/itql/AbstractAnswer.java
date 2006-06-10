
package org.topazproject.mulgara.itql;

import java.net.URISyntaxException;

import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.GraphException;
import org.jrdf.graph.mem.GraphImpl;

import org.w3c.dom.Element;

/** 
 * This provides some common help in parsing an answer to a set of iTQL commands.
 * 
 * @author Ronald Tschalär
 */
public abstract class AbstractAnswer {
  protected static final String ANSWER     = "answer";
  protected static final String QUERY      = "query";
  protected static final String MESSAGE    = "message";

  protected void parse(Element root, String xml) throws AnswerException {
    try {
      parseAnswer(root, new GraphImpl().getElementFactory());
    } catch (URISyntaxException use) {
      throw new AnswerException("Error parsing response: '" + xml + "'", use);
    } catch (GraphElementFactoryException gefe) {
      throw new AnswerException("Error parsing response: '" + xml + "'", gefe);
    } catch (GraphException ge) {
      throw new AnswerException("Error building answer: '" + xml + "'", ge);
    }
  }

  /**
   * Parse an answer. This has the following structure:
   *
   * <pre>
   * &lt;answer xmlns="http://tucana.org/tql#"&gt;
   *   &lt;query&gt;...&lt;/query&gt;
   *   &lt;query&gt;...&lt;/query&gt;
   *   ...
   * &lt;/answer&gt;
   * </pre>
   */
  protected void parseAnswer(Element ansElem, GraphElementFactory gef)
      throws URISyntaxException, GraphElementFactoryException, AnswerException {
    for (Element q = XmlHelper.getFirstChild(ansElem, QUERY); q != null;
         q = XmlHelper.getNextSibling(q, QUERY)) {
      Element first = XmlHelper.getFirstChild(q, "*");
      if (first != null && first.getTagName().equals(MESSAGE))
        parseMessage(XmlHelper.getText(first));
      else
        parseQueryAnswer(q, gef);
    }
  }

  /**
   * Parse an answer to a single query command. The structure is
   *
   * <pre>
   *   &lt;query&gt;
   *     &lt;variables&gt;...&lt;/variables&gt;
   *     &lt;solution&gt;...&lt;/solution&gt;
   *     &lt;solution&gt;...&lt;/solution&gt;
   *     ...
   *   &lt;/query&gt;
   * </pre>
   */
  protected abstract void parseQueryAnswer(Element query, GraphElementFactory gef)
      throws URISyntaxException, GraphElementFactoryException, AnswerException;

  /**
   * Parse an answer to a single non-query command. The structure is
   *
   * <pre>
   *   &lt;query&gt;
   *     &lt;message&gt;blah blah&lt;/message&gt;
   *   &lt;/query&gt;
   * </pre>
   */
  protected abstract void parseMessage(String msg) throws AnswerException;
}
