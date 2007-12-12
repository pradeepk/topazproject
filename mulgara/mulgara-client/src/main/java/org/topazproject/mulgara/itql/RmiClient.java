/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.itql;

import org.mulgara.itql.ItqlInterpreterBean;

/** 
 * A mulgara client using RMI.
 *
 * @author Ronald Tschalär
 */
class RmiClient extends IIBClient {
  /** 
   * Create a new instance pointed at the given database.
   * 
   * @param database  the url of the database
   */
  public RmiClient(String database) {
    super(new ItqlInterpreterBean());
    iib.setServerURI(database);
  }
}
