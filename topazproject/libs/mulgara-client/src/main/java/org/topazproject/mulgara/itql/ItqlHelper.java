
package org.topazproject.mulgara.itql;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.text.SimpleDateFormat;

import java.rmi.RemoteException;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  /** The base URI for rdf defined URIs: {@value} */
  public static final String RDF_URI    = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  /** The base URI for rdf-schema defined URIs: {@value} */
  public static final String RDFS_URI   = "http://www.w3.org/2000/01/rdf-schema#";
  /** The base URI for owl defined URIs: {@value} */
  public static final String OWL_URI    = "http://www.w3.org/2002/07/owl#";
  /** The base URI for xml-schema defined URIs: {@value} */
  public static final String XSD_URI    = "http://www.w3.org/2001/XMLSchema#";
  /** The base URI for tucana defined URIs: {@value} */
  public static final String TUCANA_URI = "http://tucana.org/tucana#";
  /** The base URI for dublin-core defined URIs: {@value} */
  public static final String DC_URI     = "http://purl.org/dc/elements/1.1/";
  /** The base URI for oai-dublin-core defined URIs: {@value} */
  public static final String OAI_DC_URI = "http://www.openarchives.org/OAI/2.0/oai_dc/";
  /** The base URI for fedora defined URIs: {@value} */
  public static final String FEDORA_URI = "info:fedora/";
  /** The base URI for topaz defined URIs: {@value} */
  public static final String TOPAZ_URI  = "http://rdf.topazproject.org/RDF/";

  private final ItqlInterpreterBean interpreter;
  private       Map                 aliases = new HashMap();

  static {
    defaultAliases.put("rdf",    RDF_URI);
    defaultAliases.put("rdfs",   RDFS_URI);
    defaultAliases.put("owl",    OWL_URI);
    defaultAliases.put("xsd",    XSD_URI);
    defaultAliases.put("tucana", TUCANA_URI);
    defaultAliases.put("dc",     DC_URI);
    defaultAliases.put("oai_dc", OAI_DC_URI);
    defaultAliases.put("fedora", FEDORA_URI);
    defaultAliases.put("topaz",  TOPAZ_URI);
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

  /**
   * Get a copy of the default aliases.
   *
   * @return the default aliases
   */
  public static Map getDefaultAliases() {
    return new HashMap(defaultAliases);
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

  /**
   * Bind values to an itql fmt string containing ${xxx} placeholders.
   *
   * @param fmt the ITQL fmt string
   * @param values the name value pair for substitusion. name appears  in the fmt string as ${name}
   *        and the value is its replacement
   *
   * @return Returns an ITQL query string with all local values bound
   *
   * @throws IllegalArgumentException if a value is missing for a ${token}
   */
  public static String bindValues(String fmt, Map values) {
    Pattern      p   = Pattern.compile("\\$\\{(\\w*)\\}");
    Matcher      m   = p.matcher(fmt);
    StringBuffer sb  = new StringBuffer(fmt.length() * 2);
    int          pos = 0;

    while (m.find()) {
      int    ts    = m.start();
      int    te    = m.end();
      String token = fmt.substring(ts + 2, te - 1);
      String val   = (String) values.get(token);

      if (val == null)
        throw new IllegalArgumentException("Missing value for ${'" + token + "}");

      sb.append(fmt.substring(pos, ts));
      sb.append(val);
      pos = te;
    }

    sb.append(fmt.substring(pos));

    return sb.toString();
  }

  /**
   * Does input valdation for uri parameters. Only absolute (non-relative) URIs are valid.
   *
   * @param uri the uri string to validate
   * @param name the name of this uri for use in error messages
   *
   * @return Returns the uri
   *
   * @throws NullPointerException if the uri string is null
   * @throws IllegalArgumentException if the uri is not a valid absolure URI
   */
  public static URI validateUri(String uri, String name) {
    if (uri == null)
      throw new NullPointerException("'" + name + "' cannot be null");

    try {
      URI u = new URI(uri);

      if (!u.isAbsolute())
        throw new URISyntaxException(uri, "missing scheme component", 0);

      return u;
    } catch (URISyntaxException e) {
      IllegalArgumentException iae =
        new IllegalArgumentException("'" + name + "' must be a valid absolute URI");
      iae.initCause(e);
      throw iae;
    }
  }

  /**
   * Escape a literal before binding to an ITQL statement.
   *
   * @param val the literal value that is to be escaped
   *
   * @return Returns the escaped literal
   */
  public static String escapeLiteral(String val) {
    return val.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'");
  }

  /**
   * Get the current UTC time formatted as an xsd:dateTime string. (eg. '2006-07-24T17:54:42Z')
   *
   * @return Returns a string usable as a literal in rdf.
   */
  public static String getUTCTime() {
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    fmt.setTimeZone(new SimpleTimeZone(0, "UTC"));
    return fmt.format(new Date());
  }
}
