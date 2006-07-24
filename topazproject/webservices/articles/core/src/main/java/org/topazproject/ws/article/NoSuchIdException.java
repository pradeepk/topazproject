
package org.topazproject.ws.article;

/** 
 * Signifies that the requested object does not exist. 
 * 
 * @author Ronald Tschalär
 */
public class NoSuchIdException extends Exception {
  private final String id;

  /** 
   * Create a new exception instance. 
   * 
   * @param id  the (non-existant) id
   */
  public NoSuchIdException(String id) {
    this.id = id;
  }

  /** 
   * @return the (non-existant) id
   */
  public String getId() {
    return id;
  }
}
