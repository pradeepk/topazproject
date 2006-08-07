/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * Fetch article action. 
 */
public class FetchArticleAction extends ActionSupport {
  private String articleDOI;

  private ArrayList<String> messages = new ArrayList<String>();
  private static final Log log = LogFactory.getLog(FetchArticleAction.class);
  private FetchArticleService fetchArticleService;

  public String execute() throws Exception {
    try {
      fetchArticleService.getDOIAsHTML(articleDOI);
    } catch (Exception e) {
      messages.add(e.getMessage());
      log.error(e, e);
      return ERROR;
    }

    return SUCCESS;
  }

  public void setFetchArticleService(FetchArticleService fetchArticleService) {
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
