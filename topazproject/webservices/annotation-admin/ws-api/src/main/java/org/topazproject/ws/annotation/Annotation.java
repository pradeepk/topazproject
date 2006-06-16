package org.topazproject.ws.annotation;

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
   * @param annotates the subject of this annotation
   * @param annotationDef an xml document containing the new meta-data for this annotation; it must
   *        follow the ??? DTD.
   *
   * @return Returns a unique identifier for the newly created annotation
   *
   * @throws RemoteException if an error occured
   */
  public String createAnnotation(String annotates, String annotationDef)
                          throws RemoteException;

  /**
   * Delete an annotation.
   *
   * @param id the id of the annotation to remove
   *
   * @throws NoSuchIdException if the annotation does not exist
   * @throws RemoteException if some other error occured
   */
  public void deleteAnnotation(String id) throws NoSuchIdException, RemoteException;

  /**
   * Set the given annotation's meta-data. The new data completely replaces the old.
   *
   * @param id the id of the annotation to update
   * @param annotationDef an xml document containing the new meta-data for this annotation; it must
   *        follow the ??? DTD.
   *
   * @throws NoSuchIdException if the annotation does not exist
   * @throws RemoteException if some other error occured
   */
  public void setAnnotationInfo(String id, String annotationDef)
                         throws NoSuchIdException, RemoteException;

  /**
   * Retrieve the given annotation's current meta-data.
   *
   * @param id the id of the annotation for which to get the meta-data
   *
   * @return an xml document containing the current meta-data for this annotation, or null if none
   *         has been set yet; it follows the ???  DTD.
   *
   * @throws NoSuchIdException if the annotation does not exist
   * @throws RemoteException if some other error occured
   */
  public String getAnnotationInfo(String id) throws NoSuchIdException, RemoteException;

  /**
   * List the set of annotations on a resource.
   *
   * @param annotates the resource for which annotations are to be looked-up
   *
   * @return an array of id's; if no annotations have been defined, an empty array is returned
   *
   * @throws RemoteException if an error occured
   */
  public String[] listAnnotations(String annotates) throws RemoteException;

  /**
   * Sets the state of an annotation. (eg. flagged for review)
   *
   * @param id the id of the annotation whose state is changed
   * @param state the new state
   *
   * @throws NoSuchIdException if the annotation does not exist
   * @throws RemoteException if some other error occured
   */
  public void setAnnotationState(String id, int state)
                          throws NoSuchIdException, RemoteException;

  /**
   * List the set of annotations in a specific state.
   *
   * @param state the state to filter the list of annotations by (eg. flagged for review)
   *
   * @return an array of id's; if no annotations have been defined, an empty array is returned
   *
   * @throws RemoteException if some error occured
   */
  public String[] listAnnotations(int state) throws RemoteException;
}
