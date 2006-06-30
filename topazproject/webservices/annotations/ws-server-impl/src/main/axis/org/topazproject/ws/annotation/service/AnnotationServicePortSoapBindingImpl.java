package org.topazproject.ws.annotation.service;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

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

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.annotation.AnnotationImpl;
import org.topazproject.ws.annotation.PEP;

import fedora.client.APIMStubFactory;
import fedora.client.Uploader;

import fedora.server.management.FedoraAPIM;

/**
 * The implementation of the annotation service.
 */
public class AnnotationServicePortSoapBindingImpl implements Annotation, ServiceLifecycle {
  private static Log     log          =
    LogFactory.getLog(AnnotationServicePortSoapBindingImpl.class);
  private AnnotationImpl impl         = null;
  private Configuration  itqlConfig;
  private Configuration  fedoraConfig;
  private String         hostname;

  /**
   * Creates a new AnnotationServicePortSoapBindingImpl object.
   */
  public AnnotationServicePortSoapBindingImpl() {
    Configuration root = ConfigurationStore.getInstance().getConfiguration();
    itqlConfig     = root.subset("topaz.services.itql");
    fedoraConfig   = root.subset("topaz.services.fedora");
    hostname       = root.getString("topaz.server.hostname");
  }

  /**
   * @see javax.xml.rpc.server.ServiceLifecycle#init
   */
  public void init(Object context) throws ServiceException {
    PEP              pep;
    ProtectedService fedora;
    ItqlHelper       itql;

    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      pep = new PEP((ServletEndpointContext) context);
    } catch (Exception e) {
      if (log.isErrorEnabled())
        log.error("Failed to create PEP.", e);

      throw new ServiceException(e);
    }

    HttpSession session = ((ServletEndpointContext) context).getHttpSession();

    try {
      itql = new ItqlHelper(ProtectedServiceFactory.createService(itqlConfig, session));
    } catch (Exception e) {
      if (log.isErrorEnabled())
        log.error("Failed to create itql service interface", e);

      throw new ServiceException(e);
    }

    URI        fedoraServer;
    FedoraAPIM apim;
    Uploader   uploader;

    try {
      ProtectedService fedoraSvc = ProtectedServiceFactory.createService(fedoraConfig, session);
      URI              fedoraURI = new URI(fedoraSvc.getServiceUri());
      fedoraServer = getRemoteFedoraURI(fedoraURI, hostname);

      if (fedoraSvc.requiresUserNamePassword()) {
        apim =
          APIMStubFactory.getStub(fedoraURI.getScheme(), fedoraURI.getHost(), fedoraURI.getPort(),
                                  fedoraSvc.getUserName(), fedoraSvc.getPassword());
        uploader =
          new Uploader(fedoraURI.getScheme(), fedoraURI.getHost(), fedoraURI.getPort(),
                       fedoraSvc.getUserName(), fedoraSvc.getPassword());
      } else {
        String pqf = fedoraURI.getPath();

        if (fedoraURI.getQuery() != null)
          pqf += ("?" + fedoraURI.getQuery());

        if (fedoraURI.getFragment() != null)
          pqf += ("#" + fedoraURI.getFragment());

        apim =
          APIMStubFactory.getStubAltPath(fedoraURI.getScheme(), fedoraURI.getHost(),
                                         fedoraURI.getPort(), pqf, null, null);

        // XXX: short of wholesale copying and modifying of Uploader, I see no way to get the
        // path in there.
        uploader =
          new Uploader(fedoraURI.getScheme(), fedoraURI.getHost(), fedoraURI.getPort(),
                       fedoraSvc.getUserName(), fedoraSvc.getPassword());
      }
    } catch (Exception e) {
      if (log.isErrorEnabled())
        log.error("Failed to create fedora service interface", e);

      throw new ServiceException(e);
    }

    Principal principal = ((ServletEndpointContext) context).getUserPrincipal();
    String    user = (principal == null) ? null : principal.getName();

    impl = new AnnotationImpl(pep, itql, fedoraServer, apim, uploader, user);
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

  /**
   * @see javax.xml.rpc.server.ServiceLifecycle#destroy
   */
  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null; // release context
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#createAnnotation
   */
  public String createAnnotation(String type, String annotates, String context, String supersedes,
                                 String body) throws NoSuchIdException, RemoteException {
    try {
      return impl.createAnnotation(type, annotates, context, supersedes, body);
    } catch (org.topazproject.ws.annotation.NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("Failed to create annotation", e);

      throw new NoSuchIdException(e.getId());
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#createAnnotation
   */
  public String createAnnotation(String type, String annotates, String context, String supersedes,
                                 String contentType, String content)
                          throws NoSuchIdException, RemoteException {
    try {
      return impl.createAnnotation(type, annotates, context, supersedes, contentType, content);
    } catch (org.topazproject.ws.annotation.NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("Failed to create annotation", e);

      throw new NoSuchIdException(e.getId());
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#deleteAnnotation
   */
  public void deleteAnnotation(String id, boolean deletePreceding)
                        throws NoSuchIdException, RemoteException {
    try {
      impl.deleteAnnotation(id, deletePreceding);
    } catch (org.topazproject.ws.annotation.NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("Failed to delete annotation", e);

      throw new NoSuchIdException(e.getId());
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#getAnnotation
   */
  public String getAnnotation(String id) throws NoSuchIdException, RemoteException {
    try {
      return impl.getAnnotation(id);
    } catch (org.topazproject.ws.annotation.NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("Failed to get annotation", e);

      throw new NoSuchIdException(e.getId());
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#getLatestAnnotations
   */
  public String[] getLatestAnnotations(String id, boolean idsOnly)
                                throws NoSuchIdException, RemoteException {
    try {
      return impl.getLatestAnnotations(id, idsOnly);
    } catch (org.topazproject.ws.annotation.NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("Failed to get latest annotations", e);

      throw new NoSuchIdException(e.getId());
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#getPrecedingAnnotations
   */
  public String[] getPrecedingAnnotations(String id, boolean idsOnly)
                                   throws NoSuchIdException, RemoteException {
    try {
      return impl.getPrecedingAnnotations(id, idsOnly);
    } catch (org.topazproject.ws.annotation.NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("Failed to get preceding annotations", e);

      throw new NoSuchIdException(e.getId());
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#listAnnotations
   */
  public String[] listAnnotations(String on, String type, boolean idsOnly)
                           throws RemoteException {
    return impl.listAnnotations(on, type, idsOnly);
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#setAnnotationState
   */
  public void setAnnotationState(String id, int state)
                          throws NoSuchIdException, RemoteException {
    try {
      impl.setAnnotationState(id, state);
    } catch (org.topazproject.ws.annotation.NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("Failed to set annotation state", e);

      throw new NoSuchIdException(e.getId());
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#listAnnotations
   */
  public String[] listAnnotations(int state) throws RemoteException {
    return impl.listAnnotations(state);
  }
}
