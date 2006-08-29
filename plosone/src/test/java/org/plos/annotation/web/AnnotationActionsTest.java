/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
 package org.plos.annotation.web;

import com.opensymphony.xwork.Action;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.BasePlosoneTestCase;
import org.plos.annotation.service.Annotation;
import org.plos.annotation.service.Reply;

import java.util.Collection;
import java.util.ArrayList;

public class AnnotationActionsTest extends BasePlosoneTestCase {
  private static final String target = "http://here.is/viru";
  private final String body = "spmething that I always wanted to say about everything and more about nothing\n";
//  private final String target = "doi:10.1371/annotation/21";

  private static final Log log = LogFactory.getLog(AnnotationActionsTest.class);
  private static String annotationId = "doi:somedefaultvalue";
  private static String replyId;
  private final String PROFANE_WORD = "BUSH";
  private static String replyToReplyId;

  public static Test suite() {
    final TestSuite orderedTests = new TestSuite();

    final String[] testsInSequence =
            new String[]{
                    "testDeleteAllAnnotations",
                    "testDeleteAllReplies",
                    "testCreateAnnotation",
                    "testCreateReply",
                    "testListAnnotations",
                    "testListReplies",
                    "testDeleteAnnotations",
                    "testDeleteRepliesWithId",
                    "testCreateAnnotationShouldFailDueToProfanityInBody",
                    "testCreateAnnotationShouldFailDueToProfanityInTitle",
                    "testCreateReplyShouldFailDueToProfanityInBody",
                    "testCreateReplyShouldFailDueToProfanityInTitle",
                    "testCreateThreadedReplies",
                    "testListThreadedReplies",
                    "testGetAnnotationBodyShouldDeclawTheContentDueToSecurityImplications",
                    "testGetAnnotationShouldDeclawTheTitleDueToSecurityImplications",
                    "testGetReplyBodyShouldDeclawTheContentDueToSecurityImplications",
                    "testGetReplyShouldDeclawTheTitleDueToSecurityImplications"
            };

    for (final String testName : testsInSequence) {
      orderedTests.addTest(new AnnotationActionsTest(testName));
    }

    return orderedTests;
  }

