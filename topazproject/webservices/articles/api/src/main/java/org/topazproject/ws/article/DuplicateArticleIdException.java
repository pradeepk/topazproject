/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article;

import org.topazproject.common.DuplicateIdException;

/** 
 * Signifies that an object with the requested id already exists.
 * 
 * @author Ronald Tschalär
 * @version $Id$
 */
public class DuplicateArticleIdException extends DuplicateIdException {
  public DuplicateArticleIdException(String id) {
    super(id);
  }
}
