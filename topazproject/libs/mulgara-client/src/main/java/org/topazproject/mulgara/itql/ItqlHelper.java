
package org.topazproject.mulgara.itql;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.mulgara.itql.service.ItqlInterpreterBean;
import org.topazproject.mulgara.itql.service.ItqlInterpreterBeanServiceLocator;
import org.topazproject.mulgara.itql.service.ItqlInterpreterException;
import org.topazproject.mulgara.itql.service.QueryException;

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
   * @throws MalformedURLException if service's uri is not a valid URL
   * @throws ServiceException if an error occurred locating the web-service
   * @throws RemoteException if an error occurred talking to the web-service
   */
  public ItqlHelper(ProtectedService service)
      throws MalformedURLException, ServiceException, RemoteException {

    ItqlInterpreterBeanServiceLocator locator = new ItqlInterpreterBeanServiceLocator();
    locator.setMaintainSession(true);

    interpreter = locator.getItqlInterpreterBeanServicePort(new URL(service.getServiceUri()));

    if (service.requiresUserNamePassword()) {
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
    /* Ideally we would send a bunch of iTQL alias commands. However, those are local to an
     * ItqlInterpreter instance, which in turn is bound to a (http) session. The problem is
     * that if there is a long period of inactivity that causes the server to time-out the
     * session, then the ItqlInterpreter instance goes away and a new is created, meaning we
     * loose the aliases. If we could easily detect this case then that wouldn't be a problem,
     * but I know of no way to do so.
     *
     * Hence we do the aliases locally. It's a bit of a hack, since we don't truly parse the
     * iTQL, but it seems to work for most cases.
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
   * @return the answer as an XML string; use one of the Answer classes to parse it
   * @throws ItqlInterpreterException if an exception was encountered while processing the queries
   * @throws RemoteException if an exception occurred talking to the service
   */
  public String doQuery(String itql) throws ItqlInterpreterException, RemoteException {
    itql = unalias(itql);
    if (!itql.endsWith(";"))
      itql += ";";

    if (log.isDebugEnabled())
      log.debug("sending query '" + itql + "'");

    String xml = interpreter.executeQueryToString(itql);

    if (log.isDebugEnabled())
      log.debug("got result '" + xml + "'");

    return xml;
  }

  /** 
   * Run one or more iTQL update commands (or any commands that do not produce output).
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
}
