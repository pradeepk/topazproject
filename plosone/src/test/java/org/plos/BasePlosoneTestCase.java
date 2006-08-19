/* $HeadURL::                                                                            $
* $Id$
*/
package org.plos;

import org.plos.annotation.service.AnnotationService;
import org.plos.annotation.web.CreateAction;
import org.plos.annotation.web.DeleteAction;
import org.plos.annotation.web.ListAction;
import org.plos.article.service.ArticleService;
import org.plos.article.service.FetchArticleService;
import org.plos.article.web.FetchArticleAction;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;

public abstract class BasePlosoneTestCase extends AbstractDependencyInjectionSpringContextTests {
  private FetchArticleService fetchArticleService;
  private ArticleService articleService;
  private FetchArticleAction fetchArticleAction;
  private CreateAction createAnnotationAction;
  private DeleteAction deleteAnnotationAction;
  private ListAction listAnnotationAction;
  private AnnotationService annotationService;

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

  public AnnotationService getAnnotationService() throws MalformedURLException, ServiceException {
    return annotationService;
  }

  public void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  public CreateAction getCreateAnnotationAction() {
    return createAnnotationAction;
  }

  public void setCreateAnnotationAction(final CreateAction createAnnotationAction) {
    this.createAnnotationAction = createAnnotationAction;
  }

  public DeleteAction getDeleteAnnotationAction() {
    return deleteAnnotationAction;
  }

  public void setDeleteAnnotationAction(final DeleteAction deleteAnnotationAction) {
    this.deleteAnnotationAction = deleteAnnotationAction;
  }

  public ListAction getListAnnotationAction() {
    return listAnnotationAction;
  }

  public void setListAnnotationAction(final ListAction listAnnotationAction) {
    this.listAnnotationAction = listAnnotationAction;
  }
}
