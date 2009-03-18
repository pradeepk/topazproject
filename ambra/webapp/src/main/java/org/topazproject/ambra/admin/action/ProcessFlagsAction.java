/* $$HeadURL::                                                                            $$
 * $$Id$$
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import org.topazproject.ambra.annotation.service.AnnotationConverter;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.annotation.service.Flag;
import org.topazproject.ambra.annotation.service.ReplyService;
import org.topazproject.ambra.models.Annotea;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.MinorCorrection;
import org.topazproject.ambra.models.Retraction;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.rating.service.RatingsService;
import org.topazproject.ambra.admin.service.AdminService;
import org.topazproject.ambra.admin.service.AdminService.JournalInfo;

@SuppressWarnings("serial")
public class ProcessFlagsAction extends BaseAdminActionSupport {

  private static final Log log = LogFactory.getLog(ProcessFlagsAction.class);

  // Fields Used by template
  private String[] commentsToUnflag;
  private String[] commentsToDelete;
  private String[] convertToFormalCorrection;
  private String[] convertToMinorCorrection;
  private String[] convertToRetraction;
  private String[] convertToNote;
  private JournalInfo journalInfo;

  // Necessary Services
  private AdminService      adminService;
  private AnnotationService annotationService;
  private RatingsService    ratingsService;
  private ReplyService      replyService;
  protected AnnotationConverter converter;

  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * Set the RatingsService.
   *
   * For Spring wiring.
   *
   * @param ratingsService RatingService.
   */
  public void setRatingsService(RatingsService ratingsService) {
    this.ratingsService = ratingsService;
  }

  public void setCommentsToUnflag(String[] comments) {
    commentsToUnflag = comments;
  }

  public void setCommentsToDelete(String[] comments) {
    commentsToDelete = comments;
  }

  public void setConvertToFormalCorrection(String[] convertToFormalCorrection) {
    this.convertToFormalCorrection = convertToFormalCorrection;
  }

  public void setConvertToMinorCorrection(String[] convertToMinorCorrection) {
    this.convertToMinorCorrection = convertToMinorCorrection;
  }

  public void setConvertToRetraction(String[] convertToRetraction) {
    this.convertToRetraction = convertToRetraction;
  }

  public void setConvertToNote(String[] convertToNote) {
    this.convertToNote = convertToNote;
  }

  /**
   * Process the checked action items on the Flag Annotations displayed in the admin page. If an
   * error occurs when processing a checked item, the exception is logged and an action error is
   * added to be displayed on the admin console. We continue to attempt to process the other checked
   * items. The same admin console will be displayed regardless of error, and the errors will be
   * displayed in the console at the top of the page.
   */
  @Override
  @Transactional(rollbackFor = { Throwable.class })
  public String execute() {
    if (commentsToUnflag != null){
      for (String toUnFlag : commentsToUnflag){
        if (log.isDebugEnabled()){
          log.debug("Found comment to unflag: " + toUnFlag);
        }
        String[] tokens = toUnFlag.split("_");
        // token[0] = the target ID
        // token[1] = the flag ID
        // token[2] = the targetType (a string identifier)
        try {
          deleteFlag(tokens[0], tokens[1]);
        } catch(Exception e) {
          String errorMessage = "Failed to delete flag id='" + tokens[1] + "'" +
          "for annotation id='" + tokens[0] + "'";
          addActionError(errorMessage + " Exception: " +e.getMessage());
          log.error(errorMessage, e);
          TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
          return ERROR;
        }
      }
    }

    if (commentsToDelete != null) {
      for (String toDelete : commentsToDelete) {
        if (log.isDebugEnabled()) {
          log.debug("Found comment to delete: " + toDelete);
        }
        String[] tokens = toDelete.split("_");
        try {
          deleteTarget(tokens[0], tokens[1], tokens[2]);
        } catch (Exception e) {
          String errorMessage = "Failed to delete annotation id='" + tokens[1] + "'.";
          addActionError(errorMessage + " Exception: " +e.getMessage());
          log.error(errorMessage, e);
          TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
          return ERROR;
        }
      }
    }

    if (convertToFormalCorrection != null) {
      for (String paramStr : convertToFormalCorrection) {
        if (log.isDebugEnabled()) {
          log.debug("Converting to Formal Correction: "+paramStr);
        }
        String[] tokens = paramStr.split("_");
        try {
          annotationService.convertAnnotationToType(tokens[1], FormalCorrection.class);
          deleteFlag(tokens[1], tokens[0]);
        } catch(Exception e) {
          String errorMessage = "Failed to convert annotation id='" + tokens[1] +
                                "' to Formal Correction annotation.";
          addActionError(errorMessage + " Exception: " +e.getMessage());
          log.error(errorMessage, e);
          TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
          return ERROR;
        }
      }
    }

    if (convertToMinorCorrection != null) {
      for (String paramStr : convertToMinorCorrection) {
        if (log.isDebugEnabled()) {
          log.debug("Converting to Minor Correction: "+paramStr);
        }
        String[] tokens = paramStr.split("_");
        try {
          annotationService.convertAnnotationToType(tokens[1], MinorCorrection.class);
          deleteFlag(tokens[1], tokens[0]);
        } catch(Exception e) {
          String errorMessage = "Failed to convert annotation id='" + tokens[1] +
                                "' to Minor Correction annotation.";
          addActionError(errorMessage + " Exception: " +e.getMessage());
          log.error(errorMessage, e);
          TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
          return ERROR;
        }
      }
    }

    if (convertToRetraction != null) {
      for (String paramStr : convertToRetraction) {
        if (log.isDebugEnabled()) {
          log.debug("Converting to Retraction: "+paramStr);
        }
        String[] tokens = paramStr.split("_");
        try {
          annotationService.convertAnnotationToType(tokens[1], Retraction.class);
          deleteFlag(tokens[1], tokens[0]);
        } catch(Exception e) {
          String errorMessage = "Failed to convert annotation id='" + tokens[1] +
                                "' to Retraction annotation.";
          addActionError(errorMessage + " Exception: " +e.getMessage());
          log.error(errorMessage, e);
          TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
          return ERROR;
        }
      }
    }

    if (convertToNote != null) {
      for (String paramStr : convertToNote) {
        if (log.isDebugEnabled()) {
          log.debug("Converting to Note: "+paramStr);
        }
        String[] tokens = paramStr.split("_");
        try {
          annotationService.convertAnnotationToType(tokens[1], Comment.class);
          deleteFlag(tokens[1], tokens[0]);
        } catch(Exception e) {
          String errorMessage = "Failed to convert annotation id='" + tokens[1]
                                 + "' to Note annotation.";
          addActionError(errorMessage + " Exception: " +e.getMessage());
          log.error(errorMessage, e);
          TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
          return ERROR;
        }
      }
    }

    // create a faux journal object for template
    journalInfo = adminService.createJournalInfo();
    return base();
  }

  /**
   * @param root
   * @param target
   * @param targetType Type, Class name, of target
   *
   * Delete the target. Root is either an id (in which case
   * target is a reply) or else it is a "" in which case target is an annotation.
   * In either case:
   *         remove flags on this target
   *         get all replies to this target
   *             for each reply
   *                 check to see if reply has flags
   *                     remove each flag
   *         remove all replies in bulk
   *         remove target
   *
   *  Note that because this may be called from a 'batch' job (i.e multiple
   *  rows were checked off in process flags UI) it may be that things have
   *  been deleted by previous actions. So we catch 'non-exsietent-id' exceptions
   *  which may well get thrown and recover and continue (faster than going across
   *  teh web service tro see if the ID is valid then going out again to get its object).
   * @throws Exception
   */
  private void deleteTarget(String root, String target, String targetType)
      throws Exception {
    Reply[] replies;
    Flag[] flags = converter.convertAsFlags(
        annotationService.listAnnotations(target, annotationService.COMMENT_SET), false, false);

    if (log.isDebugEnabled()) {
      log.debug("Deleting Target" + target + " Root is " + root);
      log.debug(target + " has " + flags.length + " flags - deleting them");
    }
    for (Flag flag: flags) {
      try {
        deleteFlag(target, flag.getId());
      } catch (Exception e) {
        //keep going through the list even if there is an exception
        if (log.isWarnEnabled()) {
          log.warn("Couldn't delete flag: " + flag.getId(), e);
        }
      }
    }

    if (targetType.equals(Annotea.WEB_TYPE_REPLY)) {
      replies = replyService.listAllReplies(root, target);
    } else if (
        targetType.equals(Annotea.WEB_TYPE_COMMENT) ||
        targetType.equals(Annotea.WEB_TYPE_NOTE) ||
        targetType.equals(Annotea.WEB_TYPE_MINOR_CORRECTION) ||
        targetType.equals(Annotea.WEB_TYPE_FORMAL_CORRECTION) ||
        targetType.equals(Annotea.WEB_TYPE_RETRACTION)) {
      replies = replyService.listAllReplies(target, target);
    } else {
      // Flag type doesn't have Replies
      replies = new Reply[0];
    }

    if (log.isDebugEnabled()) {
      log.debug(target + " has " + replies.length + " replies. Removing their flags");
    }
    for (Reply reply : replies) {
      Flag[] replyFlags = converter.convertAsFlags
        (annotationService.listAnnotations(reply.getId().toString(), annotationService.COMMENT_SET),
                                           false, false);
      if (log.isDebugEnabled()) {
        log.debug("Reply " + reply.getId() + " has " + replyFlags.length + " flags");
      }
      for (Flag flag: replyFlags) {
        deleteFlag(reply.getId().toString(), flag.getId());
      }
    }

    if (targetType.equals(Annotea.WEB_TYPE_REPLY)) {
      replyService.deleteReplies(target); // Bulk delete
      //annotationService.deleteReply(target);
      if (log.isDebugEnabled()) {
        log.debug("Deleted reply: " + target);
      }
    } else if (
        targetType.equals(Annotea.WEB_TYPE_COMMENT) ||
        targetType.equals(Annotea.WEB_TYPE_NOTE) ||
        targetType.equals(Annotea.WEB_TYPE_MINOR_CORRECTION) ||
        targetType.equals(Annotea.WEB_TYPE_FORMAL_CORRECTION) ||
        targetType.equals(Annotea.WEB_TYPE_RETRACTION)) {
      replyService.deleteReplies(target, target);
      annotationService.deleteAnnotation(target);
      if (log.isDebugEnabled()) {
        log.debug("Deleted annotation: " + target);
      }
    } else if (targetType.equals(Annotea.WEB_TYPE_RATING)) {
      ratingsService.deleteRating(target);
      if (log.isDebugEnabled()) {
        log.debug("Deleted Rating: " + target);
      }
    }
  }

  /*
   *TODO: It is redundant (and error prone) to presume the caller of this action has provided the
   * correct target or targetType. This information should be retrieved from the flag by the
   * service.
   */
  private void deleteFlag(String target, String flag) throws Exception {
    // Delete flag
    if (log.isDebugEnabled())
      log.debug("Deleting flag: " + flag + " on target: " + target);
    annotationService.deleteAnnotation(flag);
  }

  public void setReplyService(ReplyService replyService) {
    this.replyService = replyService;
  }

  @Required
  public void setAnnotationConverter(AnnotationConverter converter) {
    this.converter = converter;
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
   * Sets the AdminService.
   *
   * @param  adminService The adminService to set.
   */
  @Required
  public void setAdminService(AdminService adminService) {
    this.adminService = adminService;
  }
}
