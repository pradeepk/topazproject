package org.plos.service;

/**
 * Used to indicate that the verification token is invalid.
 * $HeadURL: $
 * @version: $Id: $
 */
public class VerificationTokenInvalidException extends Exception {
  /**
   * Constructor with message.
   * @param message
   */
  public VerificationTokenInvalidException(final String message) {
    super(message);
  }
}
