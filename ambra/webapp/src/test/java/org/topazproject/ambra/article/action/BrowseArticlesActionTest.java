/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.ambra.article.action;


import static org.easymock.classextension.EasyMock.*;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.model.article.ArticleInfo;
import org.testng.annotations.*;
import static org.testng.Assert.*;

import java.util.ArrayList;

public class BrowseArticlesActionTest {

  @Test
  public void testExecuteForName() throws Exception {
    BrowseService browseService = createStrictMock(BrowseService.class);
    BrowseArticlesAction action = new BrowseArticlesAction();
    action.setCatName("name");
    action.setStartPage(5);
    action.setPageSize(7);
    action.setField("category");
    expect(browseService
        .getCategoryInfos())
        .andReturn(null);
    expect(browseService
        .getArticlesByCategory(eq("name"), eq(5), eq(7), aryEq(new int[1])))
        .andReturn(new ArrayList<ArticleInfo>());
    replay(browseService);
    action.setBrowseService(browseService);
    assertEquals( action.execute(), BaseActionSupport.SUCCESS);
    verify(browseService);
  }

}
