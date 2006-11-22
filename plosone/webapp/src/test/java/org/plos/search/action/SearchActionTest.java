/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search.action;

import static com.opensymphony.xwork.Action.SUCCESS;
import org.plos.BasePlosoneTestCase;

public class SearchActionTest extends BasePlosoneTestCase {
  public void testSimpleSearchShouldReturnSomething(){
    final SearchAction searchAction = getSearchAction();
    searchAction.setSimpleQuery("membrane");
    searchAction.setStartPage(1);
    searchAction.setPageSize(10);
    assertEquals(SUCCESS, searchAction.executeSimpleSearch());
    assertTrue(searchAction.getSearchResults().size() > 0);
  }

}
