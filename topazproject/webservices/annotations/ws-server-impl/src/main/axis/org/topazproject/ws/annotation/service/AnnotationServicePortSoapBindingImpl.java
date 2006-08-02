package org.topazproject.ws.annotation.service;

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

import org.topazproject.ws.annotation.AnnotationImpl;
import org.topazproject.ws.annotation.AnnotationPEP;

import org.topazproject.xacml.Util;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The implementation of the annotation service.
 */
public class AnnotationServicePortSoapBindingImpl implements Annotation, ServiceLifecycle {
  private static Log log = LogFactory.getLog(AnnotationServicePortSoapBindingImpl.class);

  //
  private AnnotationImpl impl                 = null;
  private Configuration  itqlConfig;
  private Configuration  fedoraConfig;
  private Configuration  fedoraUploaderConfig;
  private String         hostname;

  /**
   * Creates a new AnnotationServicePortSoapBindingImpl object.
   */
  public AnnotationServicePortSoapBindingImpl() {
    Configuration root = ConfigurationStore.getInstance().getConfiguration();
    itqlConfig             = root.subset("topaz.services.itql");
    fedoraConfig           = root.subset("topaz.services.fedora");
    fedoraUploaderConfig   = root.subset("topaz.services.fedoraUploader");
    hostname               = root.getString("topaz.server.hostname");
  }

  /**
   * @see javax.xml.rpc.server.ServiceLifecycle#init
   */
  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    AnnotationPEP    pep = createPEP((ServletEndpointContext) context);

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

    impl = new AnnotationImpl(pep, itql, fedoraServer, apim, uploader, user);
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
                                 String contentType, byte[] content)
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

  private static ProtectedService createService(Configuration conf, HttpSession session, String name)
                                         throws ServiceException {
    try {
      return ProtectedServiceFactory.createService(conf, session);
    } catch (IOException e) {
      throw new ServiceException("Failed to create a service URI for '" + name + "'", e);
    }
  }

  private static AnnotationPEP createPEP(ServletEndpointContext context)
                                  throws ServiceException {
    try {
      return new WSAnnotationPEP(context);
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

  private static class WSAnnotationPEP extends AnnotationPEP {
    static {
      init(WSAnnotationPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSAnnotationPEP(ServletEndpointContext context)
                    throws IOException, ParsingException, UnknownIdentifierException {
      super(Util.lookupPDP(context, "topaz.annotations.pdpName"), Util.createSubjAttrs(context));
    }
  }
}
