/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.itql;

import java.lang.ref.WeakReference;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.text.SimpleDateFormat;

import java.rmi.RemoteException;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.reauth.AbstractReAuthStubFactory;
import org.topazproject.mulgara.itql.service.ItqlInterpreterBean;
import org.topazproject.mulgara.itql.service.ItqlInterpreterBeanServiceLocator;
import org.topazproject.mulgara.itql.service.ItqlInterpreterException;
import org.topazproject.mulgara.itql.service.QueryException;

/** 
 * A simple helper for Itql commands. This is a thin wrapper around ItqlInterpreterBean.
 * <p>
 * (The org.topazproject.mulgara.itql.service classes are generated by wsdl2java after
 * java2wsdl is run on the
 * {@link org.kowari.itql.ItqlInterpreterBean org.kowari.itql.ItqlInterpreterBean} class.)
 *
 * @author Ronald Tschalär
 * @see org.kowari.itql.ItqlInterpreterBean
 */
public class ItqlHelper {
  private static final Log     log = LogFactory.getLog(ItqlHelper.class);
  private static final HashMap defaultAliases = new HashMap();
  private static final List    instanceList   = new LinkedList();

  private static final String ANSWER     = "answer";
  private static final String QUERY      = "query";
  private static final String VARS       = "variables";
  private static final String MESSAGE    = "message";
  private static final String SOLUTION   = "solution";
  private static final String RSRC_ATTR  = "resource";
  private static final String BNODE_ATTR = "blank-node";

  /** The base URI for rdf defined URIs: {@value} */
  public static final String RDF_URI      = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  /** The base URI for rdf-schema defined URIs: {@value} */
  public static final String RDFS_URI     = "http://www.w3.org/2000/01/rdf-schema#";
  /** The base URI for owl defined URIs: {@value} */
  public static final String OWL_URI      = "http://www.w3.org/2002/07/owl#";
  /** The base URI for xml-schema defined URIs: {@value} */
  public static final String XSD_URI      = "http://www.w3.org/2001/XMLSchema#";
  /** The base URI for tucana defined URIs: {@value} */
  public static final String TUCANA_URI   = "http://tucana.org/tucana#";
  /** The base URI for dublin-core defined URIs: {@value} */
  public static final String DC_URI       = "http://purl.org/dc/elements/1.1/";
  /** The base URI for dublin-core terms URIs: {@value} */
  public static final String DC_TERMS_URI = "http://purl.org/dc/terms/";
  /** The base URI for oai-dublin-core defined URIs: {@value} */
  public static final String OAI_DC_URI   = "http://www.openarchives.org/OAI/2.0/oai_dc/";
  /** The base URI for fedora defined URIs: {@value} */
  public static final String FEDORA_URI   = "info:fedora/";
  /** The base URI for topaz defined URIs: {@value} */
  public static final String TOPAZ_URI    = "http://rdf.topazproject.org/RDF/";

  private final ItqlInterpreterBean interpreter;
  private       BeanReference       cleanupRef;
  private       boolean             inTransaction = false;
  private       Map                 aliases = new HashMap();

