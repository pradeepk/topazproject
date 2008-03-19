/* $HeadURL::                                                                            $
 * $Id:ListReplyAction.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.action;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.annotation.service.Reply;
import org.plos.annotation.service.WebAnnotation;
import org.plos.article.service.ArticleOtmService;
import org.plos.models.Article;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

/**
 * Action class to get a list of replies to annotations.
 */
public class ListReplyAction extends AnnotationActionSupport {
  private String root;
  private String inReplyTo;
  private Reply[] replies;
  private WebAnnotation baseAnnotation;
  private ArticleOtmService articleOtmService;
  private Article articleInfo;
  private String citation;

  private static final Log log = LogFactory.getLog(ListReplyAction.class);

  @Override
  public String execute() throws Exception {
    try {
      replies = getAnnotationService().listReplies(root, inReplyTo);
    } catch (final ApplicationException e) {
      log.error("ListReplyAction.execute() failed for root: " + root, e);
      addActionError("Reply fetching failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * List all the replies for a given root and inRelyTo in a threaded tree structure.
   * @return webwork status for the call
   * @throws Exception Exception
   */
  public String listAllReplies() throws Exception {
    try {
      // Allow a single 'root' param to be accepted. If 'inReplyTo' is null or empty string, set to root value. 
      // This results in that single annotation being displayed. 
      if ((inReplyTo == null) || inReplyTo.length() == 0) {
        inReplyTo = root;
      }
      if (log.isDebugEnabled()){
        log.debug("listing all Replies for root: " + root);
      }
      baseAnnotation = getAnnotationService().getAnnotation(root);
      replies = getAnnotationService().listAllReplies(root, inReplyTo);
      articleInfo = getArticleOtmService().getArticle(new URI(baseAnnotation.getAnnotates()));
      
      // construct citation string
      citation = assembleCitationString();
      
    } catch (final ApplicationException e) {
      log.error("Could not list all replies for root:" + root, e);
      addActionError("Reply fetching failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }
  
  /**
   * Assemble the Correction annotation citation string.
   * <p>FORMAT:
   * <p>[Author of comment]. "[Comment Title]." Online comment. [Date Comment Posted]. "[Title of Original Article]." [Full name of first author of original article] et al. [Title of Journal]. {URL}
   * @return Correction annotation citation string.
   */
  private String assembleCitationString() {
    assert baseAnnotation != null;
    assert articleInfo != null;
    StringBuffer sb = new StringBuffer(1024);
    sb.append(baseAnnotation.getCreatorName());
    sb.append(". ");
    sb.append(baseAnnotation.getCommentTitle());
    sb.append(". Online comment.  ");
    sb.append(baseAnnotation.getCreated());
    sb.append(".  ");
    sb.append(articleInfo.getDublinCore().getTitle());
    sb.append(".  ");
    sb.append(articleInfo.getDublinCore().getCreators().iterator().next()); 
    sb.append(" et al.  ");
    sb.append("[TODO Title of Journal].  ");
    sb.append(baseAnnotation.getId());
    return sb.toString();
  }
  
  /**
   * @return The constructed annotation citation string.
   */
  public String getCitation() {
    return citation;
  }

  public void setRoot(final String root) {
    this.root = root;
  }

  public void setInReplyTo(final String inReplyTo) {
    this.inReplyTo = inReplyTo;
  }

  public Reply[] getReplies() {
    return replies;
  }

  @RequiredStringValidator(message = "root is required")
  public String getRoot() {
    return root;
  }

  public String getInReplyTo() {
    return inReplyTo;
  }

  /**
   * @return Returns the baseAnnotation.
   */
  public WebAnnotation getBaseAnnotation() {
    return baseAnnotation;
  }

  /**
   * @param baseAnnotation The baseAnnotation to set.
   */
  public void setBaseAnnotation(WebAnnotation baseAnnotation) {
    this.baseAnnotation = baseAnnotation;
  }

  /**
   * @return Returns the articleOtmService.
   */
  public ArticleOtmService getArticleOtmService() {
    return articleOtmService;
  }

  /**
   * @param articleOtmService The articleOtmService to set.
   */
  public void setArticleOtmService(ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * @return Returns the articleInfo.
   */
  public Article getArticleInfo() {
    return articleInfo;
  }

  /**
   * @param articleInfo The articleInfo to set.
   */
  public void setArticleInfo(Article articleInfo) {
    this.articleInfo = articleInfo;
  }
}
