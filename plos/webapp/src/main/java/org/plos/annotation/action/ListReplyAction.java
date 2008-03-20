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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.annotation.service.Reply;
import org.plos.annotation.service.WebAnnotation;
import org.plos.article.service.ArticleOtmService;
import org.plos.journal.JournalService;
import org.plos.models.Article;
import org.plos.models.Journal;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.util.TransactionHelper;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

/**
 * Action class to get a list of replies to annotations.
 */
@SuppressWarnings("serial")
public class ListReplyAction extends AnnotationActionSupport {

  /**
   * Date format used in constructing an annotation citation string
   * <p>
   * <code>d MMM yyyy</code>
   * <p>
   * E.g.: "5 Jan 2008"
   */
  private static final DateFormat annotationCitationDateFormat = new SimpleDateFormat("d MMM yyyy");

  private String root;
  private String inReplyTo;
  private Reply[] replies;
  private Session session;
  private WebAnnotation baseAnnotation;
  private ArticleOtmService articleOtmService;
  private JournalService journalService;
  private Article articleInfo;
  private String citation;
  private Set<Journal> journalList;

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
   * List all the replies for a given root and inRelyTo in a threaded tree
   * structure.
   * 
   * @return webwork status for the call
   * @throws Exception Exception
   */
  public String listAllReplies() throws Exception {
    try {
      // Allow a single 'root' param to be accepted. If 'inReplyTo' is null or
      // empty string, set to root value.
      // This results in that single annotation being displayed.
      if ((inReplyTo == null) || inReplyTo.length() == 0) {
        inReplyTo = root;
      }
      if (log.isDebugEnabled()) {
        log.debug("listing all Replies for root: " + root);
      }
      baseAnnotation = getAnnotationService().getAnnotation(root);
      replies = getAnnotationService().listAllReplies(root, inReplyTo);
      final URI articleURI = new URI(baseAnnotation.getAnnotates());
      articleInfo = getArticleOtmService().getArticle(articleURI);

      TransactionHelper.doInTx(session, new TransactionHelper.Action<Void>() {
        public Void run(Transaction tx) {
          journalList = journalService.getJournalsForObject(articleURI);
          return null;
        }
      });

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
   * Assembles a String representing an annotation citatation based on a
   * prescribed format.
   * <p>
   * FORMAT: <blockquote>{Author of comment}. {Comment Title}. Online comment.
   * {Date Comment Posted}. {Title of Original Article}. {Full name of first
   * author of original article} et al. {Title of Journal}. {annotation URL}</blockquote>
   * 
   * @return A newly created String.
   * @see http://wiki.plos.org/pmwiki.php/Topaz/Corrections for the format
   *      specification
   */
  private String assembleCitationString() {
    assert baseAnnotation != null;
    assert articleInfo != null;

    StringBuffer sb = new StringBuffer(1024);

    // author of comment
    sb.append(baseAnnotation.getCreatorName());
    sb.append(". ");

    // comment title
    sb.append(baseAnnotation.getCommentTitle());
    sb.append(". Online comment.  ");

    // comment post date
    synchronized (annotationCitationDateFormat) {
      sb.append(annotationCitationDateFormat.format(baseAnnotation.getCreatedAsDate()));
    }
    sb.append(".  ");

    // original article title
    sb.append(articleInfo.getDublinCore().getTitle());
    sb.append(".  ");

    // first author name et al.
    // NOTE presuming the creator list, which is a Set, is ordered
    // whereby the initial element is the "primary" author
    try {
      sb.append(articleInfo.getDublinCore().getCreators().iterator().next());
    } catch (Throwable t) {
      sb.append("-Unknown Author(s)-");
    }
    sb.append(" et al.  ");

    // [primary] owning journal title
    // NOTE presuming the journal list, which is a Set, is ordered
    // whereby the initial element is the "primary" journal
    try {
      sb.append(journalList.iterator().next().getDublinCore().getTitle());
    } catch (Throwable t) {
      sb.append("-Unknown Journal(s)-");
    }
    sb.append(".  ");

    // annotation URI
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

  /**
   * @param session The session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * @param journalService The journalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }
}
