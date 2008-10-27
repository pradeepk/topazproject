/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.ambra.article.action;

import java.net.URI;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.annotation.service.ArticleAnnotationService;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.article.service.FetchArticleService;
import org.topazproject.ambra.article.service.NoSuchArticleIdException;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.model.article.ArticleInfo;
import org.topazproject.ambra.model.article.ArticleType;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.MinorCorrection;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

/**
 * Fetch article action.
 */
@SuppressWarnings("serial")
public class FetchArticleAction extends BaseActionSupport {
  private String articleURI;
  private String annotationId = "";

  private final ArrayList<String> messages = new ArrayList<String>();
  private static final Log log = LogFactory.getLog(FetchArticleAction.class);
  private BrowseService browseService;
  private FetchArticleService fetchArticleService;
  private JournalService journalService;
  private Set<Journal> journalList;
  private Article articleInfo;
  private ArticleType articleType;
  private ArticleInfo articleInfoX;
  private String transformedArticle;
  private final String articleTypeHeading = "Research Article"; // displayed article type (assigned default)
  private ArticleAnnotationService annotationService;
  private int numDiscussions = 0;
  private int numMinorCorrections = 0;
  private int numFormalCorrections = 0;
  /**
   * Represents the number of notes that are not corrections from  a UI stand point
   */
  private int numComments = 0;


  @Override
  @Transactional(readOnly = true)
  public String execute() {
    try {
      setTransformedArticle(fetchArticleService.getURIAsHTML(articleURI));

      ArticleAnnotation anns[] = annotationService.listAnnotations(articleURI, null);
      for (ArticleAnnotation a : anns) {
        if (a.getContext() == null) {
          numDiscussions ++;
        } else {
          if (a instanceof MinorCorrection) {
            numMinorCorrections++;
          } else if (a instanceof FormalCorrection) {
            numFormalCorrections++;
          } else {
            numComments++;
          }
        }
      }

      Article artInfo = fetchArticleService.getArticleInfo(articleURI);

      setArticleInfo(artInfo);

      articleType = ArticleType.getDefaultArticleType();
      for (URI artTypeUri : artInfo.getArticleType()) {
        if (ArticleType.getKnownArticleTypeForURI(artTypeUri)!= null) {
          articleType = ArticleType.getKnownArticleTypeForURI(artTypeUri);
          break;
        }
      }

      // get the alternate ArticleInfo, e.g. contains RelatedArticles
      articleInfoX = browseService.getArticleInfo(URI.create(articleURI));

      journalList  = journalService.getJournalsForObject(URI.create(articleURI));
    } catch (NoSuchArticleIdException e) {
      messages.add("No article found for id: " + articleURI);
      log.info("Could not find article: " + articleURI, e);
      return ERROR;
    } catch (Exception e) {
      messages.add(e.getMessage());
      log.error("Error retrieving article: " + articleURI, e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * A struts Action method used to display the annotated article. 
   * The transformedArticle field is populated with the annotated articleURI content. 
   * 
   * @return Annotated Article XML String
   */
  @Transactional(readOnly = true)
  public String displayAnnotatedArticle() {
    try {
      setTransformedArticle(fetchArticleService.getAnnotatedContent(articleURI));
    } catch (Exception e) {
      log.error ("Could not get annotated article:" + articleURI, e);
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
   * @return articleURI
   */
  @RequiredStringValidator(message = "Article URI is required.")
  public String getArticleURI() {
    return articleURI;
  }

  /**
   * Set articleURI to fetch the article for.
   * @param articleURI articleURI
   */
  public void setArticleURI(final String articleURI) {
    this.articleURI = articleURI;
  }

  public ArrayList<String> getMessages() {
    return messages;
  }

  /**
   * @return Returns the annotationId.
   */
  public String getAnnotationId() {
    return annotationId;
  }

  /**
   * @param annotationId The annotationId to set.
   */
  public void setAnnotationId(String annotationId) {
    this.annotationId = annotationId;
  }

  /**
   * @return Returns the annotationService.
   */
  public ArticleAnnotationService getAnnotationService() {
    return annotationService;
  }

  /**
   * @param annotationService The annotationService to set.
   */
  public void setAnnotationService(ArticleAnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * @return Returns the numDiscussions.
   */
  public int getNumDiscussions() {
    return numDiscussions;
  }

  /**
   * @return Returns the numComments.
   */
  public int getNumComments() {
    return numComments;
  }

  /**
   * @return Returns the numMinorCorrections.
   */
  public int getNumMinorCorrections() {
    return numMinorCorrections;
  }

  /**
   * @return Returns the numMinorCorrections.
   */
  public int getNumFormalCorrections() {
    return numFormalCorrections;
  }

  /**
   * @return Returns the calculated number of notes.
   */
  public int getNumNotes() {
    return numComments + numMinorCorrections + numFormalCorrections;
  }

  /**
   * @return Returns the total number of corrections.
   */
  public int getNumCorrections() {
    return numMinorCorrections + numFormalCorrections;
  }

  /**
   * Return the ArticleInfo from the Browse cache.
   *
   * TODO: convert all usages of "articleInfo" (ObjectInfo) to use the Browse cache version of
   * ArticleInfo.  Note that for all templates to use ArticleInfo, it will have to
   * be enhanced.  articleInfo & articleInfoX are both present, for now, to support:
   *   - existing templates/services w/o a large conversion
   *   - access to RelatedArticles
   *
   * @return Returns the articleInfoX.
   */
  public ArticleInfo getArticleInfoX() {
    return articleInfoX;
  }

  /**
   * @return Returns the articleInfo.
   */
  public Article getArticleInfo() {
    return articleInfo;
  }

  /**
   * The article type displayed by article_content.ftl as per #693
   *
   * @return the article type heading
   */
  public String getArticleTypeHeading() {
    return articleTypeHeading;
  }

  /**
   * @param articleInfo The articleInfo to set.
   */
  private void setArticleInfo(Article articleInfo) {
    this.articleInfo = articleInfo;
  }

  /**
   * @param journalService The journalService to set.
   */
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * @return Returns the journalList.
   */
  public Set<Journal> getJournalList() {
    return journalList;
  }

  /**
   * @param browseService The browseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }

  public ArticleType getArticleType() {
    return articleType;
  }
}
