/* $HeadURL::                                                                            $
* $Id$
*/
package org.plos;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.plos.article.service.ArticleService;
import org.plos.article.service.FetchArticleService;
import org.plos.article.web.FetchArticleAction;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;

public class BasePlosoneTestCase extends AbstractDependencyInjectionSpringContextTests {
  private FetchArticleService fetchArticleService;
  private ArticleService articleService;
  private FetchArticleAction fetchArticleAction;

  protected String[] getConfigLocations() {
    return new String[]{"testApplicationContext.xml"};
  }

  protected ArticleService getArticleService() throws MalformedURLException, ServiceException {
    return articleService;
  }

  public void setArticleService(final ArticleService articleService) {
    this.articleService = articleService;
  }

  public FetchArticleService getFetchArticleService() throws MalformedURLException, ServiceException {
    return fetchArticleService;
  }

  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  protected FetchArticleAction getFetchArticleAction() throws MalformedURLException, ServiceException {
    return fetchArticleAction;
  }

  public void setFetchArticleAction(final FetchArticleAction fetchArticleAction) {
    this.fetchArticleAction = fetchArticleAction;
  }
}
