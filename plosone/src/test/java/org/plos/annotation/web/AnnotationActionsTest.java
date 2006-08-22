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

public class AnnotationActionsTest extends BasePlosoneTestCase {
  private static final String target = "http://here.is/viru";
//  private final String target = "doi:10.1371/annotation/21";

  private static final Log log = LogFactory.getLog(AnnotationActionsTest.class);
  private static String annotationId;

  public static Test suite() {
    final TestSuite orderedTests = new TestSuite();

    String[] testsInSequence = new String[]{
                                    "testDeleteAllAnnotations",
                                    "testCreateAnnotation",
                                    "testListAnnotations",
                                    "testDeleteAnnotations",
                                    "testCreateAnnotationShouldFailDueToProfanity",
                                    "testCreateAnnotationShouldFailDueToSecurityImplications"
                               };

    for (final String testName : testsInSequence) {
      orderedTests.addTest(new AnnotationActionsTest(testName));
    }

    return orderedTests;
  }

  public void testDeleteAllAnnotations() throws Exception {
    final ListAction listAction = getListAnnotationAction();
    listAction.setTarget(target);
    assertEquals(Action.SUCCESS, listAction.execute());

    for (final AnnotationInfo annotation : listAction.getAnnotations()) {
      DeleteAction deleteAction = getDeleteAnnotationAction(annotation.getId());
      assertEquals(Action.SUCCESS, deleteAction.execute());
    }
  }

  public void testDeleteAnnotations() throws Exception {
    DeleteAction deleteAction = getDeleteAnnotationAction(annotationId);
    assertEquals(Action.SUCCESS, deleteAction.execute());
    log.debug("annotation deleted with id:" + annotationId);
    assertEquals(0, deleteAction.getActionErrors().size());

    deleteAction = getDeleteAnnotationAction(annotationId);
    assertEquals(Action.ERROR, deleteAction.execute());
    assertEquals(1, deleteAction.getActionErrors().size());
  }

  public void testListAnnotations() throws Exception {
    final ListAction listAction = getListAnnotationAction();
    listAction.setTarget(target);
    assertEquals(Action.SUCCESS, listAction.execute());
    assertEquals(1, listAction.getAnnotations().length);
  }

  public void testCreateAnnotation() throws Exception {
    final String body = "spmething that I always wanted to say";
    final String title = "Annotation1";
    final String context = "foo:bar##xpointer(id(\"Main\")/p[2])";
    final CreateAction createAction = getCreateAnnotationAction(target, title, context, "text/plain", body);
    assertEquals(Action.SUCCESS, createAction.execute());
    final String annotationId = createAction.getAnnotationId();
    log.debug("annotation created with id:" + annotationId);
    assertNotNull(annotationId);
    final AnnotationInfo savedAnnotation = getAnnotationService().getAnnotationInfo(annotationId);
    assertEquals(target, savedAnnotation.getAnnotates());
    assertEquals(title, savedAnnotation.getTitle());
    assertEquals(context, savedAnnotation.getContext());
    assertEquals(body.trim(), getAnnotationService().getBody(savedAnnotation.getBody()));

    this.annotationId = annotationId;
  }

  public void testCreateAnnotationShouldFailDueToProfanity() throws Exception {
    final String body = "something that I always wanted to say BUSH ";
    final String title = "Annotation1";
    final String context = "foo:bar##xpointer(id(\"Main\")/p[2])";
    final CreateAction createAction = getCreateAnnotationAction(target, title, context, "text/plain", body);
    assertEquals(Action.ERROR, createAction.execute());
    assertTrue(createAction.getFieldErrors().size() > 1);
  }

  public void testCreateAnnotationShouldFailDueToSecurityImplications() throws Exception {
    final String body = "something that I always <div>document.write('Booooom');office.cancellunch('tuesday')</div>";
    final String title = "Annotation1";
    final String context = "foo:bar##xpointer(id(\"Main\")/p[2])";
    final CreateAction createAction = getCreateAnnotationAction(target, title, context, "text/plain", body);
    assertEquals(Action.ERROR, createAction.execute());
    assertTrue(createAction.getFieldErrors().size() > 1);
  }

  private DeleteAction getDeleteAnnotationAction(final String annotationId) {
    final DeleteAction deleteAction = getDeleteAnnotationAction();
    deleteAction.setAnnotationId(annotationId);
    deleteAction.setDeletePreceding(false);
    return deleteAction;
  }

  private CreateAction getCreateAnnotationAction(final String target, final String title, final String context, final String mimeType, final String body) {
    final CreateAction createAction = getCreateAnnotationAction();
    createAction.setTitle(title);
    createAction.setTarget(target);
    createAction.setTargetContext(context);
    createAction.setMimeType(mimeType);
    createAction.setBody(body);
    return createAction;
  }

  public AnnotationActionsTest(final String testName) {
    super(testName);
  }

}
