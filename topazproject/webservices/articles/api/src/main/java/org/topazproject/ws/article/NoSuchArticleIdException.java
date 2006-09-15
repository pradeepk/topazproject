/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article;

import org.topazproject.common.NoSuchIdException;

/** 
 * Signifies that the requested object does not exist. 
 * 
 * @author Ronald Tschalär
 */
public class NoSuchArticleIdException extends NoSuchIdException {
  public NoSuchArticleIdException(String id) {
    super(id);
  }
}
