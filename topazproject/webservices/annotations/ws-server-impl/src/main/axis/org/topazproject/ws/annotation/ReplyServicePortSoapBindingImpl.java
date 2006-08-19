/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.annotation;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.rmi.RemoteException;

import java.security.Principal;

import javax.servlet.http.HttpSession;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.annotation.impl.RepliesImpl;
import org.topazproject.ws.annotation.impl.RepliesPEP;

import org.topazproject.xacml.Util;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The implementation of the reply service.
 */
public class ReplyServicePortSoapBindingImpl implements Replies, ServiceLifecycle {
  private static Log log = LogFactory.getLog(ReplyServicePortSoapBindingImpl.class);

  //
  private RepliesImpl   impl                 = null;
  private Configuration itqlConfig;
  private Configuration fedoraConfig;
  private Configuration fedoraUploaderConfig;
  private String        hostname;
  private String        baseURI;

  /**
   * Creates a new ReplyServicePortSoapBindingImpl object.
   */
  public ReplyServicePortSoapBindingImpl() {
    Configuration root = ConfigurationStore.getInstance().getConfiguration();
    itqlConfig             = root.subset("topaz.services.itql");
    fedoraConfig           = root.subset("topaz.services.fedora");
    fedoraUploaderConfig   = root.subset("topaz.services.fedoraUploader");
    hostname               = root.getString("topaz.server.hostname");
    baseURI                = root.getString("topaz.objects.base-uri");
  }