  public void testDeleteAllAnnotations() throws Exception {
    final ListAnnotationAction listAnnotationAction = getListAnnotationAction();
    listAnnotationAction.setTarget(target);
    assertEquals(Action.SUCCESS, listAnnotationAction.execute());

    for (final Annotation annotation : listAnnotationAction.getAnnotations()) {
      DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotation.getId());
      assertEquals(Action.SUCCESS, deleteAnnotationAction.execute());
    }
  }

  public void testDeleteAllReplies() throws Exception {
    DeleteReplyAction deleteReplyAction = getDeleteReplyAction();
    deleteReplyAction.setRoot(target);
    deleteReplyAction.setInReplyTo(target);
    assertEquals(Action.SUCCESS, deleteReplyAction.deleteReplyWithRootAndReplyTo());
  }

  public void testDeleteAnnotations() throws Exception {
    DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotationId);
    assertEquals(Action.SUCCESS, deleteAnnotationAction.execute());
    log.debug("annotation deleted with id:" + annotationId);
    assertEquals(0, deleteAnnotationAction.getActionErrors().size());

    deleteAnnotationAction = getDeleteAnnotationAction(annotationId);
    assertEquals(Action.ERROR, deleteAnnotationAction.execute());
    assertEquals(1, deleteAnnotationAction.getActionErrors().size());
  }

  public void testDeleteRepliesWithId() throws Exception {
    DeleteReplyAction deleteReplyAction = getDeleteReplyAction();
    deleteReplyAction.setId(replyId);
    assertEquals(Action.SUCCESS, deleteReplyAction.deleteReplyWithId());
    log.debug("annotation deleted with id:" + replyId);
    assertEquals(0, deleteReplyAction.getActionErrors().size());

    deleteReplyAction = getDeleteReplyAction();
    deleteReplyAction.setId(replyId);
    assertEquals(Action.ERROR, deleteReplyAction.deleteReplyWithId());

    assertEquals(1, deleteReplyAction.getActionErrors().size());
  }

  public void testListAnnotations() throws Exception {
    final ListAnnotationAction listAnnotationAction = getListAnnotationAction();
    listAnnotationAction.setTarget(target);
    assertEquals(Action.SUCCESS, listAnnotationAction.execute());
    assertEquals(1, listAnnotationAction.getAnnotations().length);
  }

  public void testCreateAnnotation() throws Exception {
    final String title = "Annotation1";
    final String context = "foo:bar##xpointer(id(\"Main\")/p[2])";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, context, "text/plain", body);
    assertEquals(Action.SUCCESS, createAnnotationAction.execute());
    final String annotationId = createAnnotationAction.getAnnotationId();
    log.debug("annotation created with id:" + annotationId);
    assertNotNull(annotationId);

    final GetAnnotationAction getAnnotationAction = getGetAnnotationAction();
    getAnnotationAction.setAnnotationId(annotationId);
    assertEquals(Action.SUCCESS, getAnnotationAction.execute());
    final Annotation savedAnnotation = getAnnotationAction.getAnnotation();
    assertEquals(target, savedAnnotation.getAnnotates());
    assertEquals(title, savedAnnotation.getTitle());
    assertEquals(context, savedAnnotation.getContext());

    final BodyFetchAction bodyFetchAction = getAnnotationBodyFetcherAction();
    bodyFetchAction.setBodyUrl(savedAnnotation.getBody());
    assertEquals(Action.SUCCESS, bodyFetchAction.execute());
    assertEquals(body, bodyFetchAction.getBody());

    AnnotationActionsTest.annotationId = annotationId;
  }

  public void testCreateReply() throws Exception {
    final String title = "Reply1";
    final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, annotationId, title, "text/plain", body);
    assertEquals(Action.SUCCESS, createReplyAction.execute());
    final String replyId = createReplyAction.getReplyId();
    log.debug("annotation created with id:" + replyId);
    assertNotNull(replyId);

    final GetReplyAction getReplyAction = getGetReplyAction();
    getReplyAction.setReplyId(replyId);
    assertEquals(Action.SUCCESS, getReplyAction.execute());
    final Reply savedReply = getAnnotationService().getReply(replyId);
    assertEquals(annotationId, savedReply.getRoot());
    assertEquals(annotationId, savedReply.getInReplyTo());
    assertEquals(title, savedReply.getTitle());

    final BodyFetchAction bodyFetchAction = getAnnotationBodyFetcherAction();
    bodyFetchAction.setBodyUrl(savedReply.getBody());
    assertEquals(Action.SUCCESS, bodyFetchAction.execute());
    assertEquals(body, bodyFetchAction.getBody());

    AnnotationActionsTest.replyId = replyId;
  }


  public void testCreateThreadedReplies() throws Exception {
    final String replyId;
    {
      final String title = "Reply1";
      final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, annotationId, title, "text/plain", body);
      assertEquals(Action.SUCCESS, createReplyAction.execute());
      replyId = createReplyAction.getReplyId();
      log.debug("reply created with id:" + replyId);
      assertNotNull(replyId);

      final GetReplyAction getReplyAction = getGetReplyAction();
      getReplyAction.setReplyId(replyId);
      assertEquals(Action.SUCCESS, getReplyAction.execute());
      final Reply savedReply = getAnnotationService().getReply(replyId);
      assertEquals(annotationId, savedReply.getRoot());
      assertEquals(annotationId, savedReply.getInReplyTo());
      assertEquals(title, savedReply.getTitle());

      final BodyFetchAction bodyFetchAction = getAnnotationBodyFetcherAction();
      bodyFetchAction.setBodyUrl(savedReply.getBody());
      assertEquals(Action.SUCCESS, bodyFetchAction.execute());
      assertEquals(body, bodyFetchAction.getBody());
      AnnotationActionsTest.replyId = replyId;
    }

    {
      final String title = "Reply to Reply1";
      final String replyBody2 = "some text in response to the earlier teply";
      final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, replyId, title, "text/plain", replyBody2);
      assertEquals(Action.SUCCESS, createReplyAction.execute());
      final String replyToReplyId = createReplyAction.getReplyId();
      log.debug("reply created with id:" + replyToReplyId);
      assertNotNull(replyToReplyId);

      final GetReplyAction getReplyAction = getGetReplyAction();
      getReplyAction.setReplyId(replyToReplyId);
      assertEquals(Action.SUCCESS, getReplyAction.execute());
      final Reply savedReply = getAnnotationService().getReply(replyToReplyId);
      assertEquals(annotationId, savedReply.getRoot());
      assertEquals(replyId, savedReply.getInReplyTo());
      assertEquals(title, savedReply.getTitle());

      final BodyFetchAction bodyFetchAction = getAnnotationBodyFetcherAction();
      bodyFetchAction.setBodyUrl(savedReply.getBody());
      assertEquals(Action.SUCCESS, bodyFetchAction.execute());
      assertEquals(replyBody2, bodyFetchAction.getBody());
      AnnotationActionsTest.replyToReplyId = replyToReplyId;
    }

  }

  public void testListThreadedReplies() throws Exception {
    final ListReplyAction listReplyAction = getListReplyAction();
    listReplyAction.setRoot(annotationId);
    listReplyAction.setInReplyTo(annotationId);
    assertEquals(Action.SUCCESS, listReplyAction.listAllReplies());

    final Reply[] replies = listReplyAction.getAllReplies();

    final Collection<String> list = new ArrayList<String>();
    for (final Reply reply : replies) {
      list.add(reply.getId());
    }

    assertTrue(list.contains(replyId));
    assertTrue(list.contains(replyToReplyId));
  }

  public void testListReplies() throws Exception {
    final ListReplyAction listReplyAction = getListReplyAction();
    listReplyAction.setRoot(annotationId);
    listReplyAction.setInReplyTo(annotationId);
    assertEquals(Action.SUCCESS, listReplyAction.execute());
    assertEquals(1, listReplyAction.getReplies().length);
  }

  public void testCreateAnnotationShouldFailDueToProfanityInBody() throws Exception {
    final String body = "something that I always wanted to say " + PROFANE_WORD;
    final String title = "Annotation1";
    final String context = "foo:bar##xpointer(id(\"Main\")/p[2])";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, context, "text/plain", body);
    assertEquals(Action.ERROR, createAnnotationAction.execute());
    assertTrue(createAnnotationAction.hasErrors());
  }

  public void testCreateAnnotationShouldFailDueToProfanityInTitle() throws Exception {
    final String body = "something that I always wanted to say";
    final String title = "Annotation " + PROFANE_WORD;
    final String context = "foo:bar##xpointer(id(\"Main\")/p[2])";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, context, "text/plain", body);
    assertEquals(Action.ERROR, createAnnotationAction.execute());
    assertTrue(createAnnotationAction.hasErrors());
  }

  public void testCreateReplyShouldFailDueToProfanityInBody() throws Exception {
    final String body = "something that I always wanted to say " + PROFANE_WORD;
    final String title = "Reply1";
    final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, annotationId, title, "text/plain", body);
    assertEquals(Action.ERROR, createReplyAction.execute());
    assertTrue(createReplyAction.hasErrors());
  }

  public void testCreateReplyShouldFailDueToProfanityInTitle() throws Exception {
    final String body = "something that I always wanted to say";
    final String title = "Reply1 " + PROFANE_WORD;
    final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, annotationId, title, "text/plain", body);
    assertEquals(Action.ERROR, createReplyAction.execute());
    assertTrue(createReplyAction.hasErrors());
  }

  public void testGetAnnotationBodyShouldDeclawTheContentDueToSecurityImplications() throws Exception {
    final String body = "something that I always <div>document.write('Booooom');office.cancellunch('tuesday')</div>";
    final String declawedBody = "something that I always &lt;div&gt;document.write('Booooom');office.cancellunch('tuesday')&lt;/div&gt;";
//    final String body = "something that I always $div$document.write('Booooom');office.cancellunch('tuesday')$/div$";
//    final String declawedBody = "something that I always dollardivdollardocument.write('Booooom');office.cancellunch('tuesday')dollar/divdollar";

    final String title = "Annotation1";
    final String context = "foo:bar##xpointer(id(\"Main\")/p[2])";

    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, context, "text/plain", body);
    assertEquals(Action.SUCCESS, createAnnotationAction.execute());
    final String annotationId1 = createAnnotationAction.getAnnotationId();

    final Annotation savedAnnotation = getAnnotationService().getAnnotation(annotationId1);
    final BodyFetchAction bodyFetchAction = getAnnotationBodyFetcherAction();
    bodyFetchAction.setBodyUrl(savedAnnotation.getBody());

    assertEquals(Action.SUCCESS, bodyFetchAction.execute());
    assertEquals(declawedBody, bodyFetchAction.getBody());

  }

  public void testGetAnnotationShouldDeclawTheTitleDueToSecurityImplications() throws Exception {
    final String body = "something that I think";

    final String title = "Annotation1 <&>";
    final String declawedTitle = "Annotation1 &lt;&amp;&gt;";
    final String context = "foo:bar##xpointer(id(\"Main\")/p[2])";

    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, context, "text/plain", body);
    assertEquals(Action.SUCCESS, createAnnotationAction.execute());
    final String annotationId1 = createAnnotationAction.getAnnotationId();

    final GetAnnotationAction getAnnotationAction = getGetAnnotationAction();
    getAnnotationAction.setAnnotationId(annotationId1);
    assertEquals(Action.SUCCESS, getAnnotationAction.execute());
    final Annotation savedAnnotation = getAnnotationAction.getAnnotation();
    assertNotNull(savedAnnotation);
    assertEquals(declawedTitle, savedAnnotation.getTitle());

  }

  public void testGetReplyBodyShouldDeclawTheContentDueToSecurityImplications() throws Exception {
    final String body = "something that I always <div>document.write('Booooom');office.cancellunch('tuesday')</div>";
    final String declawedBody = "something that I always &lt;div&gt;document.write('Booooom');office.cancellunch('tuesday')&lt;/div&gt;";
//    final String body = "something that I always $div$document.write('Booooom');office.cancellunch('tuesday')$/div$";
//    final String declawedBody = "something that I always dollardivdollardocument.write('Booooom');office.cancellunch('tuesday')dollar/divdollar";

    final String title = "Annotation1";

    final CreateReplyAction createAnnotationAction = getCreateReplyAction(annotationId, annotationId, title, "text/plain", body);
    assertEquals(Action.SUCCESS, createAnnotationAction.execute());
    final String id1 = createAnnotationAction.getReplyId();

    final Reply savedReply = getAnnotationService().getReply(id1);
    final BodyFetchAction bodyFetchAction = getAnnotationBodyFetcherAction();
    bodyFetchAction.setBodyUrl(savedReply.getBody());

    assertEquals(Action.SUCCESS, bodyFetchAction.execute());
    assertEquals(declawedBody, bodyFetchAction.getBody());

  }


  public void testGetReplyShouldDeclawTheTitleDueToSecurityImplications() throws Exception {
    final String body = "something that I think";

    final String title = "reply <&>";
    final String declawedTitle = "reply &lt;&amp;&gt;";

    final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, annotationId, title, "text/plain", body);
    assertEquals(Action.SUCCESS, createReplyAction.execute());
    final String replyId1 = createReplyAction.getReplyId();

    final GetReplyAction getAnnotationAction = getGetReplyAction();
    getAnnotationAction.setReplyId(replyId1);
    assertEquals(Action.SUCCESS, getAnnotationAction.execute());
    final Reply savedReply = getAnnotationAction.getReply();
    assertNotNull(savedReply);
    assertEquals(declawedTitle, savedReply.getTitle());

  }
  private DeleteAnnotationAction getDeleteAnnotationAction(final String annotationId) {
    final DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction();
    deleteAnnotationAction.setAnnotationId(annotationId);
    deleteAnnotationAction.setDeletePreceding(false);
    return deleteAnnotationAction;
  }

  private CreateReplyAction getCreateReplyAction(final String root, final String inReplyTo, final String title, final String mimeType, final String body) {
    final CreateReplyAction createReplyAction = getCreateReplyAction();
    createReplyAction.setRoot(root);
    createReplyAction.setInReplyTo(inReplyTo);
    createReplyAction.setTitle(title);
    createReplyAction.setMimeType(mimeType);
    createReplyAction.setBody(body);
    return createReplyAction;
  }

  private CreateAnnotationAction getCreateAnnotationAction(final String target, final String title, final String context, final String mimeType, final String body) {
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction();
    createAnnotationAction.setTitle(title);
    createAnnotationAction.setTarget(target);
    createAnnotationAction.setTargetContext(context);
    createAnnotationAction.setMimeType(mimeType);
    createAnnotationAction.setBody(body);
    return createAnnotationAction;
  }

  public AnnotationActionsTest(final String testName) {
    super(testName);
  }
}
