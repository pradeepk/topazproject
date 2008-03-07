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

import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.ehcache.Ehcache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.annotation.service.Reply;
import org.plos.annotation.service.WebAnnotation;
import org.plos.article.action.CreateCitation;
import org.plos.article.service.CitationInfo;
import org.plos.article.service.FetchArticleService;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.models.Article;
import org.plos.util.ArticleXMLUtils;
import org.plos.util.CacheAdminHelper;
import org.plos.util.CitationUtils;
import org.springframework.beans.factory.annotation.Required;
import org.xml.sax.SAXException;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.thoughtworks.xstream.XStream;

/**
 * Action class to get a list of replies to annotations.
 */
@SuppressWarnings("serial")
public class ListReplyAction extends AnnotationActionSupport {

  private FetchArticleService fetchArticleService;

  private String root;
  private String inReplyTo;
  private Reply[] replies;
  private WebAnnotation baseAnnotation;
  private Article articleInfo;
  private ArticleXMLUtils citationService;
  private Ehcache articleAnnotationCache;
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
      articleInfo = fetchArticleService.getArticleInfo(baseAnnotation.getAnnotates());

      // construct citation string
      // we're only showing annotation citations for formal corrections
      if(baseAnnotation.isFormalCorrection()) {
        // lock @ Article level
        final Object lock = (FetchArticleService.ARTICLE_LOCK + articleURI).intern();
        CitationInfo result = CacheAdminHelper.getFromCacheE(articleAnnotationCache,
                CreateCitation.CITATION_KEY + articleURI, -1, lock, "citation",
                new CacheAdminHelper.EhcacheUpdaterE<CitationInfo, ApplicationException>() {
                  public CitationInfo lookup() throws ApplicationException {

                    XStream xstream = new XStream();
                    try {
                      return (CitationInfo) xstream.fromXML(
                              citationService.getTransformedArticle(articleURI.toString()));
                    } catch (IOException ie) {
                      throw new ApplicationException(ie);
                    } catch (NoSuchArticleIdException nsaie) {
                      throw new ApplicationException(nsaie);
                    } catch (ParserConfigurationException pce) {
                      throw new ApplicationException(pce);
                    } catch (SAXException se) {
                      throw new ApplicationException(se);
                    }
            }
        });
        citation = CitationUtils.generateArticleCorrectionCitationString(result, baseAnnotation);
      }
    } catch (ApplicationException ae) {
      citation = null;
      log.error("Could not list all replies for root: " + root, ae);
      addActionError("Reply fetching failed with error message: " + ae.getMessage());
      return ERROR;
    }

    return SUCCESS;
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
   * @param fetchArticleService The fetchArticleService to set.
   */
  @Required
  public void setFetchArticleService(FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
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
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *   to use.
   */
  @Required
  public void setArticleAnnotationCache(Ehcache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
  }

  /**
   * @param citationService The citationService to set.
   */
  @Required
  public void setCitationService(ArticleXMLUtils citationService) {
    this.citationService = citationService;
  }

}
