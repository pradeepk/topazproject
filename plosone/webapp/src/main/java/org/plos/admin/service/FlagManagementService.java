/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.service;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.annotation.service.AnnotationInfo;
import org.plos.annotation.service.ReplyInfo;
import org.plos.annotation.service.AnnotationWebService;
import org.plos.annotation.service.AnnotationService;
import org.plos.annotation.service.Flag;
import org.plos.annotation.service.ReplyWebService;
import static org.plos.annotation.service.Annotation.FLAG_MASK;
import static org.plos.annotation.service.Annotation.PUBLIC_MASK;
import org.plos.rating.service.RatingInfo;
import org.plos.user.service.UserService;

/**
 * @author alan
 * Manage documents on server. Ingest and access ingested documents.
 */
public class FlagManagementService {
  
  private static final Log log = LogFactory.getLog(FlagManagementService.class);
  
  private AnnotationWebService annotationWebService;
  private AnnotationService annotationService;
  private ReplyWebService replyWebService;
  
  private UserService userService;
  
  public Collection getFlaggedComments() throws RemoteException, ApplicationException {
    ArrayList<FlaggedCommentRecord> commentrecords = new ArrayList<FlaggedCommentRecord>();
    AnnotationInfo[] annotationinfos;
    ReplyInfo[] replyinfos;
    Flag flags[] = null;
    String creatorUserName;					
    
    final RatingInfo[] ratingInfos = annotationService.listFlaggedRatings();
    annotationinfos = annotationWebService.listAnnotations(null, FLAG_MASK| PUBLIC_MASK);
    replyinfos = replyWebService.listReplies(null, FLAG_MASK| PUBLIC_MASK ); // Bug - not marked with public flag for now
    if (log.isDebugEnabled()) { 
      log.debug("There are " + ratingInfos.length + " ratings with flags");
      log.debug("There are " + annotationinfos.length + " annotations with flags");
      log.debug("There are " + replyinfos.length + " replies with flags");
    }

    for (final RatingInfo ratingInfo : ratingInfos) {
      flags = annotationService.listFlags(ratingInfo.getId());
      if (log.isDebugEnabled())
        log.debug("There are " + flags.length + " flags on rating: " + ratingInfo.getId());
      for (final Flag flag : flags) {
        if (flag.isDeleted()) {
          if (log.isDebugEnabled())
            log.debug("Flag: " + flag.getId() + " is deleted - skipping");
          continue;
        }
        try {
          creatorUserName = userService.getUsernameByTopazId(flag.getCreator());
        } catch (ApplicationException ae) { // Bug ?
          creatorUserName = "anonymous";
        }
        FlaggedCommentRecord fcr =
          new FlaggedCommentRecord(
              flag.getId(),
              flag.getAnnotates(),
              ratingInfo.getTitle(),
              flag.getComment(),
              flag.getCreated(),
              creatorUserName,
              flag.getCreator(),
              null,
              flag.getReasonCode());
        commentrecords.add(fcr);
      }
    }

    for (AnnotationInfo annotationinfo : annotationinfos) {
      flags = annotationService.listFlags((String) annotationinfo.getId());
      if (log.isDebugEnabled())
        log.debug("There are " + flags.length + " flags on annotation: " + annotationinfo.getId());
      for (Flag flag : flags) {
        if (flag.isDeleted()) {
          if (log.isDebugEnabled())
            log.debug("Flag: " + flag.getId() + " is deleted - skipping");
          continue;
        }
        try {
          creatorUserName = userService.getUsernameByTopazId(flag.getCreator());
        } catch (ApplicationException ae) { // Bug ?
          creatorUserName = "anonymous";
        }
        FlaggedCommentRecord fcr = 
          new FlaggedCommentRecord(
              flag.getId(), 
              flag.getAnnotates(),							
              annotationinfo.getTitle(), 
              flag.getComment(), 
              flag.getCreated(), 
              creatorUserName, 
              flag.getCreator(),
              null, 
              flag.getReasonCode());
        commentrecords.add(fcr);
      }
    }
    
    for (ReplyInfo replyinfo : replyinfos) {
      flags = annotationService.listFlags((String) replyinfo.getId());
      if (log.isDebugEnabled())
        log.debug("There are " + flags.length + " flags on reply: " + replyinfo.getId());			
      for (Flag flag : flags) {
        if (flag.isDeleted()) {
          if (log.isDebugEnabled())
            log.debug("Flag: " + flag.getId() + " is deleted - skipping");					
          continue;
        }
        try {
          creatorUserName = userService.getUsernameByTopazId(flag
              .getCreator());
        } catch (ApplicationException ae) {
          creatorUserName = "anonymous";
        }
        FlaggedCommentRecord fcr = 
          new FlaggedCommentRecord(
              flag.getId(),
              replyinfo.getId(),							
              replyinfo.getTitle(), 
              flag.getComment(), 
              flag.getCreated(), 
              creatorUserName, 
              flag.getCreator(),							
              replyinfo.getRoot(), 
              flag.getReasonCode());
        commentrecords.add(fcr);
      }
    }
    Collections.sort(commentrecords);		
    return commentrecords;
  }
  
  public void setAnnotationWebService(
      AnnotationWebService annotationWebService) {
    this.annotationWebService = annotationWebService;
  }
  
  protected AnnotationWebService getAnnotationWebService() {
    return annotationWebService;
  }
  
  public void setReplyWebService(ReplyWebService replyWebService) {
    this.replyWebService = replyWebService;
  }
  
  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }
  
  public void setUserService(UserService userService) {
    this.userService = userService;
  }
  
  public AnnotationService getAnnotationService() {
    return annotationService;
  }
}
