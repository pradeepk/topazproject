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

import java.rmi.RemoteException;

/**
 * Annotation related operations.
 *
 * @author Pradeep Krishnan
 */
public interface Annotations {
  /**
   * Creates a new annotation.
   *
   * @param mediator an entity that mediates access to the created annotation. Can be used by an
   *        application to identify the annotations it created. Defined by
   *        <code>http://purl.org/dc/terms/mediator</code>. May be <code>null</code>.
   * @param type An annotation type or <code>null</code>. The different types of annotations
   *        defined in <code>http://www.w3.org/2000/10/annotationType#</code> are:
   *        <ul><li><code>http://www.w3.org/2000/10/annotationType#Advice</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#Change</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#Comment</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#Example</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#Explantion</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#Question</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#SeeAlso</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#Annotation</code></li> </ul>
   *        Defaults to <code>http://www.w3.org/2000/10/annotationType#Annotation</code>
   * @param annotates the resource to which this annotation applies. Defined by
   *        <code>http://www.w3.org/2000/10/annotation-ns#annotates</code> and the inverse
   *        <code>http://www.w3.org/2000/10/annotation-ns#hasAnnotation</code>. Must be a valid
   *        <code>URI</code>.
   * @param context the context within the resource named in <code>annotates</code> to which this
   *        annotation applies or <code>null</code>. Defined by
   *        <code>http://www.w3.org/2000/10/annotation-ns#context</code>
   * @param supersedes the annotation that this supersedes or <code>null</code>. Defined by
   *        <code>http://purl.org/dc/terms/replaces</code> and the inverse relation
   *        <code>http://purl.org/dc/terms/isReplacedBy</code> both of which are sub properties of
   *        <code>http://purl.org/dc/elements/1.1/relation</code>. Defaults to
   *        <code>http://www.w3.org/1999/02/22-rdf-syntax-ns#nil</code>
   * @param title the annotation title or <code>null</code>. Defined by
   *        <code>http://purl.org/dc/elements/1.1/title</code>
   * @param body the resource representing the content of an annotation. Defined by
   *        <code>http://www.w3.org/2000/10/annotation-ns#body</code>, a sub property of
   *        <code>http://www.w3.org/2000/10/annotation-ns#related</code>. Must be a valid
   *        <code>URL</code>
   *
   * @return Returns a unique identifier for the newly created annotation
   *
   * @throws NoSuchIdException if <code>supersedes</code> is not a valid annotation id
   * @throws RemoteException if some other error occured
   */
  public String createAnnotation(String mediator, String type, String annotates, String context,
                                 String supersedes, String title, String body)
                          throws NoSuchIdException, RemoteException;

  /**
   * Creates a new annotation. A new resource URL is created for the annotation body from the
   * supplied content.
   *
   * @param mediator an entity that mediates access to the created annotation. (eg. an app-id)
   * @param type An annotation type or <code>null</code>.
   * @param annotates the resource to which this annotation applies.
   * @param context the context within the resource named in <code>annotates</code> to which this
   *        annotation applies or <code>null</code>.
   * @param supersedes the annotation that this supersedes or <code>null</code>.
   * @param title the annotation title or <code>null</code>
   * @param contentType the mime-type and optionally the character encoding of the annotation body.
   *        eg. <code>text/html;charset=utf-8</code>, <code>text/plain;charset=iso-8859-1</code>,
   *        <code>text/plain</code> etc.
   * @param content the annotation body content in the character encoding specified. If no
   *        character encoding is specified the interpretation will be left upto the client that
   *        later retrieves the annotation body.
   *
   * @return Returns a unique identifier for the newly created annotation
   *
   * @throws NoSuchIdException if <code>supersedes</code> is not a valid annotation id
   * @throws RemoteException if some other error occured
   */
  public String createAnnotation(String mediator, String type, String annotates, String context,
                                 String supersedes, String title, String contentType, byte[] content)
                          throws NoSuchIdException, RemoteException;

  /**
   * Deletes an annotation. Deletes all triples for which this annotation is the subject.
   * Additionally if the <code>deletePreceding</code> is <code>true</code>  then all preceding
   * annotations to this are deleted.
   *
   * @param id the id of the annotation to remove
   * @param deletePreceding whether to delete all annotations that are supersed by this annotation.
   *
   * @throws NoSuchIdException if the annotation does not exist
   * @throws RemoteException if some other error occured
   */
  public void deleteAnnotation(String id, boolean deletePreceding)
                        throws NoSuchIdException, RemoteException;

  /**
   * Retrieve the annotation meta-data. Note that there may be other annotations that supersede
   * this. To always get the latest version(s), use {@link #getLatestAnnotations}.
   *
   * @param id the id of the annotation for which to get the meta-data
   *
   * @return Returns the annotation meta data as an xml document.
   *
   * @throws NoSuchIdException if the annotation does not exist
   * @throws RemoteException if some other error occured
   */
  public AnnotationInfo getAnnotationInfo(String id) throws NoSuchIdException, RemoteException;

  /**
   * Gets the latest version(s) of this annotation. The latest version(s) are the ones that are not
   * superseded by other annotations and therefore could just be this annotation itself.
   *
   * @param id the annotation id.
   *
   * @return an array of annotation ids or metadata; the array will atleast contain one element
   *
   * @throws NoSuchIdException if the annotation does not exist
   * @throws RemoteException if an error occured
   */
  public AnnotationInfo[] getLatestAnnotations(String id)
                                        throws NoSuchIdException, RemoteException;

  /**
   * Gets the set of annotations of the given type on a resource. Matching annotations are further
   * filtered out if they are superseded by other annotations or if they are in an administrator
   * review state. Note that this returns only those annotations that the caller has permissions
   * to view.
   *
   * @param mediator if present only those annotations that match this mediator are returned
   * @param annotates the resource for which annotations are to be looked-up
   * @param type the annotation type to use in filtering the annotations or <code>null</code>  to
   *        include all
   *
   * @return an array of annotation ids or metadata for matching annotations; if no annotations
   *         have been defined, an empty array is returned
   *
   * @throws RemoteException if an error occured
   */
  public AnnotationInfo[] listAnnotations(String mediator, String annotates, String type)
                                   throws RemoteException;

  /**
   * Gets the chain of annotations that precede this to give a history of changes.
   *
   * @param id the annotation id
   *
   * @return an array of annotation ids or metadata; if this annotation does not supersede any
   *         other annotation, then an empty array is returned
   *
   * @throws NoSuchIdException if the annotation does not exist
   * @throws RemoteException if an error occured
   */
  public AnnotationInfo[] getPrecedingAnnotations(String id)
                                           throws NoSuchIdException, RemoteException;

  /**
   * Sets the administrative state of an annotation. (eg. flagged for review)
   *
   * @param id the annotation id
   * @param state the new state or 0 to take the annotation out of an administrator state
   *
   * @throws NoSuchIdException if the annotation does not exist
   * @throws RemoteException if some other error occured
   */
  public void setAnnotationState(String id, int state)
                          throws NoSuchIdException, RemoteException;

  /**
   * List the set of annotations in a specific administrative state.
   *
   * @param mediator if present only those annotations that match this mediator are returned
   * @param state the state to filter the list of annotations by or 0 to return annotations in any
   *        administartive state
   *
   * @return an array of id's; if no matching annotations are found, an empty array is returned
   *
   * @throws RemoteException if some error occured
   */
  public String[] listAnnotations(String mediator, int state)
                           throws RemoteException;
}
