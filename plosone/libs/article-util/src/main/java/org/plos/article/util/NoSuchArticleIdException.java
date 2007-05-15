/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.util;

/**
 * Signifies that the article does not exist.
 *
 * @author Ronald Tschalär
 * @author Eric Brown
 */
public class NoSuchArticleIdException extends Exception {
  private final String id;

  /**
   * Create a new exception instance with a default exception message.
   *
   * @param id      the (non-existant) id
   */
  public NoSuchArticleIdException(String id) {
    this(id, "id = '" + id + "'");
  }

  /**
   * Create a new exception instance.
   *
   * @param id      the (non-existant) id
   * @param message the exception message
   */
  public NoSuchArticleIdException(String id, String message) {
    super(message);
    this.id = id;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }
}