  /**
   * @see javax.xml.rpc.server.ServiceLifecycle#init
   */
  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      ItqlHelper.validateUri(baseURI, "topaz.objects.base-uri");
      ItqlHelper.validateUri(itqlConfig.getString("uri"), "topaz.services.itql.uri");
      ItqlHelper.validateUri(fedoraConfig.getString("uri"), "topaz.services.fedora.uri");
      ItqlHelper.validateUri(fedoraUploaderConfig.getString("uri"),
                             "topaz.services.fedoraUploader.uri");
    } catch (Throwable t) {
      throw new ServiceException("invalid/missing configuration", t);
    }

    RepliesPEP       pep = createPEP((ServletEndpointContext) context);

    HttpSession      session = ((ServletEndpointContext) context).getHttpSession();

    ProtectedService itqlSvc     = createService(itqlConfig, session, "itql");
    ProtectedService fedoraSvc   = createService(fedoraConfig, session, "fedora");
    ProtectedService uploaderSvc = createService(fedoraUploaderConfig, session, "fedora-uploader");

    URI              itqlURI     = createServiceUri(itqlSvc.getServiceUri(), "itql");
    URI              fedoraURI   = createServiceUri(fedoraSvc.getServiceUri(), "fedora");
    URI              uploaderURI = createServiceUri(uploaderSvc.getServiceUri(), "fedora-uploader");

    URI              fedoraServer = getRemoteFedoraURI(fedoraURI, hostname);

    ItqlHelper       itql     = createItqlHelper(itqlSvc);
    FedoraAPIM       apim     = createFedoraAPIM(fedoraSvc);
    Uploader         uploader = createFedoraUploader(uploaderSvc);

    Principal        principal = ((ServletEndpointContext) context).getUserPrincipal();
    String           user      = (principal == null) ? null : principal.getName();

    impl = new RepliesImpl(pep, itql, fedoraServer, apim, uploader, user, baseURI);
  }

  /**
   * @see javax.xml.rpc.server.ServiceLifecycle#destroy
   */
  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null; // release context
  }

  /**
   * @see org.topazproject.ws.annotation.Reply#createReply
   */
  public String createReply(String type, String root, String inReplyTo, String title, String body)
                     throws RemoteException, NoSuchIdException {
    try {
      return impl.createReply(type, root, inReplyTo, title, body);
    } catch (NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("", e);

      throw e;
    } catch (RemoteException e) {
      log.info("", e);
      throw e;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Reply#createReply
   */
  public String createReply(String type, String root, String inReplyTo, String title,
                            String contentType, byte[] content)
                     throws RemoteException, NoSuchIdException {
    try {
      return impl.createReply(type, root, inReplyTo, title, contentType, content);
    } catch (NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("", e);

      throw e;
    } catch (RemoteException e) {
      log.info("", e);
      throw e;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Reply#deleteReplies
   */
  public void deleteReplies(String root, String inReplyTo)
                     throws NoSuchIdException, RemoteException {
    try {
      impl.deleteReplies(root, inReplyTo);
    } catch (NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("", e);

      throw e;
    } catch (RemoteException e) {
      log.info("", e);
      throw e;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Reply#deleteReplies
   */
  public void deleteReplies(String id) throws NoSuchIdException, RemoteException {
    try {
      impl.deleteReplies(id);
    } catch (NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("", e);

      throw e;
    } catch (RemoteException e) {
      log.info("", e);
      throw e;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Reply#getReplyInfo
   */
  public ReplyInfo getReplyInfo(String id) throws NoSuchIdException, RemoteException {
    try {
      return impl.getReplyInfo(id);
    } catch (NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("", e);

      throw e;
    } catch (RemoteException e) {
      log.info("", e);
      throw e;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Reply#listReplies
   */
  public ReplyInfo[] listReplies(String root, String inReplyTo)
                          throws NoSuchIdException, RemoteException {
    try {
      return impl.listReplies(root, inReplyTo);
    } catch (RemoteException e) {
      log.info("", e);
      throw e;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Reply#listReplies
   */
  public ReplyInfo[] listAllReplies(String root, String inReplyTo)
                             throws NoSuchIdException, RemoteException {
    try {
      return impl.listAllReplies(root, inReplyTo);
    } catch (RemoteException e) {
      log.info("", e);
      throw e;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Reply#listReplies
   */
  public ReplyThread getReplyThread(String root, String inReplyTo)
                             throws NoSuchIdException, RemoteException {
    try {
      return impl.getReplyThread(root, inReplyTo);
    } catch (RemoteException e) {
      log.info("", e);
      throw e;
    } catch (RuntimeException re) {
      log.warn("", re);
      throw re;
    } catch (Error e) {
      log.error("", e);
      throw e;
    }
  }

  private static ProtectedService createService(Configuration conf, HttpSession session, String name)
                                         throws ServiceException {
    try {
      return ProtectedServiceFactory.createService(conf, session);
    } catch (Exception e) {
      throw new ServiceException("Failed to create a service URI for '" + name + "'", e);
    }
  }

  private static RepliesPEP createPEP(ServletEndpointContext context)
                               throws ServiceException {
    try {
      return new WSRepliesPEP(context);
    } catch (IOException e) {
      throw new ServiceException("Failed to create PEP", e);
    } catch (ParsingException e) {
      throw new ServiceException("Failed to create PEP", e);
    } catch (UnknownIdentifierException e) {
      throw new ServiceException("Failed to create PEP", e);
    }
  }

  private static URI createServiceUri(String uri, String name) {
    try {
      URL u = new URL(uri);

      return URI.create(u.toString());
    } catch (MalformedURLException e) {
      throw new Error("Misconfigured uri for service '" + name + "'", e);
    }
  }

  private static ItqlHelper createItqlHelper(ProtectedService itqlSvc)
                                      throws ServiceException {
    try {
      return new ItqlHelper(itqlSvc);
    } catch (MalformedURLException e) {
      throw new ServiceException("Misconfigured URL for itql service", e);
    } catch (RemoteException e) {
      throw new ServiceException("Failed to initialize itql service", e);
    }
  }

  private static URI getRemoteFedoraURI(URI fedoraURI, String hostname) {
    if (!fedoraURI.getHost().equals("localhost"))
      return fedoraURI; // it's already remote

    try {
      return new URI(fedoraURI.getScheme(), null, hostname, fedoraURI.getPort(),
                     fedoraURI.getPath(), null, null);
    } catch (URISyntaxException use) {
      throw new Error(use); // Can't happen
    }
  }

  private static FedoraAPIM createFedoraAPIM(ProtectedService fedoraSvc)
                                      throws ServiceException {
    try {
      return APIMStubFactory.create(fedoraSvc);
    } catch (MalformedURLException e) {
      throw new ServiceException("Failed to create fedora-API-M client stub", e);
    }
  }

  private static Uploader createFedoraUploader(ProtectedService uploaderSvc) {
    return new Uploader(uploaderSvc);
  }

  private static class WSRepliesPEP extends RepliesPEP {
    static {
      init(WSRepliesPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSRepliesPEP(ServletEndpointContext context)
                 throws IOException, ParsingException, UnknownIdentifierException {
      super(Util.lookupPDP(context, "topaz.replies.pdpName"), Util.createSubjAttrs(context));
    }
  }
}
