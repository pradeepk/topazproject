/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.ws.annotation.NoSuchAnnotationIdException;
import org.topazproject.ws.annotation.Replies;
import org.topazproject.ws.annotation.RepliesClientFactory;
import org.topazproject.ws.annotation.ReplyInfo;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * Wrapper over reply web service
 */
public class ReplyWebService extends BaseAnnotationService {
  private Replies replyService;

  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService protectedService = getProtectedService();
    replyService = RepliesClientFactory.create(protectedService);
  }

  /**
   * Create a reply
   * @param mimeType mimeType
   * @param root root
   * @param inReplyTo inReplyTo
   * @param title title
   * @param body body
   * @param annotationService annotationService
   * @return a new reply
   * @throws RemoteException
   * @throws UnsupportedEncodingException
   * @see org.topazproject.ws.annotation.Replies#createReply(String,String,String,String,boolean,String,String,byte[])
   */
  public String createReply(final String mimeType, final String root, final String inReplyTo, final String title, final String body, final AnnotationService annotationService) throws RemoteException, NoSuchAnnotationIdException, UnsupportedEncodingException {
    final String contentType = getContentType(mimeType);
    return replyService.createReply(getApplicationId(), getDefaultType(), root, inReplyTo, isAnonymous(), title, contentType, body.getBytes(getEncodingCharset()));
  }

  /**
   * @param replyId replyId
   * @throws java.rmi.RemoteException
   * @see org.topazproject.ws.annotation.Replies#deleteReplies(String)
   */
  public void deleteReplies(final String replyId) throws RemoteException, NoSuchAnnotationIdException {
    replyService.deleteReplies(replyId);
  }

  /**
   * @param root root
   * @param inReplyTo inReplyTo
   * @throws RemoteException
   * @see org.topazproject.ws.annotation.Replies#deleteReplies(String, String)
   */
  public void deleteReplies(final String root, final String inReplyTo) throws RemoteException, NoSuchAnnotationIdException {
    replyService.deleteReplies(root, inReplyTo);
  }

  /**
   * @param replyId replyId
   * @return a reply
   * @throws java.rmi.RemoteException
   * @see org.topazproject.ws.annotation.Replies#getReplyInfo(String)
   */
  public ReplyInfo getReplyInfo(final String replyId) throws RemoteException, NoSuchAnnotationIdException {
    return replyService.getReplyInfo(replyId);
  }

  /**
   * @param root root
   * @param inReplyTo inReplyTo
   * @return list of replies
   * @throws RemoteException
   * @see org.topazproject.ws.annotation.Replies#listReplies(String, String)
   */
  public ReplyInfo[] listReplies(final String root, final String inReplyTo) throws RemoteException, NoSuchAnnotationIdException {
    return replyService.listReplies(root, inReplyTo);
  }

  /**
   * @param root root
   * @param inReplyTo inReplyTo
   * @return list all replies
   * @throws RemoteException
   * @see org.topazproject.ws.annotation.Replies#listAllReplies(String, String)
   */
  public ReplyInfo[] listAllReplies(final String root, final String inReplyTo) throws RemoteException, NoSuchAnnotationIdException {
    return replyService.listAllReplies(root, inReplyTo);
  }
}
