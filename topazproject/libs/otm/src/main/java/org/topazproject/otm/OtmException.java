
package org.topazproject.otm;

/** 
 * The base exception class for all exceptions thrown by the OTM layer. 
 * 
 * @author Ronald Tschalär
 */
public class OtmException extends RuntimeException {
  /** 
   * Create a new exception instance with a message. 
   * 
   * @param msg the details about the exception
   */
  public OtmException(String msg) {
    super(msg);
  }

  /** 
   * Create a new chained exception instance with a message. 
   * 
   * @param msg   the details about the exception
   * @param cause the underlying exception that caused this exception
   */
  public OtmException(String msg, Throwable cause) {
    super(msg);
    initCause(cause);
  }
}
