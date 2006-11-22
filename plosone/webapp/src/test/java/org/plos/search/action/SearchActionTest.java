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
import org.plos.search.SearchUtil;
import org.plos.search.service.SearchHit;
import org.plos.util.FileUtils;

import java.io.File;
import java.util.Collection;

public class SearchActionTest extends BasePlosoneTestCase {
  public void testSimpleSearchShouldReturnSomething(){
    final SearchAction searchAction = getSearchAction();
    searchAction.setQuery("membrane");
    searchAction.setStartPage(0);
    searchAction.setPageSize(10);
    assertEquals(SUCCESS, searchAction.executeSimpleSearch());
    assertTrue(searchAction.getSearchResults().size() > 0);
  }

  public void testSearchObjectFormed() throws Exception {
    final String searchResultXml = "src/test/resources/searchResult.xml";

    final String text = FileUtils.getTextFromUrl(new File(searchResultXml).toURL().toString());
    final Collection<SearchHit> searchHits = SearchUtil.convertSearchResultXml(text);
    assertEquals(2, searchHits.size());
  }

}
