
package org.topazproject.ws.admin.service;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/** 
 * The implementation of the annotation administration. Just a dummy for now.
 * 
 */
public class AnnotationServicePortSoapBindingImpl implements Annotation {
  private static final Map subjects = new HashMap();

  /**
   * @see org.topazproject.ws.admin.Annotation#createAnnotation
   */
  public void createAnnotation(String on, String id) throws DuplicateIdException, RemoteException {
    synchronized (subjects) {

      SubjectInfo subject = (SubjectInfo) subjects.get(on);
      if (subject == null){
        subject = new SubjectInfo();
        subjects.put(on,subject);
      }
      else{
        if (subject.annotations.containsKey(id))
          throw new DuplicateIdException(id);
      }
     
      subject.annotations.put(id,new AnnotationInfo());
    }
  }

  /** 
   * @see org.topazproject.ws.admin.Annotation#deleteAnnotation
   */
  public void deleteAnnotation(String on, String id) throws NoSuchIdException, RemoteException {
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
  public String getAnnotationInfo(String on, String id) throws NoSuchIdException, RemoteException {
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
    synchronized (subjects) {
      SubjectInfo subject = (SubjectInfo) subjects.get(on);
      if (subject == null)
        return new String[0];
      
      return (String[]) subject.annotations.keySet().toArray(new String[0]);
    }
  }


  private static class SubjectInfo {
    final Map annotations = new HashMap();
  }
  
  private static class AnnotationInfo {
    String info = null;
  }

}
