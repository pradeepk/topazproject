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

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.admin.service.AdminService;
import org.topazproject.ambra.admin.service.AdminService.JournalInfo;
import org.topazproject.ambra.models.Volume;
import org.topazproject.ambra.models.Issue;
import org.topazproject.otm.RdfUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 *
 */
public class VolumeManagementAction extends BaseAdminActionSupport {

  // Fields set by templates
  private String       command;
  private URI          volumeURI;
  private URI          issueURI;
  private String       displayName;
  private URI          imageURI;
  private String[]     issuesToDelete;
  private String       issuesToOrder;

  // Fields used by template
  private String       issuesCSV;
  private Volume       volume;
  private List<Issue>  issues;
  private JournalInfo  journalInfo;

  // Necessary services
  private AdminService adminService;

  private static final Log log = LogFactory.getLog(VolumeManagementAction.class);

  /**
   *
   */
  private enum VM_COMMANDS {
    UPDATE_VOLUME,
    CREATE_ISSUE,
    REMOVE_ISSUES,
    INVALID;

    /**
     * Convert a string specifying an action to its
     * enumerated equivalent.
     *
     * @param action  string value to convert.
     * @return        enumerated equivalent
     */
    public static VM_COMMANDS toCommand(String action) {
      VM_COMMANDS a;
      try {
        a = valueOf(action);
      } catch (Exception e) {
        // It's ok just return invalid.
        a = INVALID;
      }
      return a;
    }
  }

  /**
   * Main entry porint for Volume management action.
   */
  @Override
  @Transactional(rollbackFor = { Throwable.class })
  public String execute() throws Exception  {
    // Dispatch on hidden field command
    switch(VM_COMMANDS.toCommand(command)) {
      case CREATE_ISSUE:
        create_Issue();
        break;

      case UPDATE_VOLUME:
        update_Volume();
        break;

      case REMOVE_ISSUES:
        remove_Issues();
        break;

      case INVALID:
        repopulate();
        break;
    }
    return SUCCESS;
  }

  private void create_Issue() {
    if (issueURI != null) {
      try {
        Volume volume = adminService.getVolume(volumeURI);
        Issue i = adminService.createIssue(volume, issueURI, imageURI, displayName, null);
        if (i != null) {
          addActionMessage("Created Issue: " + i.getId());
        } else {
          addActionMessage("Duplicate Issue URI, " + issueURI);
        }
      } catch (Exception e) {
        addActionMessage("Issue not created due to the following error.");
        addActionMessage(e.getMessage());
        log.error("Create ISsue Failed.", e);
      }
    } else {
      addActionMessage("Invalid Issue URI");
    }
    repopulate();
  }

  private void update_Volume() {
    try {
      Volume volume = adminService.getVolume(volumeURI);
      List<URI> issueURIs = adminService.parseCSV(issuesToOrder);
      /*
       * Make sure the only changes to the articleListCSV
       * are ordering.
       */
      if (validateCSV(volume, issueURIs))
        volume = adminService.updateVolume(volume, displayName, issueURIs);

     } catch (Exception e) {
       addActionMessage("Volume was not updated due to the following error.");
       addActionMessage(e.getMessage());
       log.error("Update Volume Failed.", e);
    }
    repopulate();
  }

  private void remove_Issues() {
    try {
      for(String issurURI : issuesToDelete)
        adminService.deleteIssue(URI.create(issurURI));
    } catch (Exception e) {
      addActionMessage("Issue not removed due to the following error.");
      addActionMessage(e.getMessage());
      log.error("Remove Issue Failed.", e);
    }
    repopulate();
  }

  private void repopulate() {
    // Re-populate fields for template
    volume = adminService.getVolume(volumeURI);
    issuesCSV = adminService.getIssuesCSV(volumeURI);
    issues = adminService.getIssues(volumeURI);
    journalInfo = adminService.createJournalInfo();
  }

  /**
   *
   * @param volume
   * @param issueURIs
   * @return
   * @throws java.net.URISyntaxException
   */
  public Boolean validateCSV(Volume volume, List<URI> issueURIs) throws URISyntaxException {
    List<URI> curList = volume.getIssueList();

    if (issueURIs.size() != curList.size()) {
      addActionMessage("Issue not updated due to the following error.");
      addActionMessage("There has been an addition or deletion in the Issue URI List.");

      return false;
    }

    for(URI uri : curList) {
      if (!issueURIs.contains(uri)) {
        addActionMessage("Issue not updated due to the following error.");
        addActionMessage("One of the URI's in the Issue URI List has changed.");

        return false;
      }
    }
    return true;
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
   * Gets issues.
   *
   * @return Current issues associated with the volume.
   */
  public List<Issue> getIssues() {
    return issues;
  }

  /**
   * Gets issues.
   *
   * @return Current issues associated with the volume.
   */
  public String getIssuesCSV() {
    return issuesCSV;
  }

  /**
   * Gets volume.
   *
   * @return Current volume object.
   */
  public Volume getVolume() {
    return volume;
  }

  /**
   * Set the volume to manage.
   *  
   * @param  theVolume the volume to manage.
   */
  @Required
  public void setVolumeURI(String theVolume) {
    try {
      this.volumeURI = RdfUtil.validateUri(theVolume.trim(), "Volume Uri");
    } catch (Exception e) {
      this.volumeURI = null;
      if (log.isDebugEnabled())
        log.debug("setVolume URI conversion failed.");
    }
  }

 /**
   * Set the volume to manage.
   *
   * @param  issueURI .
   */
  @Required
  public void setIssueURI(String issueURI) {
    try {
      this.issueURI = RdfUtil.validateUri(issueURI.trim(), "Issue Uri");
    } catch (Exception e) {
      this.issueURI = null;
      if (log.isDebugEnabled())
        log.debug("setIssue URI conversion failed. ");
    }
  }
     /**
   * Set display name for a voulume.
   *
   * @param dsplyName the display of the volume.
   */
  public void setDisplayName(String dsplyName) {
    this.displayName = dsplyName.trim();
  }

  /**
   * Set image.
   *
   * @param image the image for this journal.
   */
  public void setImageURI(String image) {
    try {
      this.imageURI = RdfUtil.validateUri(image.trim(), "Image Uri");
    } catch (Exception e) {
      this.imageURI = null;
      if (log.isDebugEnabled())
        log.debug("setImage URI conversion failed. ");
    }
  }

  /**
   * Set issues to delete.
   *
   * @param issues .
   */
  public void setIssuesToDelete(String[] issues) {
    this.issuesToDelete = issues;
  }

  /**
   * Set issues to delete.
   *
   * @param issues .
   */
  public void setIssuesToOrder(String issues) {
    this.issuesToOrder = issues;
  }

  /**
   * Sets the Action to execute.
   *
   * @param  command the command to execute.
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
}
