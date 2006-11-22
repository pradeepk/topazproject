/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search.service;

import org.topazproject.fedoragsearch.service.FgsOperations;
import org.topazproject.fedoragsearch.service.FgsOperationsServiceLocator;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * Wrapper over annotation(not the same as reply) web service
 */
public class SearchWebService {
  private URL uri;
  private FgsOperations fgsOperations;

  /**
   * @see org.plos.service.BaseConfigurableService#init()
   */
  public void init() throws IOException, URISyntaxException, ServiceException {
    fgsOperations = new FgsOperationsServiceLocator().getOperations(uri);
  }

  /**
   * Set the uri for the web service
   * @param uri uri
   */
  public void setUri(final URL uri) {
    this.uri = uri;
  }

  /**
   * @see org.topazproject.fedoragsearch.service.FgsOperations#gfindObjects(String, long, int, int, int, String, String)
   */
  public String find(final String query, final int startPage, final int pageSize, final int snippetsMax, final int fieldMaxLength, final String indexName, final String resultPageXslt) throws RemoteException {
    return fgsOperations.gfindObjects(query, startPage, pageSize, snippetsMax, fieldMaxLength, indexName, resultPageXslt);
  }
}
