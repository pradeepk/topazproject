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

import javax.activation.DataHandler;

import javax.servlet.http.HttpSession;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import org.topazproject.common.ExceptionUtils;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.annotation.impl.AnnotationsImpl;
import org.topazproject.ws.annotation.impl.AnnotationsPEP;

import org.topazproject.xacml.Util;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The implementation of the annotation service.
 */
public class AnnotationServicePortSoapBindingImpl implements Annotations, ServiceLifecycle {
  private static Log log = LogFactory.getLog(AnnotationServicePortSoapBindingImpl.class);

  //
  private AnnotationsImpl impl                 = null;
  private Configuration   itqlConfig;
  private Configuration   fedoraConfig;
  private Configuration   fedoraUploaderConfig;
  private String          hostname;
  private String          baseURI;

  /**
   * Creates a new AnnotationServicePortSoapBindingImpl object.
   */
  public AnnotationServicePortSoapBindingImpl() {
    Configuration root = ConfigurationStore.getInstance().getConfiguration();
    itqlConfig             = root.subset("topaz.services.itql");
    fedoraConfig           = root.subset("topaz.services.fedora");
    fedoraUploaderConfig   = root.subset("topaz.services.fedoraUploader");
    hostname               = root.getString("topaz.server.hostname");
    baseURI                = root.getString("topaz.objects.base-uri");
  }

  /*
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

    AnnotationsPEP   pep = createPEP((ServletEndpointContext) context);

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

    impl = new AnnotationsImpl(pep, itql, fedoraServer, apim, uploader, user, baseURI);
  }

  /*
   * @see javax.xml.rpc.server.ServiceLifecycle#destroy
   */
  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null; // release context
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#createAnnotation
   */
  public String createAnnotation(String mediator, String type, String annotates, String context,
                                 String supersedes, boolean anonymize, String title, String body)
                          throws NoSuchAnnotationIdException, RemoteException {
    try {
      return impl.createAnnotation(mediator, type, annotates, context, supersedes, anonymize,
                                   title, body);
    } catch (Throwable t) {
      newExceptionHandler(t).createAnnotation(null, null, null, null, null, false, null, null);
      return null;      // not reached
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#createAnnotation
   */
  public String createAnnotation(String mediator, String type, String annotates, String context,
                                 String supersedes, boolean anonymize, String title,
                                 String contentType, byte[] content)
                          throws NoSuchAnnotationIdException, RemoteException {
    try {
      return impl.createAnnotation(mediator, type, annotates, context, supersedes, anonymize,
                                   title, contentType, content);
    } catch (Throwable t) {
      newExceptionHandler(t).createAnnotation(null, null, null, null, null, false, null, null,
                                              null);
      return null;      // not reached
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#deleteAnnotation
   */
  public void deleteAnnotation(String id, boolean deletePreceding)
                        throws NoSuchAnnotationIdException, RemoteException {
    try {
      impl.deleteAnnotation(id, deletePreceding);
    } catch (Throwable t) {
      newExceptionHandler(t).deleteAnnotation(null, false);
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#getAnnotationInfo
   */
  public AnnotationInfo getAnnotationInfo(String id)
                                   throws NoSuchAnnotationIdException, RemoteException {
    try {
      return impl.getAnnotationInfo(id);
    } catch (Throwable t) {
      newExceptionHandler(t).getAnnotationInfo(null);
      return null;      // not reached
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#getLatestAnnotations
   */
  public AnnotationInfo[] getLatestAnnotations(String id)
                                        throws NoSuchAnnotationIdException, RemoteException {
    try {
      return impl.getLatestAnnotations(id);
    } catch (Throwable t) {
      newExceptionHandler(t).getLatestAnnotations(null);
      return null;      // not reached
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#getPrecedingAnnotations
   */
  public AnnotationInfo[] getPrecedingAnnotations(String id)
                                           throws NoSuchAnnotationIdException, RemoteException {
    try {
      return impl.getPrecedingAnnotations(id);
    } catch (Throwable t) {
      newExceptionHandler(t).getPrecedingAnnotations(null);
      return null;      // not reached
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#listAnnotations
   */
  public AnnotationInfo[] listAnnotations(String mediator, String on, String type)
                                   throws RemoteException {
    try {
      return impl.listAnnotations(mediator, on, type);
    } catch (Throwable t) {
      newExceptionHandler(t).listAnnotations(null, null, null);
      return null;      // not reached
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#setAnnotationState
   */
  public void setAnnotationState(String id, int state)
                          throws NoSuchAnnotationIdException, RemoteException {
    try {
      impl.setAnnotationState(id, state);
    } catch (Throwable t) {
      newExceptionHandler(t).setAnnotationState(null, 0);
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#listAnnotations
   */
  public String[] listAnnotations(String mediator, int state)
                           throws RemoteException {
    try {
      return impl.listAnnotations(mediator, state);
    } catch (Throwable t) {
      newExceptionHandler(t).listAnnotations(null, 0);
      return null;      // not reached
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#getAnnotatedContent
   */
  public DataHandler getAnnotatedContent(String resource, String resourceURL, DataHandler content,
                                         String mediator, String type)
                                  throws RemoteException {
    try {
      return impl.getAnnotatedContent(resource, resourceURL, content, mediator, type);
    } catch (Throwable t) {
      newExceptionHandler(t).getAnnotatedContent(null, null, null, null, null);
      return null;      // not reached
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

  private static AnnotationsPEP createPEP(ServletEndpointContext context)
                                   throws ServiceException {
    try {
      return new WSAnnotationsPEP(context);
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

  private static Annotations newExceptionHandler(Throwable t) {
    return ((Annotations) ExceptionUtils.newExceptionHandler(Annotations.class, t, log));
  }

  private static class WSAnnotationsPEP extends AnnotationsPEP {
    static {
      init(WSAnnotationsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSAnnotationsPEP(ServletEndpointContext context)
                     throws IOException, ParsingException, UnknownIdentifierException {
      super(Util.lookupPDP(context, "topaz.annotations.pdpName"), Util.createSubjAttrs(context));
    }
  }
}
