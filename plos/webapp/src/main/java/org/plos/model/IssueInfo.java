package org.plos.model;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.plos.model.article.ArticleInfo;


/**
 * The info about a single Issue that the UI needs.
 */
public class IssueInfo implements Serializable {

  private URI          id;
  private String       displayName;
  private URI          prevIssue;
  private URI          nextIssue;
  private URI          imageArticle;
  private String       description;
  private List<ArticleInfo> articlesInIssue = new ArrayList<ArticleInfo>();

  // XXX TODO, List<URI> w/Article DOI vs. List<ArticleInfo>???

  public IssueInfo(URI id, String displayName, URI prevIssue, URI nextIssue,
                   URI imageArticle, String description) {

    this.id = id;
    this.displayName = displayName;
    this.prevIssue = prevIssue;
    this.nextIssue = nextIssue;
    this.imageArticle = imageArticle;
    this.description = description;
  }

  public void addArticleToIssue(ArticleInfo article) {
    articlesInIssue.add(article);
  }
  
  /**
   * Get the id.
   *
   * @return the id.
   */
  public URI getId() {
    return id;
  }

  /**
   * Get the displayName.
   *
   * @return the displayName.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Get the previous Issue.
   *
   * @return the previous Issue.
   */
  public URI getPrevIssue() {
    return prevIssue;
  }

  /**
   * Get the next Issue.
   *
   * @return the next Issue.
   */
  public URI getNextIssue() {
    return nextIssue;
  }

  /**
   * Get the image Article DOI.
   *
   * @return the image Article DOI.
   */
  public URI getImageArticle() {
    return imageArticle;
  }

  /**
   * Get the description.
   *
   * @return the description.
   */
  public String getDescription() {
    return description;
  }

  public List<ArticleInfo> getArticlesInIssue() {
    return articlesInIssue;
  }
  
}