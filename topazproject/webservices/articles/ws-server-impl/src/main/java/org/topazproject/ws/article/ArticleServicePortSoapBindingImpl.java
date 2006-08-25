/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.ws.article.impl.ArticleImpl;
import org.topazproject.ws.article.impl.ArticlePEP;
import org.topazproject.xacml.Util;

public class ArticleServicePortSoapBindingImpl implements Article, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(ArticleServicePortSoapBindingImpl.class);

  private ArticleImpl impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      ArticlePEP pep = new WSArticlePEP((ServletEndpointContext) context);

      // get other config
      Configuration conf = ConfigurationStore.getInstance().getConfiguration();
      conf = conf.subset("topaz");

      if (!conf.containsKey("services.fedora.uri"))
        throw new ConfigurationException("missing key 'topaz.services.fedora.uri'");
      if (!conf.containsKey("services.fedoraUploader.uri"))
        throw new ConfigurationException("missing key 'topaz.services.fedoraUploader.uri'");
      if (!conf.containsKey("services.itql.uri"))
        throw new ConfigurationException("missing key 'topaz.services.itql.uri'");
      if (!conf.containsKey("server.hostname"))
        throw new ConfigurationException("missing key 'topaz.server.hostname'");

      Configuration fedoraConf = conf.subset("services.fedora");
      Configuration uploadConf = conf.subset("services.fedoraUploader");
      Configuration itqlConf   = conf.subset("services.itql");

      // xxx: since this web service is running at application-scope no sessions
      //HttpSession session = ((ServletEndpointContext) context).getHttpSession();
      HttpSession session = null;

      ProtectedService fedoraSvc = ProtectedServiceFactory.createService(fedoraConf, session);
      ProtectedService uploadSvc = ProtectedServiceFactory.createService(uploadConf, session);
      ProtectedService itqlSvc   = ProtectedServiceFactory.createService(itqlConf, session);

      String hostname = conf.getString("server.hostname");

      // create the impl
      impl = new ArticleImpl(fedoraSvc, uploadSvc, itqlSvc, hostname, pep);
    } catch (Exception e) {
      log.error("Failed to initialize ArticleImpl.", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null;
  }

  /**
   * @see org.topazproject.ws.article.Article#ingest
   */
  public String ingest(DataHandler zip)
      throws RemoteException, DuplicateIdException, IngestException {
    try {
      return impl.ingest(zip);
    } catch (DuplicateIdException die) {
      log.info("", die);
      throw die;
    } catch (IngestException ie) {
      log.info("", ie);
      throw ie;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.article.Article#markSuperseded
   */
  public void markSuperseded(String oldDoi, String newDoi)
      throws RemoteException, NoSuchIdException {
    try {
      impl.markSuperseded(oldDoi, newDoi);
    } catch (NoSuchIdException nsie) {
      log.info("", nsie);
      throw nsie;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.article.Article#delete
   */
  public void delete(String doi, boolean purge) throws RemoteException, NoSuchIdException {
    try {
      impl.delete(doi, purge);
    } catch (NoSuchIdException nsie) {
      log.info("", nsie);
      throw nsie;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.article.Article#setState
   */
  public void setState(String doi, int state) throws RemoteException, NoSuchIdException {
    try {
      impl.setState(doi, state);
    } catch (NoSuchIdException nsie) {
      log.info("", nsie);
      throw nsie;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.article.Article#getObjectURL
   */
  public String getObjectURL(String doi, String rep) throws RemoteException, NoSuchIdException {
    try {
      return impl.getObjectURL(doi, rep);
    } catch (NoSuchIdException nsie) {
      log.info("", nsie);
      throw nsie;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  private static class WSArticlePEP extends ArticlePEP {
    static {
      init(WSArticlePEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSArticlePEP(ServletEndpointContext context) throws Exception {
      super(Util.lookupPDP(context, "topaz.articles.pdpName"), Util.createSubjAttrs(context));
    }
  }
}
