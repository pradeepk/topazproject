/* $HeadURL::                                                                            $
* $Id$
*/
package org.plos;

import org.plos.annotation.service.AnnotationService;
import org.plos.annotation.web.CreateAnnotationAction;
import org.plos.annotation.web.DeleteAnnotationAction;
import org.plos.annotation.web.ListAnnotationAction;
import org.plos.annotation.web.ListReplyAction;
import org.plos.annotation.web.DeleteReplyAction;
import org.plos.annotation.web.CreateReplyAction;
import org.plos.annotation.web.BodyFetchAction;
import org.plos.annotation.web.GetAnnotationAction;
import org.plos.annotation.web.GetReplyAction;
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
  private CreateAnnotationAction createAnnotationAction;
  private DeleteAnnotationAction deleteAnnotationAction;
  private ListAnnotationAction listAnnotationAction;
  private AnnotationService annotationService;
  private ListReplyAction listReplyAction;
  private DeleteReplyAction deleteReplyAction;
  private CreateReplyAction createReplyAction;
  private BodyFetchAction bodyFetchAction;
  private GetAnnotationAction getAnnotationAction;
  private GetReplyAction getReplyAction;

  public BasePlosoneTestCase() {
    super();
  }

  public BasePlosoneTestCase(final String testName) {
    super(testName);
  }

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

  public CreateAnnotationAction getCreateAnnotationAction() {
    return createAnnotationAction;
  }

  public void setCreateAnnotationAction(final CreateAnnotationAction createAnnotationAnnotationAction) {
    this.createAnnotationAction = createAnnotationAnnotationAction;
  }

  public DeleteAnnotationAction getDeleteAnnotationAction() {
    return deleteAnnotationAction;
  }

  public void setDeleteAnnotationAction(final DeleteAnnotationAction deleteAnnotationAnnotationAction) {
    this.deleteAnnotationAction = deleteAnnotationAnnotationAction;
  }

  public ListAnnotationAction getListAnnotationAction() {
    return listAnnotationAction;
  }

  public void setListAnnotationAction(final ListAnnotationAction listAnnotationAnnotationAction) {
    this.listAnnotationAction = listAnnotationAnnotationAction;
  }

  public ListReplyAction getListReplyAction() {
    return listReplyAction;
  }

  public void setListReplyAction(final ListReplyAction listReplyAction) {
    this.listReplyAction = listReplyAction;
  }

  public DeleteReplyAction getDeleteReplyAction() {
    return deleteReplyAction;
  }

  public void setDeleteReplyAction(final DeleteReplyAction deleteReplyAction) {
    this.deleteReplyAction = deleteReplyAction;
  }

  public CreateReplyAction getCreateReplyAction() {
    return createReplyAction;
  }

  public void setCreateReplyAction(final CreateReplyAction createReplyAction) {
    this.createReplyAction = createReplyAction;
  }

  public void setAnnotationBodyFetcherAction(final BodyFetchAction bodyFetchAction) {
    this.bodyFetchAction = bodyFetchAction;
  }

  public BodyFetchAction getAnnotationBodyFetcherAction() {
    return bodyFetchAction;
  }

  public GetAnnotationAction getGetAnnotationAction() {
    return getAnnotationAction;
  }

  public void setGetAnnotationAction(final GetAnnotationAction getAnnotationAction) {
    this.getAnnotationAction = getAnnotationAction;
  }

  public GetReplyAction getGetReplyAction() {
    return getReplyAction;
  }

  public void setGetReplyAction(final GetReplyAction getReplyAction) {
    this.getReplyAction = getReplyAction;
  }
}
