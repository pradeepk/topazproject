
package org.topazproject.mulgara.itql;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.mulgara.itql.service.ItqlInterpreterBean;
import org.topazproject.mulgara.itql.service.ItqlInterpreterBeanServiceLocator;
import org.topazproject.mulgara.itql.service.ItqlInterpreterException;
import org.topazproject.mulgara.itql.service.QueryException;

import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.GraphException;
import org.jrdf.graph.mem.GraphImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** 
 * A simple helper for Itql commands. This is a thin wrapper around ItqlInterpreterBean.
 * 
 * @author Ronald Tschalär
 */
public class ItqlHelper {
  private static final Log     log = LogFactory.getLog(ItqlHelper.class);
  private static final HashMap defaultAliases = new HashMap();

  private static final String ANSWER     = "answer";
  private static final String QUERY      = "query";
  private static final String VARS       = "variables";
  private static final String MESSAGE    = "message";
  private static final String SOLUTION   = "solution";
  private static final String RSRC_ATTR  = "resource";
  private static final String BNODE_ATTR = "blank-node";

  private final ItqlInterpreterBean interpreter;
  private       Map                 aliases = new HashMap();

  static {
    defaultAliases.put("rdf",    "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    defaultAliases.put("rdfs",   "http://www.w3.org/2000/01/rdf-schema#");
    defaultAliases.put("owl",    "http://www.w3.org/2002/07/owl#");
    defaultAliases.put("xsd",    "http://www.w3.org/2001/XMLSchema#");
    defaultAliases.put("tucana", "http://tucana.org/tucana#");
    defaultAliases.put("dc",     "http://purl.org/dc/elements/1.1/");
    defaultAliases.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
    defaultAliases.put("fedora", "info:fedora/");
    defaultAliases.put("topaz",  "http://rdf.topazproject.org/RDF#");
  }

  /** 
   * Create a new instance pointed at the given database. The instance is initialized with the
   * default set of aliases.
   * 
   * @param database  the url of the database web-service
   * @throws MalformedURLException if <var>database</var> is not a valid URL
   * @throws ServiceException if an error occurred locating the web-service
   * @throws RemoteException if an error occurred talking to the web-service
   */
  public ItqlHelper(URI database) throws MalformedURLException, ServiceException, RemoteException {
    ItqlInterpreterBeanServiceLocator locator = new ItqlInterpreterBeanServiceLocator();
    locator.setMaintainSession(true);
    interpreter = locator.getItqlInterpreterBeanServicePort(database.toURL());
    interpreter.setServerURI("local:///");
    setAliases(defaultAliases);
  }


  /** 
   * Create a new instance pointed at the given service. The instance is initialized with the
   * default set of aliases.
   * 
   * @param  service  the database web-service
   * 
   * @throws MalformedURLException if <var>database</var> is not a valid URL
   * @throws ServiceException if an error occurred locating the web-service
   * @throws RemoteException if an error occurred talking to the web-service
   */
  public ItqlHelper(ProtectedService service) 
      throws MalformedURLException, ServiceException, RemoteException {
    
    ItqlInterpreterBeanServiceLocator locator = new ItqlInterpreterBeanServiceLocator();
    locator.setMaintainSession(true);
    
    interpreter = locator.getItqlInterpreterBeanServicePort(new URL(service.getServiceUri()));
    
    if (service.requiresUserNamePassword()){
      Stub stub = (Stub)interpreter;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }
    
    interpreter.setServerURI("local:///");
    setAliases(defaultAliases);
  }
  
  /** 
   * Set the current list of aliases.
   * 
   * @param aliases the aliases to use; keys and values must be {@link java.lang.String String}'s
   */
  public void setAliases(Map aliases) {
    /* This doesn't work because the values in the map should be URI's. So instead we handle
     * the aliases locally.
    interpreter.setAliasMap(defaultAliases);
    */
    this.aliases = aliases;
  }

  /** 
   * Get the current list of aliases. 
   * 
   * @return the aliases; this map is "live"
   */
  public Map getAliases() {
    return aliases;
  }

  private String unalias(String itql) {
    // this is not particularly sophisticated, but should catch most stuff
    for (Iterator iter = aliases.keySet().iterator(); iter.hasNext(); ) {
      String alias = (String) iter.next();
      String uri   = (String) aliases.get(alias);
      itql = itql.replaceAll("<" + alias + ":", "<" + uri)
                 .replaceAll("\\^\\^" + alias + ":(\\S+)", "^^<" + uri + "$1>");
    }

    return itql;
  }

  /** 
   * Run one or more iTQL queries. 
   * 
   * @param itql the iTQL query/queries to run
   * @return the answer
   * @throws ItqlInterpreterException if an exception was encountered while processing the queries
   * @throws RemoteException if an exception occurred talking to the service
   * @throws AnswerException if an exception occurred parsing the query response
   */
  public Answer doQuery(String itql)
      throws ItqlInterpreterException, RemoteException, AnswerException {
    itql = unalias(itql);
    if (!itql.endsWith(";"))
      itql += ";";

    if (log.isDebugEnabled())
      log.debug("sending query '" + itql + "'");

    String xml = interpreter.executeQueryToString(itql);

    if (log.isDebugEnabled())
      log.debug("got result '" + xml + "'");

    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setIgnoringComments(true);
    builderFactory.setCoalescing(true);

    DocumentBuilder builder;
    try {
      builder = builderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException pce) {
      throw new RuntimeException(pce);  // can this happen?
    }

    Document doc;
    try {
      doc = builder.parse(new InputSource(new StringReader(xml)));
    } catch (IOException ioe) {
      throw new Error(ioe);     // can't happen
    } catch (SAXException se) {
      throw new AnswerException("Unexpected response: '" + xml + "'", se);
    }

    Element root = doc.getDocumentElement();
    if (!root.getTagName().equals(ANSWER))
      throw new AnswerException("Unexpected response: '" + xml + "'");

    try {
      return parseAnswer(root, new GraphImpl().getElementFactory());
    } catch (URISyntaxException use) {
      throw new AnswerException("Error parsing response: '" + xml + "'", use);
    } catch (GraphElementFactoryException gefe) {
      throw new AnswerException("Error parsing response: '" + xml + "'", gefe);
    } catch (GraphException ge) {
      throw new AnswerException("Error building answer: '" + xml + "'", ge);
    }
  }

  /** 
   * Run an iTQL update command (or any command that does not produce output).
   * 
   * @param itql the iTQL statement(s) to execute
   * @throws ItqlInterpreterException if an exception was encountered while processing the queries
   * @throws RemoteException if an exception occurred talking to the service
   */
  public void doUpdate(String itql) throws ItqlInterpreterException, RemoteException {
    itql = unalias(itql);
    if (!itql.endsWith(";"))
      itql += ";";

    if (log.isDebugEnabled())
      log.debug("sending update '" + itql + "'");

    interpreter.executeUpdate(itql);
  }

  /** 
   * Begin a transaction. If not invoked, iTQL commands will run in auto-commit mode.
   * One of {@link #commitTxn commitTxn()} or {@link #rollbackTxn rollbackTxn()} must be
   * invoked to end the transaction.
   * 
   * @param txnName  a name to associate with the transaction; used for logging only
   * @throws QueryException if an exception occurred starting the transaction
   * @throws RemoteException if an exception occurred talking to the service
   */
  public void beginTxn(String txnName) throws QueryException, RemoteException {
    if (log.isDebugEnabled())
      log.debug("sending beginTransaction '" + txnName + "'");

    interpreter.beginTransaction(txnName);
  }

  /** 
   * Commit a transaction. May only be invoked after a {@link #beginTxn beginTxn()}.
   * 
   * @param txnName  a name to associate with the transaction; used for logging only
   * @throws QueryException if an exception occurred starting the transaction
   * @throws RemoteException if an exception occurred talking to the service
   */
  public void commitTxn(String txnName) throws QueryException, RemoteException {
    if (log.isDebugEnabled())
      log.debug("sending commit '" + txnName + "'");

    interpreter.commit(txnName);
  }

  /** 
   * Roll back a transaction. May only be invoked after a {@link #beginTxn beginTxn()}.
   * 
   * @param txnName  a name to associate with the transaction; used for logging only
   * @throws QueryException if an exception occurred starting the transaction
   * @throws RemoteException if an exception occurred talking to the service
   */
  public void rollbackTxn(String txnName) throws QueryException, RemoteException {
    if (log.isDebugEnabled())
      log.debug("sending rollback '" + txnName + "'");

    interpreter.rollback(txnName);
  }

  /**
   * <answer xmlns="http://tucana.org/tql#">
   *   <query>
   *     <variables>
   *       <s/>
   *       <p/>
   *       <o/>
   *     </variables>
   *     <solution>
   *       <s blank-node="..."/>
   *       <p resource="http://purl.org/dc/elements/1.1/subject"/>
   *       <o>PlosArticle</o>
   *     </solution>
   *     ...
   *   </query>
   * </answer>
   *
   * or
   *
   * <answer xmlns="http://tucana.org/tql#">
   *   <query>
   *     <message>blah blah</message>
   *   </query>
   * </answer>
   */
  private static Answer parseAnswer(Element ansElem, GraphElementFactory gef)
      throws URISyntaxException, GraphElementFactoryException {
    Answer ans = new Answer();

    NodeList queries = getChildren(ansElem, QUERY);
    for (int idx = 0; idx < queries.getLength(); idx++) {
      Element query = (Element) queries.item(idx);
      ans.add(parseQueryAnswer(query, gef));
    }

    return ans;
  }

  private static Object parseQueryAnswer(Element query, GraphElementFactory gef)
      throws URISyntaxException, GraphElementFactoryException {
    Element varsElem = getChild(query, VARS);
    if (varsElem == null) {
      // see if this is a warning
      Element msgElem = getChild(query, MESSAGE);
      if (msgElem != null)
        return getText(msgElem);

      throw new IllegalArgumentException("could not parse query element - no variables nor " +
                                         "message element found");
    }

    NodeList varElems = getChildren(varsElem, "*");
    String[] vars = new String[varElems.getLength()];
    for (int idx2 = 0; idx2 < varElems.getLength(); idx2++)
      vars[idx2] = varElems.item(idx2).getNodeName();

    QueryAnswer q_ans = new QueryAnswer(vars);

    NodeList solutionElems = getChildren(query, SOLUTION);
    for (int idx2 = 0; idx2 < solutionElems.getLength(); idx2++) {
      Element sol = (Element) solutionElems.item(idx2);
      Object[] row = new Object[vars.length];

      for (int idx3 = 0; idx3 < row.length; idx3++)
        row[idx3] = parseVariable(getChild(sol, vars[idx3]), gef);

      q_ans.addRow(row);
    }

    return q_ans;
  }

  private static Object parseVariable(Element v, GraphElementFactory gef)
      throws URISyntaxException, GraphElementFactoryException {
    if (v == null)
      return null;

    String res = v.getAttribute(RSRC_ATTR);
    if (res.length() > 0)
      return gef.createResource(new URI(res));

    if (v.hasAttribute(BNODE_ATTR))
      return gef.createResource();

    if (v.getFirstChild() instanceof Element)
      return parseQueryAnswer(v, gef);

    return gef.createLiteral(getText(v));
  }

  private static NodeList getChildren(Element parent, String childName) {
    final NodeList children = parent.getChildNodes();
    final String   filter   = (childName != null && !childName.equals("*")) ? childName : null;

    return new NodeList() {
      private List elems;

      {
        elems = new ArrayList();
        for (int idx = 0; idx < children.getLength(); idx++) {
          Node n = children.item(idx);
          if (n.getNodeType() == Node.ELEMENT_NODE &&
              (filter == null || n.getNodeName().equals(filter)))
            elems.add(n);
        }
      }

      public Node item(int index) {
        return (Node) elems.get(index);
      }

      public int getLength() {
        return elems.size();
      }
    };
  }

  private static Element getChild(Element parent, String name) {
    NodeList children = getChildren(parent, name);
    if (children.getLength() == 0)
      return null;
    if (children.getLength() > 1)
      log.warn("Expected exactly one child named '" + name + "' of '" + parent.getTagName() +
               "' but got " + children.getLength());
    return (Element) children.item(0);
  }

  private static String getText(Node node) throws IllegalArgumentException {
    Node text = node.getFirstChild();
    if (text == null)
      return "";
    if (!(text instanceof Text))
      throw new IllegalArgumentException("Expected text, but found node '" +
                                         text.getNodeName() + "'");
    return ((Text) text).getData();
  }

  /**
   * This represents a list of answers to a list of queries. Each answer in the list is either a
   * {@link QueryAnswer QueryAnswer} or a {@link java.lang.String String}; the latter indicates
   * some warning message.
   */
  public static class Answer {
    private final List answers;

    Answer() {
      answers = new ArrayList();
    }

    void add(Object ans) {
      answers.add(ans);
    }

    /** 
     * The returned list of answers. Each element is either a {@link QueryAnswer QueryAnswer} or a
     * {@link java.lang.String String}. The list may be empty if no answers were returned.
     * 
     * @return the answers
     */
    public List getAnswers() {
      return answers;
    }
  }

  /**
   * This represents an answer to a single iTQL query. It consists of a number of variables (the
   * "columns" in each row) and a number of "rows" (query matches).
   */
  public static class QueryAnswer {
    private final String[] variables;
    private final List     rows;

    QueryAnswer(String[] vars) {
      variables = vars;
      rows = new ArrayList();
    }

    void addRow(Object[] row) {
      rows.add(row);
    }

    /** 
     * Get the list of variables in the answer. 
     * 
     * @return the list of variables
     */
    public String[] getVariables() {
      return variables;
    }

    /** 
     * Get the rows. Each row consists an array of {@link java.lang.Object Object}'s, where each
     * element is the value of the corresponding variable in the variables list. Each element in the
     * list can be one of: null, a {@link org.jrdf.graph.Literal Literal}, a {@link
     * org.jrdf.graph.URIReference URIReference}, a {@link org.jrdf.graph.BlankNode BlankNode}, or
     * a {@link QueryAnswer QueryAnswer} (in case of subqueries).
     * 
     * @return the list of rows
     */
    public List getRows() {
      return rows;
    }

    /** 
     * Return in the index of the given variable. 
     * 
     * @param var the variable
     * @return the index, or -1 if <var>var</var> is not a variable in this answer
     */
    public int indexOf(String var) {
      for (int idx = 0; idx < variables.length; idx++) {
        if (variables[idx].equals(var))
          return idx;
      }

      return -1;
    }

    /** 
     * Helper method to retrieve value for the given variable.
     * 
     * @param row the row index
     * @param var the variable's name
     * @return the variable's value in the row
     */
    public Object getVar(int row, String var) {
      return ((Object[]) rows.get(row))[indexOf(var)];
    }
  }

  /**
   * Represents a problem parsing the response from the query or building the answer.
   */
  public static class AnswerException extends Exception {
    /** 
     * Create a new instance with the given error message. 
     * 
     * @param msg the error message
     */
    public AnswerException(String msg) {
      super(msg);
    }

    /** 
     * Create a new instance with the given error message and underlying exception. 
     * 
     * @param msg   the error message
     * @param cause the cause for this exception
     */
    public AnswerException(String msg, Throwable cause) {
      super(msg, cause);
    }
  }
}
