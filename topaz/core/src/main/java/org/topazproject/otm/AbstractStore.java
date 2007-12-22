/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

import java.util.ArrayList;
import java.util.List;

/**
 * A convenient base class for Stores.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractStore implements Store {

   private List<Store> childStores = new ArrayList<Store>();

   /*
    * inherited javadoc
    */
   public List<Store> getChildStores() {
    return childStores;
  }

}
