/* $$HeadURL:: $
 * $$Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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

package org.topazproject.ambra.admin.action;

import org.topazproject.ambra.models.Issue;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.article.action.TOCArticleGroup;
import org.topazproject.ambra.admin.service.AdminService;
import org.topazproject.ambra.admin.service.AdminService.JournalInfo;
import org.topazproject.ambra.model.article.ArticleInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.ArrayList;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public class IssueManagementAction extends BaseAdminActionSupport {

  // Fields set by templates
  private String    command;
  private URI       volumeURI;
  private URI       issueURI;
  private String    articleCSVURIs;
  private URI       imageURI;
  private String    articleListCSV;
  private String    displayName;
  private Boolean   respectOrder = false;
  private List<URI> articlesToRemove = new ArrayList<URI>();

  // Fields Used by template
  private JournalInfo   journalInfo;
  private Issue         issue;
  private String        articleOrderCSV;
  private List<URI>     orphans;
  private List<TOCArticleGroup> articleGroups = new ArrayList<TOCArticleGroup>();

  // Necessary Services
  private AdminService  adminService;
  private BrowseService browseService;

  private static final Log log = LogFactory.getLog(IssueManagementAction.class);
 /**
  *
  */
  public enum IM_COMMANDS {
    ADD_ARTICLE,
    REMOVE_ARTICLES,
    UPDATE_ISSUE,
    INVALID;

    /**
     * Convert a string specifying an action to its
     * enumerated equivalent.
     *
     * @param command  string value to convert.
     * @return        enumerated equivalent
     */
    public static IM_COMMANDS toCommand(String command) {
      IM_COMMANDS a;
      try {
        a = valueOf(command);
      } catch (Exception e) {
        // It's ok just return invalid.
        a = INVALID;
      }
      return a;
    }
  }

  /**
   * Main entry porint for Issue management action.
   */
  @Override
  @Transactional(rollbackFor = { Throwable.class })
  public String execute() throws Exception  {

    switch(IM_COMMANDS.toCommand(command)) {

      case ADD_ARTICLE: {
        try {
          issue = adminService.getIssue(issueURI);
          List<URI> articleURIs = adminService.parseCSV(articleCSVURIs);

          for(URI articleURI : articleURIs) {
            issue = adminService.addArticle(issue, articleURI);
            addActionMessage("Added Article: " + articleURI);
          }
          adminService.flushStore();
        } catch (Exception e) {
          addActionMessage("Article not added due to the following error.");
          addActionMessage(e.toString());
          log.error("Add Article to Issue Failed.", e);
        }
        break;
      }

      case REMOVE_ARTICLES: {
        try {
          issue = adminService.getIssue(issueURI);
          for(URI uri : articlesToRemove) {
            issue = adminService.removeArticle(issue, uri);
            addActionMessage("Removed Article: " + uri);
          }
          adminService.flushStore();
        } catch (Exception e) {
          addActionMessage("Article not removed due to the following error.");
          addActionMessage(e.toString());
          log.error("Remove Articels from Issue Failed.", e);
        }
        break;
      }

      case UPDATE_ISSUE: {
        try {
          issue = adminService.getIssue(issueURI);
          List<URI> issueURIs = adminService.parseCSV(articleListCSV);
          /*
           * Make sure the only changes to the articleListCSV
           * are ordering.
           */
          if (validateCSV(issueURIs, browseService.getArticleList(issue)))
            issue = adminService.updateIssue(issueURI,imageURI,displayName,issueURIs,respectOrder);

        } catch (Exception e) {
          addActionMessage("Issue not updated due to the following error.");
          addActionMessage(e.toString());
          log.error("Update Issue Failed.", e);
        }
        break;
      }

      case INVALID:
        break;
    }
    // Repopulate template values
    issue = adminService.getIssue(issueURI);
    articleGroups = browseService.getArticleGrpList(issue);
    articleOrderCSV = browseService.articleGrpListToCSV(articleGroups);
    orphans = getOrphannedArticles(issue, articleGroups);

    /*
     * Add the Orphans so that validateCSV doesn't prevent
     * all updates.
     */
    String orphanCSV = convertURIsToCSV(orphans);
    articleOrderCSV = (orphanCSV.length() > 0) ? articleOrderCSV +","+ orphanCSV : articleOrderCSV;
    journalInfo = adminService.createJournalInfo();
    
    return SUCCESS;
  }

  /**
   *
   * @param uriList
   * @return
   */
  private String convertURIsToCSV(List<URI> uriList) {
    StringBuilder csv = new StringBuilder();

    for(URI uri : uriList) {
      if (csv.length() > 0)
        csv.append(',');
      csv.append(uri.toString());
    }

    return csv.toString(); 
  }
  
  /**
   *
   * @param issue
   * @param articleGroups
   * @return
   */
  private List<URI> getOrphannedArticles(Issue issue, List<TOCArticleGroup> articleGroups) {
    List<URI> articleList = issue.getArticleList();
    List<URI> orphans = new ArrayList<URI>();

    for(URI articleURI : articleList) {
      Boolean found = false;
      for (TOCArticleGroup ag : articleGroups) {
        List<ArticleInfo> infoList = ag.getArticles();
        for(ArticleInfo info : infoList ) {
          if (info.getId().equals(articleURI)) {
            found = true;
            break;
          }
        }
      }
      if (!found)
        orphans.add(articleURI);
    }

    return orphans;
  }

  /**
   *
   * @param issueURIs List of issue URI's
   * @param articleList List of article URI's
   * @return
   * @throws URISyntaxException
   */
  private Boolean validateCSV(List<URI> issueURIs, List<URI> articleList) throws URISyntaxException {

    if (issueURIs.size() != articleList.size()) {
      addActionMessage("Issue not updated due to the following error.");
      addActionMessage("There has been an addition or deletion in the Article URI List.");

      return false;
    }

    for(URI uri : articleList) {
      if (!issueURIs.contains(uri)) {
        addActionMessage("Issue not updated due to the following error.");
        addActionMessage("One of the URI's in the Article URI List has changed.");

        return false;
      }
    }
    return true;
  }

  /**
   *
   * @return
   */
  public List<URI> getOrphans() {
    return this.orphans; 
  }

  /**
   * 
   * @return
   */
  public List<TOCArticleGroup> getArticleGroups() {
    return this.articleGroups; 
  }

  /**
   * Gets the JournalInfo value object for access in the view.
   *
   * @return Current virtual Journal value object.
   */
  public JournalInfo getJournal() {
    return journalInfo;
  }

  /**
   *
   */
  public Issue getIssue() {
    return this.issue;
  }

  /*
   *
   */
  public List<TOCArticleGroup> getArticleGrps() {
    return this.articleGroups;
  }

  /**
   *
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   *
   */
  public void setDisplayName(String name) {
    this.displayName = name;
  }

  /**
   *
   */
  public String getImageURI() {
    return (this.imageURI != null) ? imageURI.toString() : null;
  }

  /**
   *
   */
  public void setImageURI(String uri) {
    try {
      this.imageURI = new URI(uri.trim());
    } catch (Exception e) {
      this.imageURI = null;
      if (log.isDebugEnabled())
        log.debug("setImage URI conversion failed."  +
            "imageURI set to null to indicate this: " + uri.trim(), e);
    }
  }

  /**
   *
   */
  public void setVolumeURI(String uri) {
    try {
      this.volumeURI = new URI(uri.trim());
    } catch (Exception e) {
      this.volumeURI = null;
      if (log.isDebugEnabled())
        log.debug("setVolume URI conversion failed." +
            "volumeURI set to null to indicate this: " + uri.trim(), e);
    }
  }

  /**
   *
   */
  public String getVolumeURI() {
    return (this.volumeURI != null) ? volumeURI.toString() : null;
  }

  /**
   *
   */
  public String getArticleOrderCSV() {
    return this.articleOrderCSV;
  }

  /**
   *
   */
  public void setArticleListCSV(String list) {
    this.articleListCSV = list;
  }

  /**
   *
   */
  public void setRespectOrder(Boolean respectOrder) {
    this.respectOrder = respectOrder;
  }

  /**
   *
   */
  public void setIssueURI(String issueURI) {
    this.issueURI = URI.create(issueURI.trim());
  }

  /**
   *
   */
  public void setArticleURI(String articleCSVURIs) {
    this.articleCSVURIs = articleCSVURIs.trim();
  }

  /**
   *
   */
  public void setArticlesToRemove(String[] articlesToRemove) {
    for(String articleURI : articlesToRemove)
      this.articlesToRemove.add(URI.create(articleURI.trim()));
  }

  /**
   * Sets the Action to execute.
   *
   * @param  command the sub-action to execute for this class.
   */
  @Required
  public void setCommand(String command) {
    this.command = command;
  }

  /**
   * Sets the AdminService.
   *
   * @param  adminService The adminService to set.
   */
  @Required
  public void setAdminService(AdminService adminService) {
    this.adminService = adminService;
  }

  /**
   * Sets the BrowseService.
   *
   * @param  browseService The browseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }
}
