/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.article.service.FetchArticleService;

import java.util.ArrayList;
import java.io.StringWriter;

/**
 * Fetch article action. 
 */
public class FetchArticleAction extends ActionSupport {
  private String articleDOI;
  private static final int TRANSFORMED_XML_FILE_SIZE = 1000000;

  private ArrayList<String> messages = new ArrayList<String>();
  private static final Log log = LogFactory.getLog(FetchArticleAction.class);
  private FetchArticleService fetchArticleService;
  private String transformedArticle;

  public String execute() throws Exception {
    try {
      final StringWriter stringWriter = new StringWriter(TRANSFORMED_XML_FILE_SIZE);
      fetchArticleService.getDOIAsHTML(articleDOI, stringWriter);
      setTransformedArticle(stringWriter.toString());
    } catch (Exception e) {
      messages.add(e.getMessage());
      log.error(e, e);
      return ERROR;
    }

    return SUCCESS;
  }

  public String getTransformedArticle() {
    return transformedArticle;
  }

  private void setTransformedArticle(final String transformedArticle) {
    this.transformedArticle = transformedArticle;
  }

  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  /**
   * @return articleDOI
   */
  @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "articleDOI", message = "Article DOI is required.")
  public String getArticleDOI() {
    return articleDOI;
  }

  /**
   * Set articleDOI.
   *
   * @param articleDOI articleDOI
   */
  public void setArticleDOI(final String articleDOI) {
    this.articleDOI = articleDOI;
  }

  public ArrayList<String> getMessages() {
    return messages;
  }
}
