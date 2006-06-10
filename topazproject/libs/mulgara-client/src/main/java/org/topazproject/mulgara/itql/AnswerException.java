
package org.topazproject.mulgara.itql;

/**
 * Represents a problem parsing the response from the query or building the answer.
 * 
 * @author Ronald Tschalär
 */
public class AnswerException extends Exception {
  /** 
   * Create a new instance with the given error message. 
   * 
   * @param msg the error message
   */
  public AnswerException(String msg) {
    super(msg);
  }

  /** 
   * Create a new instance with the given error message and underlying exception. 
   * 
   * @param msg   the error message
   * @param cause the cause for this exception
   */
  public AnswerException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
