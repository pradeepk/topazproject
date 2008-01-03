package org.plos.article.action;

import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.BrowseService;
import org.plos.configuration.ConfigurationStore;
import org.plos.journal.JournalService;
import org.plos.model.IssueInfo;
import org.plos.model.VolumeInfo;
import org.plos.models.Journal;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.otm.Session;

public class BrowseVolumeAction extends BaseActionSupport {
  private static final Log log = LogFactory.getLog(BrowseArticlesAction.class);
  private static final Configuration CONF = ConfigurationStore.getInstance()
      .getConfiguration();
  private BrowseService browseService;
  private JournalService journalService;
  private IssueInfo currentIssue;
  private int currentIssueNumber;
  private VolumeInfo currentVolume;
  private List<VolumeInfo> volumeInfos;
  private String gotoVolume;
  private Session session;

  @Override
  public String execute() throws Exception {
    Journal currentJournal = journalService.getCurrentJournal(session);
    volumeInfos = browseService.getVolumeInfos(currentJournal.getVolumes());
    Collections.reverse(volumeInfos);
    currentVolume = volumeInfos.get(0);

    currentIssue = browseService.getIssueInfo(currentJournal.getCurrentIssue());
    if (currentIssue != null) {
      // Figure out what issue number the currentIssue is in its volume
      for (VolumeInfo vol : volumeInfos) {
        int issueNum = 1;
        for (IssueInfo issue : vol.getIssueInfos()) {
          if (issue.getId().equals(currentIssue.getId())) {
            currentIssueNumber = issueNum;
            break;
          }
          issueNum++;
        }
      }
    }
    return SUCCESS;
  }

  /**
   * The sequence number of the current issue. This is calculated in the execute
   * method and displayed in the view.
   * 
   * @return
   */
  public int getCurrentIssueNumber() {
    return currentIssueNumber;
  }

  /**
   * The current issue as defined for the Journal.
   * 
   * @return
   */
  public IssueInfo getCurrentIssue() {
    return currentIssue;
  }

  public String getJournalName() {
    // TODO: Need to figure this out dynamically...
    return "PLoS Neglected Tropical Diseases";
  }

  /**
   * Called by Spring injection when this class is loaded...
   * 
   * @param browseService
   *          The browseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }

  /**
   * Called by Spring injection when this class is loaded...
   * 
   * @param journalService
   *          The JournalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * Returns the last linked volume for this journal - which is assumed to be
   * the current volume.
   * 
   * @return the current volume for this journal
   */
  public VolumeInfo getCurrentVolume() {
    return currentVolume;
  }

  /**
   * @return the VolumeInfos.
   */
  public List<VolumeInfo> getVolumeInfos() {
    return volumeInfos;
  }

  public String getGotoVolume() {
    return gotoVolume;
  }

  public void setGotoVolume(String gotoVolume) {
    this.gotoVolume = gotoVolume;
  }

  /**
   * Spring should automagically set the OTM Session.
   * 
   * @param session
   *          The OTM Session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }
}
