/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.search;

import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.ws.ImplInvocationHandler;
import org.topazproject.common.ws.WSTopazContext;
import org.topazproject.xacml.ws.WSXacmlUtil;
import org.topazproject.fedoragsearch.topazlucene.SearchContext;
import org.topazproject.xacml.AbstractSimplePEP;
import org.topazproject.ws.article.Article;

import dk.defxws.fedoragsearch.server.Operations; // API
import dk.defxws.fedoragsearch.server.GenericOperationsImpl;
import dk.defxws.fedoragsearch.server.Config;


public class FedoraGenericSearchServicePortSoapBindingImpl implements Operations, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(FedoraGenericSearchServicePortSoapBindingImpl.class);

  private TopazContext ctx = new WSTopazContext(getClass().getName());
  private WSSearchPEP  pep = null;
  private Operations   impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      pep = new WSSearchPEP((ServletEndpointContext) context);

      ctx.init(context); // ctx contains itql, apim, user, uploader, apia, ...

      // create the impl
      GenericOperationsImpl ops = new GenericOperationsImpl(); // normally ctx and pep are passed in
      ops.init(Config.getCurrentConfig());
      impl = (Operations)ImplInvocationHandler.newProxy(ops, ctx, log);
    } catch (Exception e) {
      log.error("Failed to initialize SearchImpl", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    ctx.destroy();
    impl = null;
  }

  /**
   * @see dk.defxws.fedoragsearch.server.Operations#updateIndex
   */
  public String updateIndex(String action, String value, String repositoryName, String indexName,
                            String indexDocXslt, String resultPageXslt) throws RemoteException {
    setupContext();
    return impl.updateIndex(action, value, repositoryName, indexName, indexDocXslt, resultPageXslt);
  }

  /**
   * @see dk.defxws.fedoragsearch.server.Operations#gfindObjects
   */
  public String gfindObjects(String query, long hitPageStart, int hitPageSize, int snippetsMax,
                             int fieldMaxLength, String indexName, String resultPageXslt)
      throws RemoteException {
    setupContext();
    return impl.gfindObjects(query, hitPageStart, hitPageSize, snippetsMax, fieldMaxLength,
                             indexName, resultPageXslt);
  }

  /**
   * @see dk.defxws.fedoragsearch.server.Operations#browseIndex
   */
  public String browseIndex(String startTerm, int termPageSize, String fieldName, String indexName,
                            String resultPageXslt) throws RemoteException {
    setupContext();
    return impl.browseIndex(startTerm, termPageSize, fieldName, indexName, resultPageXslt);
  }

  /**
   * @see dk.defxws.fedoragsearch.server.Operations#getRepositoryInfo
   */
  public String getRepositoryInfo(String repositoryName, String resultPageXslt)
      throws RemoteException {
    setupContext();
    return impl.getRepositoryInfo(repositoryName, resultPageXslt);
  }

  /**
   * @see dk.defxws.fedoragsearch.server.Operations#getIndexInfo
   */
  public String getIndexInfo(String indexName, String resultPageXslt) throws RemoteException {
    setupContext();
    return impl.getIndexInfo(indexName, resultPageXslt);
  }

  private void setupContext() throws RemoteException {
    ctx.activate();
    SearchContext.setContext(ctx.getItqlHelper(), pep, ctx.getUserName());
    ctx.passivate();
  }

  private static class WSSearchPEP extends AbstractSimplePEP implements Article.Permissions {
    protected static final String[] SUPPORTED_ACTIONS = new String[] { READ_META_DATA };
    protected static final String[][] SUPPORTED_OBLIGATIONS = new String[][] { null };
    
    static {
      init(WSSearchPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSSearchPEP(ServletEndpointContext context) throws Exception {
      super(WSXacmlUtil.lookupPDP(context, "topaz.search.pdpName"), 
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
