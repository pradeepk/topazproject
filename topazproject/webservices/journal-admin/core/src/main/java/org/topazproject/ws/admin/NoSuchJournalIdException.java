/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.admin;

// TODO: Subclass NoSuchIdException -- Doing so at the moment breaks journal-admin integraiton-tests
//import org.topazproject.common.NoSuchIdException;

/** 
 * Signifies that the requested object does not exist. 
 * 
 * @author Ronald Tschalär
 */
public class NoSuchJournalIdException extends Exception {
  private final String id;

  /** 
   * Create a new exception instance. 
   * 
   * @param id  the (non-existant) id
   */
  public NoSuchJournalIdException(String id) {
    this.id = id;
  }

  /** 
   * @return the (non-existant) id
   */
  public String getId() {
    return id;
  }
}
