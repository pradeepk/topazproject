/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.authentication;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.cas.client.CASReceipt;
import edu.yale.its.tp.cas.client.filter.CASFilter;
import edu.yale.its.tp.cas.proxy.ProxyTicketReceptor;

/**
 * A service class that holds information to connect to a service protected by CAS authentication.
 *
 * @author Pradeep Krishnan
 */
public class CASProtectedService implements ProtectedService {
  private static Log log         = LogFactory.getLog(CASProtectedService.class);
  private URI        originalUri;
  private String     modifiedUri;
  private CASReceipt receipt;

  /**
   * Creates a ProtectedService instance that is protected by CAS Single Signon. The ticket is
   * appended to the service URI if the HTTPSession contains a validated CASReceipt and this
   * service has set up a ProxyTicketReceptor.
   *
   * @param uri the service uri
   * @param session the CASReceipt corresponding to an authenticated user or null
   *
   * @throws IOException when there is an error in contacting CAS server.
   * @throws URISyntaxException If the service uri is invalid
   */
  public CASProtectedService(String uri, HttpSession session)
                      throws IOException, URISyntaxException {
    this(uri, getCASReceipt(session));
  }

  /**
   * Creates a ProtectedService instance that is protected by CAS Single Signon. The ticket is
   * appended to the service URI if the HTTPSession contains a validated CASReceipt and this
   * service has set up a ProxyTicketReceptor.
   *
   * @param uri the service uri
   * @param sessionMap the map containing the CASReceipt and any other info corresponding to an
   *        authenticated user or null
   *
   * @throws IOException when there is an error in contacting CAS server.
   * @throws URISyntaxException If the service uri is invalid
   */
  public CASProtectedService(String uri, final Map sessionMap)
                      throws IOException, URISyntaxException {
    this(uri, getCASReceipt(sessionMap));
  }

  /**
   * Creates a ProtectedService instance that is protected by CAS Single Signon. The ticket is
   * appended to the service URI if the HTTPSession contains a validated CASReceipt and this
   * service has set up a ProxyTicketReceptor.
   *
   * @param uri the service uri
   * @param receipt the CASReceipt corresponding to an authenticated user or null
   *
   * @throws IOException when there is an error in contacting CAS server.
   * @throws URISyntaxException If the service uri is invalid
   */
  public CASProtectedService(String uri, CASReceipt receipt)
                      throws IOException, URISyntaxException {
    this.receipt       = receipt;
    this.originalUri   = new URI(uri);

    // If no authenticated user, assume an unprotected service
    if (receipt == null)
      modifiedUri = uri;
    else
      buildServiceUri();
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#getServiceUri
   */
  public String getServiceUri() {
    return modifiedUri;
  }

  /**
   * Returns false always.
   *
   * @return Returns false
   */
  public boolean requiresUserNamePassword() {
    return false;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#getUserName
   */
  public String getUserName() {
    return null;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#getUserName
   */
  public String getPassword() {
    return null;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#hasRenewableCredentials
   */
  public boolean hasRenewableCredentials() {
    // If no authenticated user, credentials are not renewable
    return receipt != null;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#renew
   */
  public boolean renew() {
    try {
      return (receipt == null) ? false : buildServiceUri();
    } catch (IOException e) {
      log.warn("Failed to acquire CAS proxy ticket for " + originalUri, e);

      return false;
    }
  }

  private boolean buildServiceUri() throws IOException {
    String pt    = getCASTicket();
    URI    uri   = originalUri;
    String query = uri.getQuery();

    if ((query == null) || (query.length() == 0))
      query = "ticket=" + pt;
    else
      query += ("&ticket=" + pt);

    try {
      uri =
        new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
                query, uri.getFragment());
    } catch (URISyntaxException e) {
      throw new Error(e); // shouldn't happen
    }

    modifiedUri = uri.toString();

    if (log.isInfoEnabled())
      log.info("Acquired CAS proxy ticket for " + modifiedUri);

    return true;
  }

  private String getCASTicket() throws IOException {
    // get the PGT-IOU from the CAS receipt. (contained in the validate response from CAS server) 
    String pgtIou = receipt.getPgtIou();

    if (pgtIou == null)
      throw new IOException("No PGT-IOU found. Ensure that a ProxyServlet is configured "
                            + "and is accessible via HTTPS to the CAS server.");

    // use the PGT-IOU to lookup the PGT deposited at our ProxyTicketReceptor servlet
    // and then use the PGT to get a new PT from CAS server.
    return ProxyTicketReceptor.getProxyTicket(pgtIou, originalUri.toString());
  }

  private static CASReceipt getCASReceipt(HttpSession session) {
    return (session == null) ? null : (CASReceipt) session.getAttribute(CASFilter.CAS_FILTER_RECEIPT);
  }

  private static CASReceipt getCASReceipt(final Map sessionMap) {
    return (sessionMap == null) ? null : (CASReceipt) sessionMap.get(CASFilter.CAS_FILTER_RECEIPT);
  }
}
