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
import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.annotation.ReplyInfo;

public class AnnotationActionsTest extends BasePlosoneTestCase {
  private static final String target = "http://here.is/viru";
//  private final String target = "doi:10.1371/annotation/21";

  private static final Log log = LogFactory.getLog(AnnotationActionsTest.class);
  private static String annotationId;

  public static Test suite() {
    final TestSuite orderedTests = new TestSuite();

    final String[] testsInSequence =
            new String[]{
                    "testDeleteAllAnnotations",
                    "testCreateAnnotation",
                    "testListAnnotations",
                    "testDeleteAnnotations",
                    "testCreateAnnotationShouldFailDueToProfanity",
                    "testCreateAnnotationShouldFailDueToSecurityImplications",
                    "testDeleteAllReplies"
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

    for (final AnnotationInfo annotation : listAnnotationAction.getAnnotations()) {
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

  public void testListAnnotations() throws Exception {
    final ListAnnotationAction listAnnotationAction = getListAnnotationAction();
    listAnnotationAction.setTarget(target);
    assertEquals(Action.SUCCESS, listAnnotationAction.execute());
    assertEquals(1, listAnnotationAction.getAnnotations().length);
  }

  public void testCreateAnnotation() throws Exception {
    final String body = "spmething that I always wanted to say";
    final String title = "Annotation1";
    final String context = "foo:bar##xpointer(id(\"Main\")/p[2])";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, context, "text/plain", body);
    assertEquals(Action.SUCCESS, createAnnotationAction.execute());
    final String annotationId = createAnnotationAction.getAnnotationId();
    log.debug("annotation created with id:" + annotationId);
    assertNotNull(annotationId);
    final AnnotationInfo savedAnnotation = getAnnotationService().getAnnotationInfo(annotationId);
    assertEquals(target, savedAnnotation.getAnnotates());
    assertEquals(title, savedAnnotation.getTitle());
    assertEquals(context, savedAnnotation.getContext());
    assertEquals(body.trim(), getAnnotationService().getBody(savedAnnotation.getBody()));

    this.annotationId = annotationId;
  }

  public void testCreateReply() throws Exception {
    final String body = "spmething that I always wanted to say";
    final String title = "Reply1";
    final CreateReplyAction createReplyAction = getCreateReplyAction(target, target, title, "text/plain", body);
    assertEquals(Action.SUCCESS, createReplyAction.execute());
    final String annotationId = createReplyAction.getReplyId();
    log.debug("annotation created with id:" + annotationId);
    assertNotNull(annotationId);
    final AnnotationInfo savedAnnotation = getAnnotationService().getAnnotationInfo(annotationId);
    assertEquals(target, savedAnnotation.getAnnotates());
    assertEquals(title, savedAnnotation.getTitle());
    assertEquals(body.trim(), getAnnotationService().getBody(savedAnnotation.getBody()));

    this.annotationId = annotationId;
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

  public void testCreateAnnotationShouldFailDueToProfanity() throws Exception {
    final String body = "something that I always wanted to say BUSH ";
    final String title = "Annotation1";
    final String context = "foo:bar##xpointer(id(\"Main\")/p[2])";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, context, "text/plain", body);
    assertEquals(Action.ERROR, createAnnotationAction.execute());
    assertTrue(createAnnotationAction.getFieldErrors().size() > 1);
  }

  public void testCreateAnnotationShouldFailDueToSecurityImplications() throws Exception {
    final String body = "something that I always <div>document.write('Booooom');office.cancellunch('tuesday')</div>";
    final String title = "Annotation1";
    final String context = "foo:bar##xpointer(id(\"Main\")/p[2])";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, context, "text/plain", body);
    assertEquals(Action.ERROR, createAnnotationAction.execute());
    assertTrue(createAnnotationAction.getFieldErrors().size() > 1);
  }

  private DeleteAnnotationAction getDeleteAnnotationAction(final String annotationId) {
    final DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction();
    deleteAnnotationAction.setAnnotationId(annotationId);
    deleteAnnotationAction.setDeletePreceding(false);
    return deleteAnnotationAction;
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
