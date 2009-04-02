/* $HeadURL$
 * $Id$
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
package org.topazproject.ambra.article.action;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.model.IssueInfo;
import org.topazproject.ambra.model.VolumeInfo;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.Retraction;
import org.topazproject.ambra.util.ArticleXMLUtils;

/**
 * BrowseIssueAction retrieves data for presentation of an issue and a table of contents. Articles
 * contained in the issue are grouped into article types.
 *
 * @author Alex Worden
 *
 */
public class BrowseIssueAction extends BaseActionSupport{
  private static final Log log  = LogFactory.getLog(BrowseIssueAction.class);

  private String issue;
  private JournalService journalService;
  private BrowseService browseService;
  private IssueInfo issueInfo;
  private String issueDescription;
  private List<TOCArticleGroup> articleGroups;
  private MapContainer<URI, FormalCorrection> correctionMap;
  private MapContainer<URI, Retraction> retractionMap;

  private ArticleXMLUtils articleXmlUtils;

  private VolumeInfo volumeInfo;

  @Override
  @Transactional(readOnly = true)
  public String execute() {

    // was issued specified, or use Journal.currentIssue?
    if (issue == null || issue.length() == 0) {
      // JournalService, OTM usage wants to be in a Transaction
      Journal currentJournal = journalService.getJournal(getCurrentJournal());

      if (currentJournal != null) {
        URI currentIssueUri = currentJournal.getCurrentIssue();
        if (currentIssueUri != null) {
          issue = currentIssueUri.toString();
        } else {
          /*
           * Current Issue has not been set for the Journal - It should have
           * been configured via the admin console. Try to find the latest issue
           * from the latest volume. If no issue exists in the latest volume -
           * look at the previous volume and so on.
           */
          List<VolumeInfo> vols = browseService.getVolumeInfosForJournal(currentJournal);
          if (vols.size() > 0) {
            for (VolumeInfo volInfo : vols) {
              IssueInfo latestIssue = null;
              List<IssueInfo> issuesInVol = volInfo.getIssueInfos();
              if (issuesInVol.size() > 0) {
                latestIssue = issuesInVol.get(issuesInVol.size()-1);
              }
              if (latestIssue != null) {
                issue = latestIssue.getId().toString();
                break;
              }
            }
          }
        }
      }
    }

    // if still no issue, report an error and return ERROR status
    if (issue == null || issue.length() == 0) {
      log.error("No issue was specified for BrowseIssue action and no current issue could be derived.");
      return ERROR;
    }

    // look up Issue
    issueInfo = browseService.getIssueInfo(URI.create(issue));
    if (issueInfo == null) {
      log.error("Failed to retrieve IssueInfo for issue id='"+issue+"'");
      return ERROR;
    }

    volumeInfo  = browseService.getVolumeInfo(issueInfo.getParentVolume());

    // Translate the currentIssue description to HTML
    if (issueInfo.getDescription() != null) {
      try {
        issueDescription = articleXmlUtils.transformArticleDescriptionToHtml(issueInfo.getDescription());
      } catch (ApplicationException e) {
        log.error("Failed to translate issue description to HTML.", e);
        // Just use the untranslated issue description
        issueDescription = issueInfo.getDescription();
      }
    } else {
      log.error("The currentIssue description was null. Issue DOI='"+issueInfo.getId()+"'");
      issueDescription = "No description found for this issue";
    }
    articleGroups = browseService.getArticleGrpList(URI.create(issue));

    correctionMap = new MapContainer<URI, FormalCorrection>(
        browseService.getCorrectionMap(articleGroups));
    retractionMap = new MapContainer<URI, Retraction>(
        browseService.getRetractionMap(articleGroups));

    return SUCCESS;
  }

  /**
   * This class is used to work around limitation of Freemarker to wrap Map and only
   * accept String as a key.
   */
  public static class MapContainer<T,Q> implements Serializable {
    private Map<T,Q> map;

    public MapContainer(Map<T,Q> map) {
      this.map = map;
    }

    public Q getValue(T key) {
      return map.get(key);
    }
  }

  /**
   * Used by the view to retrieve the IssueInfo from the struts value stack.
   * @return the IssueInfo.
   */
  public IssueInfo getIssueInfo() {
    return issueInfo;
  }

  /**
   * Used by the view to retrieve an ordered list of TOCArticleGroup objects. Each TOCArticleGroup
   * represents a collection of article types that are defined in defaults.xml.  The groups are
   * listed by the view in the order returned here with links to the articles in that group
   * category.
   *
   * @return ordered list of TOCArticleGroup(s)
   */
  public List<TOCArticleGroup> getArticleGroups() {
    return articleGroups;
  }

  /**
   * If the request parameter 'issue' is specified, stuts will call this method. The action will
   * return a BrowseIssue page for this specific issue doi.
   * @param issue The issue for ToC view.
   */
  public void setIssue(String issue) {
    this.issue = issue;
  }

  /**
   * Spring injected method sets the JournalService.
   *
   * @param journalService The JournalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * Spring injected
   *
   * @param axu ArticleXMLUtils
   */
  @Required
  public void setArticleXmlUtils(ArticleXMLUtils axu) {
    articleXmlUtils = axu;
  }

  /**
   * Spring injected method sets the browseService.
   * @param browseService The browseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }

  public String getIssueDescription() {
    return issueDescription;
  }

  /**
   * returns the VolumeInfo for the current issue's parent volume
   * @return VolumeInfo
   */
  public VolumeInfo getVolumeInfo() {
    return volumeInfo;
  }

  public MapContainer<URI, FormalCorrection> getCorrectionMap() {
    return correctionMap;
  }

  public MapContainer<URI, Retraction> getRetractionMap() {
    return retractionMap;
  }
}
