package org.topazproject.ws.annotation.service;

import java.io.IOException;

import java.rmi.RemoteException;

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

/**
 * The implementation of the annotation service.
 */
public class AnnotationServicePortSoapBindingImpl implements Annotation, ServiceLifecycle {
  private static Log     log = LogFactory.getLog(AnnotationServicePortSoapBindingImpl.class);
  private AnnotationImpl impl       = null;
  private Configuration  itqlConfig;

  /**
   * Creates a new AnnotationServicePortSoapBindingImpl object.
   */
  public AnnotationServicePortSoapBindingImpl() {
    Configuration root = ConfigurationStore.getInstance().getConfiguration();
    itqlConfig = root.subset("topaz.services.itql");
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

    impl = new AnnotationImpl(itql, pep);
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
  public String createAnnotation(String on, String annotationInfo)
                          throws RemoteException {
    return impl.createAnnotation(on, annotationInfo);
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#deleteAnnotation
   */
  public void deleteAnnotation(String id) throws NoSuchIdException, RemoteException {
    try {
      impl.deleteAnnotation(id);
    } catch (org.topazproject.ws.annotation.NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("Failed to delete annotation", e);

      throw new NoSuchIdException(e.getId());
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#setAnnotationInfo
   */
  public void setAnnotationInfo(String id, String annotationDef)
                         throws NoSuchIdException, RemoteException {
    try {
      impl.setAnnotationInfo(id, annotationDef);
    } catch (org.topazproject.ws.annotation.NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("Failed to set annotation info", e);

      throw new NoSuchIdException(e.getId());
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#getAnnotationInfo
   */
  public String getAnnotationInfo(String id) throws NoSuchIdException, RemoteException {
    try {
      return impl.getAnnotationInfo(id);
    } catch (org.topazproject.ws.annotation.NoSuchIdException e) {
      if (log.isDebugEnabled())
        log.debug("Failed to get annotation info", e);

      throw new NoSuchIdException(e.getId());
    }
  }

  /**
   * @see org.topazproject.ws.annotation.Annotation#listAnnotations
   */
  public String[] listAnnotations(String on) throws RemoteException {
    return impl.listAnnotations(on);
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
