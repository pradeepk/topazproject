package org.topazproject.ws.admin.service;

import java.rmi.RemoteException;

import java.security.Principal;

import java.util.HashMap;
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
  private static Log       log      = LogFactory.getLog(AnnotationServicePortSoapBindingImpl.class);
  private static final Map subjects = new HashMap();
  private PEP              pep      = null;

  /**
   * @see javax.xml.rpc.server.ServiceLifecycle#init
   */
  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");
    try{
      pep = new PEP((ServletEndpointContext)context);
    }
    catch (Exception e){
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
  public void createAnnotation(String on, String id) throws DuplicateIdException, RemoteException {
     checkAccess(PEP.CREATE_ANNOTATION, on, id);
    
     synchronized (subjects) {
      SubjectInfo subject = (SubjectInfo) subjects.get(on);

      if (subject == null) {
        subject = new SubjectInfo();
        subjects.put(on, subject);
      } else {
        if (subject.annotations.containsKey(id))
          throw new DuplicateIdException(id);
      }

      subject.annotations.put(id, new AnnotationInfo());
    }
  }

  /**
   * @see org.topazproject.ws.admin.Annotation#deleteAnnotation
   */
  public void deleteAnnotation(String on, String id) throws NoSuchIdException, RemoteException {
    checkAccess(PEP.DELETE_ANNOTATION, on, id);

    synchronized (subjects) {
      SubjectInfo subject = (SubjectInfo) subjects.get(on);

      if ((subject == null) || (subject.annotations.remove(id) == null))
        throw new NoSuchIdException(id);

      if (subject.annotations.size() == 0)
        subjects.remove(on);
    }
  }

  /**
   * @see org.topazproject.ws.admin.Annotation#setAnnotationInfo
   */
  public void setAnnotationInfo(String on, String id, String annotationDef)
                         throws NoSuchIdException, RemoteException {
    checkAccess(PEP.SET_ANNOTATION_INFO, on, id);

    synchronized (subjects) {
      SubjectInfo subject = (SubjectInfo) subjects.get(on);

      if (subject == null)
        throw new NoSuchIdException(id);

      AnnotationInfo annotation = (AnnotationInfo) subject.annotations.get(id);

      if (annotation == null)
        throw new NoSuchIdException(id);

      annotation.info = annotationDef;
    }
  }

  /**
   * @see org.topazproject.ws.admin.Annotation#getAnnotationInfo
   */
  public String getAnnotationInfo(String on, String id)
                           throws NoSuchIdException, RemoteException {
    checkAccess(PEP.GET_ANNOTATION_INFO, on, id);

    synchronized (subjects) {
      SubjectInfo subject = (SubjectInfo) subjects.get(on);

      if (subject == null)
        throw new NoSuchIdException(id);

      AnnotationInfo annotation = (AnnotationInfo) subject.annotations.get(id);

      if (annotation == null)
        throw new NoSuchIdException(id);

      return annotation.info;
    }
  }

  /**
   * @see org.topazproject.ws.admin.Annotation#listAnnotations
   */
  public String[] listAnnotations(String on) throws RemoteException {
    checkAccess(PEP.LIST_ANNOTATIONS, on, null);

    synchronized (subjects) {
      SubjectInfo subject = (SubjectInfo) subjects.get(on);

      if (subject == null)
        return new String[0];

      return (String[]) subject.annotations.keySet().toArray(new String[0]);
    }
  }

  private Set checkAccess(String action, String on, String id){

    try {
      if (log.isTraceEnabled())
        log.trace("checkAccess(" + action  + ", " + on + ", " + id + ")");

      Set s = pep.checkAccess(action,on,id);
      
      if (log.isDebugEnabled())
        log.debug("allowed access to " + action  + "(" + on + ", " + id + ")"); 
    
      return s;
    }
    catch (SecurityException e){
      if (log.isDebugEnabled())
        log.debug("denied access to " + action  + "(" + on + ", " + id + ")", e); 
      
      throw e;
    }
      
  }

  private static class SubjectInfo {
    final Map annotations = new HashMap();
  }

  private static class AnnotationInfo {
    String info = null;
  }

}
