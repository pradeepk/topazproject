package org.topazproject.ws.admin.service;

import java.net.URI;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.ws.annotation.PEP;

/**
 * The implementation of the annotation administration. Just a dummy for now.
 */
public class AnnotationServicePortSoapBindingImpl implements Annotation, ServiceLifecycle {
  private static Log       log = LogFactory.getLog(AnnotationServicePortSoapBindingImpl.class);
  private static final Map annotations = new HashMap();
  private static int       nextId      = 0;
  private PEP              pep         = null;

  /**
   * @see javax.xml.rpc.server.ServiceLifecycle#init
   */
  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      pep = new PEP((ServletEndpointContext) context);
    } catch (Exception e) {
      if (log.isErrorEnabled())
        log.error("Failed to create PEP.", e);

      throw new ServiceException(e);
    }
  }

  /**
   * @see javax.xml.rpc.server.ServiceLifecycle#destroy
   */
  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    pep = null; // release context
  }

  /**
   * @see org.topazproject.ws.admin.Annotation#createAnnotation
   */
  public String createAnnotation(String on, String annotationInfo)
                          throws RemoteException {
    checkAccess(PEP.CREATE_ANNOTATION, on);

    String id;

    synchronized (annotations) {
      id = "annotations:id#" + ++nextId;
      annotations.put(id, new AnnotationInfo(on, annotationInfo));
    }

    return id;
  }

  /**
   * @see org.topazproject.ws.admin.Annotation#deleteAnnotation
   */
  public void deleteAnnotation(String id) throws NoSuchIdException, RemoteException {
    checkAccess(PEP.DELETE_ANNOTATION, id);

    synchronized (annotations) {
      if (annotations.remove(id) == null)
        throw new NoSuchIdException(id);
    }
  }

  /**
   * @see org.topazproject.ws.admin.Annotation#setAnnotationInfo
   */
  public void setAnnotationInfo(String id, String annotationDef)
                         throws NoSuchIdException, RemoteException {
    checkAccess(PEP.SET_ANNOTATION_INFO, id);

    synchronized (annotations) {
      AnnotationInfo annotation = (AnnotationInfo) annotations.get(id);

      if (annotation == null)
        throw new NoSuchIdException(id);

      annotation.info = annotationDef;
    }
  }

  /**
   * @see org.topazproject.ws.admin.Annotation#getAnnotationInfo
   */
  public String getAnnotationInfo(String id) throws NoSuchIdException, RemoteException {
    checkAccess(PEP.GET_ANNOTATION_INFO, id);

    synchronized (annotations) {
      AnnotationInfo annotation = (AnnotationInfo) annotations.get(id);

      if (annotation == null)
        throw new NoSuchIdException(id);

      return annotation.info;
    }
  }

  /**
   * @see org.topazproject.ws.admin.Annotation#listAnnotations
   */
  public String[] listAnnotations(String on) throws RemoteException {
    checkAccess(PEP.LIST_ANNOTATIONS, on);

    synchronized (annotations) {
      ArrayList ids = new ArrayList();

      for (Iterator it = annotations.entrySet().iterator(); it.hasNext();) {
        Map.Entry e = (Map.Entry) it.next();

        if (((AnnotationInfo) e.getValue()).on.equals(on))
          ids.add(e.getKey());
      }

      return (String[]) ids.toArray(new String[0]);
    }
  }

  /**
   * @see org.topazproject.ws.admin.Annotation#setAnnotationState
   */
  public void setAnnotationState(String id, int state)
                          throws NoSuchIdException, RemoteException {
    checkAccess(PEP.SET_ANNOTATION_STATE, id);

    synchronized (annotations) {
      AnnotationInfo annotation = (AnnotationInfo) annotations.get(id);

      if (annotation == null)
        throw new NoSuchIdException(id);

      annotation.state = state;
    }
  }

  /**
   * @see org.topazproject.ws.admin.Annotation#listAnnotations
   */
  public String[] listAnnotations(int state) throws RemoteException {
    checkAccess(PEP.LIST_ANNOTATIONS_IN_STATE, "" + state);

    synchronized (annotations) {
      ArrayList ids = new ArrayList();

      for (Iterator it = annotations.entrySet().iterator(); it.hasNext();) {
        Map.Entry e = (Map.Entry) it.next();

        if (((AnnotationInfo) e.getValue()).state == state)
          ids.add(e.getKey());
      }

      return (String[]) ids.toArray(new String[0]);
    }
  }

  private Set checkAccess(String action, String resource) {
    try {
      if (log.isTraceEnabled())
        log.trace("checkAccess(" + action + ", " + resource + ")");

      Set s = pep.checkAccess(action, URI.create(resource));

      if (log.isDebugEnabled())
        log.debug("allowed access to " + action + "(" + resource + ")");

      return s;
    } catch (SecurityException e) {
      if (log.isDebugEnabled())
        log.debug("denied access to " + action + "(" + resource + ")", e);

      throw e;
    }
  }

  private static class AnnotationInfo {
    String on;
    String info;
    int    state = 0;

    public AnnotationInfo(String on, String info) {
      this.on     = on;
      this.info   = info;
    }
  }
}
