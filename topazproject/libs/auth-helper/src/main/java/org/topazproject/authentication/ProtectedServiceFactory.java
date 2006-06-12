package org.topazproject.authentication;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.Configuration;

import edu.yale.its.tp.cas.client.CASReceipt;
import edu.yale.its.tp.cas.client.filter.CASFilter;
import edu.yale.its.tp.cas.proxy.ProxyTicketReceptor;

/**
 * A factory class to create ProtectedService instances.
 *
 * @author Pradeep Krishnan
 */
public class ProtectedServiceFactory {
  /**
   * Creates a ProtectedService instance based on configuration.
   * 
   * <p>
   * The expected configuration is:
   * <pre>
   *   uri         = the service uri 
   *   auth-method = CAS or BASIC
   *   userName    = userName for BASIC auth 
   *   password    = password for BASIC auth
   * </pre>
   * </p>
   *
   * @param config The service configuration.
   * @param session HttpSession to retrieve any run-time info (eg. CASReceipt)
   *
   * @return Returns the newly created instance
   *
   * @throws IOException thrown from CAS service creation
   */
  public static ProtectedService createService(Configuration config, HttpSession session)
                                        throws IOException {
    String service = config.getString("uri");
    String auth = config.getString("auth-method");

    if ("CAS".equals(auth))
      return createService(service, getCASReceipt(session));

    return createService(service, config.getString("userName"), config.getString("password"));
  }

  /**
   * Creates a ProtectedService instance with the service/username/password triple.
   *
   * @param service the service URL
   * @param userName the user name or null
   * @param password the password or null
   *
   * @return Returns the newly created instance
   */
  public static ProtectedService createService(final String service, final String userName,
                                               final String password) {
    return new ProtectedService() {
        public String getServiceUri() {
          return service;
        }

        public boolean requiresUserNamePassword() {
          return (userName != null) || (password != null);
        }

        public String getUserName() {
          return userName;
        }

        public String getPassword() {
          return password;
        }
      };
  }

  /**
   * Creates a ProtectedService instance that is protected by CAS Single Signon. The ticket is
   * appended to the service URI if the HTTPSession contains a validated CASReceipt and this
   * service has set up a ProxyTicketReceptor. Otherwise the service url is not modified.
   *
   * @param service the service url
   * @param receipt the CASReceipt corresponding to an authenticated user or null
   *
   * @return the newly created instance
   *
   * @throws IOException when there is an error in contacting CAS server.
   */
  public static ProtectedService createService(String service, CASReceipt receipt)
                                        throws IOException {
    // If no authenticated user, assume an unprotected service
    if (receipt == null)
      return createService(service, null, null);

    // get the PGT-IOU from the CAS receipt. (contained in the validate response from CAS server) 
    String pgtIou = receipt.getPgtIou();

    if (pgtIou == null)
      throw new IOException("No PGT-IOU found. Ensure that a ProxyServlet is configured "
                            + "and is accessible via HTTPS to the CAS server.");

    // use the PGT-IOU to lookup the PGT deposited at our ProxyTicketReceptor servlet
    // and then use the PGT to get a new PT from CAS server.
    String pt = ProxyTicketReceptor.getProxyTicket(pgtIou, service);

    if (pt == null)
      throw new IOException("Failed to get a CAS proxy-ticket.");

    // append the PT to the service URL. 
    // (assumes the URL does not have any other params and does not have any fragments)
    service = service + "?ticket=" + pt;

    return createService(service, null, null);
  }

  /**
   * A utility function to retrieve the CAS receipt from an HTTP Session.
   *
   * @param session the HTTPSession or null
   *
   * @return Returns CASReceipt or null
   */
  public static CASReceipt getCASReceipt(HttpSession session) {
    return (session == null) ? null : (CASReceipt) session.getAttribute(CASFilter.CAS_FILTER_RECEIPT);
  }
}
