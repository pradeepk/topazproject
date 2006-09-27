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
import org.topazproject.common.NoSuchIdException;

import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.io.StringWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.net.MalformedURLException;

/**
 * Fetch article action. 
 */
public class FetchArticleAction extends ActionSupport {
  private String articleDOI;
  private static final int INITIAL_TRANSFORMED_FILE_SIZE = 100000;

  private ArrayList<String> messages = new ArrayList<String>();
  private static final Log log = LogFactory.getLog(FetchArticleAction.class);
  private FetchArticleService fetchArticleService;
  private String transformedArticle;

  public String execute() throws Exception {
    try {
      final StringWriter stringWriter = new StringWriter(INITIAL_TRANSFORMED_FILE_SIZE);
      fetchArticleService.getDOIAsHTML(articleDOI, stringWriter);
      setTransformedArticle(stringWriter.toString());
    } catch (NoSuchIdException e) {
      messages.add("No article found for id: " + articleDOI);
      log.warn(e, e);
      return ERROR;
    } catch (TransformerException e) {
      messages.add("Transforming error: " + e.getMessage());
      log.error(e, e);
      return ERROR;
    } catch (RemoteException e) {
      messages.add(e.getMessage());
      log.warn(e, e);
      return ERROR;
    } catch (MalformedURLException e) {
      messages.add("Incorrect id syntax: " + e.getMessage());
      log.warn(e, e);
      return ERROR;
    } catch (FileNotFoundException e) {
      messages.add("File not found exception: " + e.getMessage());
      log.error(e, e);
      return ERROR;
    } catch (IOException e) {
      messages.add(e.getMessage());
      log.error(e, e);
      return ERROR;
    }

    return SUCCESS;
  }

  /**
   * @return transformed output
   */
  public String getTransformedArticle() {
    return transformedArticle;
  }

  private void setTransformedArticle(final String transformedArticle) {
    this.transformedArticle = transformedArticle;
  }

  /** Set the fetch article service
   * @param fetchArticleService fetchArticleService
   */
  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  /**
   * @return articleDOI
   */
  @RequiredStringValidator(message = "Article DOI is required.")
  public String getArticleDOI() {
    return articleDOI;
  }

  /**
   * Set articleDOI to fetch the article for.
   * @param articleDOI articleDOI
   */
  public void setArticleDOI(final String articleDOI) {
    this.articleDOI = articleDOI;
  }

  public ArrayList<String> getMessages() {
    return messages;
  }
}
