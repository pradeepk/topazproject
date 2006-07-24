
package org.topazproject.ws.article;

/** 
 * Signifies that an object with the requested id already exists.
 * 
 * @author Ronald Tschalär
 */
public class DuplicateIdException extends Exception {
  private final String id;

  /** 
   * Create a new exception instance. 
   * 
   * @param id  the (duplicate) id
   */
  public DuplicateIdException(String id) {
    this.id = id;
  }

  /** 
   * @return the (duplicate) id
   */
  public String getId() {
    return id;
  }
}
