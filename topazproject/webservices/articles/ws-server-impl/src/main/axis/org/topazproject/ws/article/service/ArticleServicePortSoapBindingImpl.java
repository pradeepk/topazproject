
package org.topazproject.ws.article.service;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.ws.article.ArticleImpl;
import org.topazproject.ws.article.ArticlePEP;
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
      if (!conf.containsKey("services.itql.uri"))
        throw new ConfigurationException("missing key 'topaz.services.itql.uri'");
      if (!conf.containsKey("server.hostname"))
        throw new ConfigurationException("missing key 'topaz.server.hostname'");

      URI fedora = new URI(conf.getString("services.fedora.uri"));
      String username = conf.getString("services.fedora.userName", null);
      String password = conf.getString("services.fedora.password", null);
      String hostname = conf.getString("server.hostname");
      URI mulgara = new URI(conf.getString("services.itql.uri"));

      // create the impl
      impl = new ArticleImpl(fedora, username, password, mulgara, hostname, pep);
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
    } catch (org.topazproject.ws.article.DuplicateIdException die) {
      log.info(die);
      throw new DuplicateIdException(die.getId());
    } catch (org.topazproject.ws.article.IngestException ie) {
      log.info(ie);
      throw new IngestException(ie.getMessage());
    }
  }

  /**
   * @see org.topazproject.ws.article.Article#markSuperseded
   */
  public void markSuperseded(String oldDoi, String newDoi)
      throws RemoteException, NoSuchIdException {
    try {
      impl.markSuperseded(oldDoi, newDoi);
    } catch (org.topazproject.ws.article.NoSuchIdException nsie) {
      log.info(nsie);
      throw new NoSuchIdException(nsie.getId());
    }
  }

  /**
   * @see org.topazproject.ws.article.Article#delete
   */
  public void delete(String doi, boolean purge) throws RemoteException, NoSuchIdException {
    try {
      impl.delete(doi, purge);
    } catch (org.topazproject.ws.article.NoSuchIdException nsie) {
      log.info(nsie);
      throw new NoSuchIdException(nsie.getId());
    }
  }

  /**
   * @see org.topazproject.ws.article.Article#setState
   */
  public void setState(String doi, int state) throws RemoteException, NoSuchIdException {
    try {
      impl.setState(doi, state);
    } catch (org.topazproject.ws.article.NoSuchIdException nsie) {
      log.info(nsie);
      throw new NoSuchIdException(nsie.getId());
    }
  }

  /**
   * @see org.topazproject.ws.article.Article#getObjectURL
   */
  public String getObjectURL(String doi, String rep) throws RemoteException, NoSuchIdException {
    try {
      return impl.getObjectURL(doi, rep);
    } catch (org.topazproject.ws.article.NoSuchIdException nsie) {
      log.info(nsie);
      throw new NoSuchIdException(nsie.getId());
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
