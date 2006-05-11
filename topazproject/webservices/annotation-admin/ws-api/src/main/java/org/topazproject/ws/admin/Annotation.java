
package org.topazproject.ws.admin;

import java.rmi.RemoteException;


/** 
 * Annotation related administrative operations.
 * 
 * @author Pradeep Krishnan
 */
public interface Annotation {
  /** 
   * Create a new annotation. 
   * 
   * @param on the subject of this annotation 
   * @param id  an identifier for this annotation; used for all subsequent lookups and manipulations
   * @throws DuplicateIdException if a annotation with the given id already exists
   * @throws RemoteException if some other error occured
   */
  public void createAnnotation(String on, String id) throws DuplicateIdException, RemoteException;

  /** 
   * Delete an annotation.  
   *
   * @param on the subject of this annotation 
   * @param id  the id of the annotation to remove
   * @throws NoSuchIdException if the annotation does not exist
   * @throws RemoteException if some other error occured
   */
  public void deleteAnnotation(String on, String id) throws NoSuchIdException, RemoteException;

  /** 
   * Set the given annotation's meta-data. The new data completely replaces the old.
   * 
   * @param on the subject of this annotation 
   * @param id          the id of the annotation to update
   * @param annotationDef  an xml document containing the new meta-data for this annotation; it must
   *                    follow the ??? DTD.
   * @throws NoSuchIdException if the annotation does not exist
   * @throws RemoteException if some other error occured
   */
  public void setAnnotationInfo(String on, String id, String annotationDef)
      throws NoSuchIdException, RemoteException;

  /** 
   * Retrieve the given annotation's current meta-data.
   * 
   * @param on the subject of this annotation 
   * @param id  the id of the annotation for which to get the meta-data
   * @return an xml document containing the current meta-data for this annotation, or null if none has
   *         been set yet; it follows the ???  DTD.
   * @throws NoSuchIdException if the annotation does not exist
   * @throws RemoteException if some other error occured
   */
  public String getAnnotationInfo(String on, String id) throws NoSuchIdException, RemoteException;

  /** 
   * List the set of annotations.
   * 
   * @param on the subject of this annotation 
   * @return an array of id's; if no annotations have been defined, an empty array is returned
   * @throws RemoteException 
   */
  public String[] listAnnotations(String on) throws RemoteException;

}