  static {
    defaultAliases.put("rdf",      RDF_URI);
    defaultAliases.put("rdfs",     RDFS_URI);
    defaultAliases.put("owl",      OWL_URI);
    defaultAliases.put("xsd",      XSD_URI);
    defaultAliases.put("tucana",   TUCANA_URI);
    defaultAliases.put("dc",       DC_URI);
    defaultAliases.put("dc_terms", DC_TERMS_URI);
    defaultAliases.put("oai_dc",   OAI_DC_URI);
    defaultAliases.put("fedora",   FEDORA_URI);
    defaultAliases.put("topaz",    TOPAZ_URI);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        for (Iterator iter = instanceList.iterator(); iter.hasNext(); ) {
          BeanReference ref = (BeanReference) iter.next();
          try {
            ref.interpreter.close();
          } catch (Throwable t) {
          }
        }
      }
    });
  }

  /** 
   * This registers the ItqlHelper instance so we can close the underlying ItqlInterpreterBean
   * session when it is gc'd or when the app exits.
   */
  private static void registerInstance(ItqlHelper inst) {
    synchronized (instanceList) {
      // first some housekeeping: clean up gc'd instances
      for (Iterator iter = instanceList.iterator(); iter.hasNext(); ) {
        BeanReference ref = (BeanReference) iter.next();
        if (ref.get() == null) {
          iter.remove();
          try {
            ref.interpreter.close();
          } catch (Throwable t) {
            if (log.isDebugEnabled())
              log.debug("Error closing interpreter instance", t);
          }
          ref.interpreter = null;
        }
      }

      // register it
      instanceList.add(inst.cleanupRef = new BeanReference(inst));
    }
  }

  /** 
   * This keeps a reference to the underlying ItqlInterpreterBean instance.
   */
  private static class BeanReference extends WeakReference {
    ItqlInterpreterBean interpreter;

    BeanReference(ItqlHelper helper) {
      super(helper);
      interpreter = helper.interpreter;
    }
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

    init();
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

    ItqlInterpreterBean stub = createStub(service);

    if (service.hasRenewableCredentials()) {
      AbstractReAuthStubFactory factory = new AbstractReAuthStubFactory() {

        public Object newStub(ProtectedService service) throws Exception {
          return initServer(createStub(service));
        }

        public Object rebuildStub(Object old, ProtectedService service, Throwable fault)
            throws Exception {

          if (inTransaction)
            throw new Exception("Cannot rebuild a stub in middle of a transaction");
          return super.rebuildStub(old, service, fault);
        }
      };

      stub = (ItqlInterpreterBean)factory.newProxyStub(stub, service);
    }

    interpreter = stub;

    init();

  }

  private static ItqlInterpreterBean createStub(ProtectedService service)
      throws MalformedURLException, ServiceException, RemoteException {

    ItqlInterpreterBeanServiceLocator locator = new ItqlInterpreterBeanServiceLocator();
    locator.setMaintainSession(true);

    ItqlInterpreterBean interpreter;
    interpreter = locator.getItqlInterpreterBeanServicePort(new URL(service.getServiceUri()));

    if (service.requiresUserNamePassword()) {
      Stub stub = (Stub)interpreter;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }

    return interpreter;
  }

  private void init() throws RemoteException {
    registerInstance(this);
    setAliases(defaultAliases);
    initServer(interpreter);
  }

  private static ItqlInterpreterBean initServer(ItqlInterpreterBean interpreter)
      throws RemoteException {
    /* There is a bug in ItqlInterpreter where it starts off assuming the connection is not
     * local. Combined with the fact that no rmi server is running this leads to it failing
     * to connect to the db (only if local does it fall back to using direct connection if
     * no rmi server is available). Therefore we first set the server-uri to a URI which
     * forces ItqlInterpreter to local-mode.
     *
     * Now, this triggers another problem: if we start a transaction and do some operation,
     * then the tx-begin (set autocommit off) will be done on the local:/// session, but the
     * operation will then switch the db session to rmi://localhost/fedora (assuming that's
     * the base for the model specified in the operation). This leads to various problems
     * including a deadlock. Hence we immediately reset the server-uri here to the correct
     * location.
     */
    interpreter.setServerURI("local:///");
    interpreter.setServerURI("rmi://localhost/fedora");

    return interpreter;
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
    if (!itql.trim().endsWith(";"))
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
    if (!itql.trim().endsWith(";"))
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
    inTransaction = true;
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
    inTransaction = false;
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
    inTransaction = false;
  }

  /** 
   * Close the session. 
   * 
   * @throws RemoteException if an exception occurred talking to the service
   */
  public void close() throws RemoteException {
    boolean wasActive;
    synchronized (instanceList) {
      wasActive = instanceList.remove(cleanupRef);
    }

    if (wasActive)
      interpreter.close();
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
   * Bind values to an itql fmt string containing ${xxx} placeholders. This is convenience
   * method for {@link bindValues(java.lang.String, java.util.Map) bindValues} when there's
   * only one variable.
   *
   * @param fmt   the ITQL fmt string
   * @param var   the name of the variable to substitute. name appears in the fmt string as ${name}
   * @param value the value to substite for the variable.
   * @return Returns an ITQL query string with all local values bound
   * @throws IllegalArgumentException if a value is missing for a ${token}
   */
  public static String bindValues(String fmt, String var, String value) {
    Map values = new HashMap();
    values.put(var, value);
    return bindValues(fmt, values);
  }

  /**
   * Bind values to an itql fmt string containing ${xxx} placeholders. This is convenience
   * method for {@link bindValues(java.lang.String, java.util.Map) bindValues} when there
   * are only two variables.
   *
   * @param fmt    the ITQL fmt string
   * @param var1   the name of the first variable to substitute. name appears in the fmt string as
   *               ${name}
   * @param value1 the value to substite for the first variable.
   * @param var2   the name of the second variable to substitute.
   * @param value2 the value to substite for the second variable.
   * @return Returns an ITQL query string with all local values bound
   * @throws IllegalArgumentException if a value is missing for a ${token}
   */
  public static String bindValues(String fmt, String var1, String value1, String var2,
                                  String value2) {
    Map values = new HashMap();
    values.put(var1, value1);
    values.put(var2, value2);
    return bindValues(fmt, values);
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
   * Inserts escapes in a literal so that it is suitable for binding to an ITQL statement.
   * Literals are bracketted with single-quotes in ITQL. This function  inserts escapes into 
   * a literal so that it is now suitable for binding. 
   * 
   * @param val the literal value that is to be escaped
   *
   * @return Returns the escaped literal suitable for use in an ITQL statement
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
