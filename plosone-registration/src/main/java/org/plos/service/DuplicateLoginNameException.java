package org.plos.service;

import org.plos.ApplicationException;

/**
 * $HeadURL: $
 * @version: $Id: $
 */
public class DuplicateLoginNameException extends ApplicationException {
  public DuplicateLoginNameException() {
    super();
  }

  public DuplicateLoginNameException(final String message) {
    super(message);
  }

}
