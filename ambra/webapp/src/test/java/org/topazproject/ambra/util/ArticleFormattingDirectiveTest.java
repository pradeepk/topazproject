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

package org.topazproject.ambra.util;

import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * @author Dragisa Krsmanovic
 */
public class ArticleFormattingDirectiveTest {

  @Test
  public void testItalic() throws Exception {
    Assert.assertEquals(ArticleFormattingDirective.format("foo<italic>bar</italic>"),
        "foo<i>bar</i>");
  }

  @Test
  public void testHtmlEncodedItalic() throws Exception {
    Assert.assertEquals(ArticleFormattingDirective.format("foo&lt;italic&gt;bar&lt;/italic&gt;"),
        "foo<i>bar</i>");
  }

  @Test
  public void testBold() throws Exception {
    Assert.assertEquals(ArticleFormattingDirective.format("foo<bold>bar</bold>"), "foo<b>bar</b>");
  }

  @Test
  public void testMonospace() throws Exception {
    Assert.assertEquals(ArticleFormattingDirective.format("foo<monospace>bar</monospace>"),
        "foo<span class=\"monospace\">bar</span>");
  }

  @Test
  public void testOverline() throws Exception {
    Assert.assertEquals(ArticleFormattingDirective.format("foo<overline>bar</overline>"),
        "foo<span class=\"overline\">bar</span>");
  }

  @Test
  public void testSmall() throws Exception {
    Assert.assertEquals(ArticleFormattingDirective.format("foo<sc>bar</sc>"),
        "foo<small>bar</small>");
  }

  @Test
  public void testStrike() throws Exception {
    Assert.assertEquals(ArticleFormattingDirective.format("foo<strike>bar</strike>"),
        "foo<s>bar</s>");
  }

  @Test
  public void testUnderline() throws Exception {
    Assert.assertEquals(ArticleFormattingDirective.format("foo<underline>bar</underline>"),
        "foo<u>bar</u>");
  }

  @Test
  public void testNamedContent() throws Exception {
    Assert.assertEquals(ArticleFormattingDirective.format("foo<named-content xmlns:xlink= " +
        "\"http://www.w3.org/1999/xlink\" content-type=\"genus-species\" xlink:type=\"simple\">" +
        "bar</named-content>"), "foo<i>bar</i>");
  }
}
