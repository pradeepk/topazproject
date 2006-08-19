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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.BasePlosoneTestCase;
import org.topazproject.ws.annotation.AnnotationClientFactory;
import org.topazproject.ws.annotation.Annotations;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;

public class AnnotationActionsTest extends BasePlosoneTestCase {
  private static final Log log = LogFactory.getLog(AnnotationActionsTest.class);

  public Annotations getAnnotationWebService() throws MalformedURLException, ServiceException {
     return AnnotationClientFactory.create("http://localhost:9080/ws-annotation-webapp-0.1/services/AnnotationServicePort");
  }

  public void testAnnotation_Create_Delete_List_Operations() throws Exception {
    final String target = "http://here.is/viru";
//    final String target = "doi:10.1371/annotation/21";
    final CreateAction createAction = getCreateAnnotationAction(target, "Annotation1", "foo:bar##xpointer(id(\"Main\")/p[2])", "text/plain", "spmething that I always wanted to say");
    assertEquals(Action.SUCCESS, createAction.execute());
    final String annotationId = createAction.getAnnotationId();
    log.debug("annotation created with id:" + annotationId);
    assertNotNull(annotationId);

    final ListAction listAction = getListAnnotationAction();
    listAction.setTarget(target);
    assertEquals(Action.SUCCESS, listAction.execute());
    assertEquals(1, listAction.getAnnotations().length);

    DeleteAction deleteAction = getDeleteAnnotationAction(annotationId);
    assertEquals(Action.SUCCESS, deleteAction.execute());
    log.debug("annotation deleted with id:" + annotationId);
    assertEquals(0, deleteAction.getActionErrors().size());

    deleteAction = getDeleteAnnotationAction(annotationId);
    assertEquals(Action.ERROR, deleteAction.execute());
    assertEquals(1, deleteAction.getActionErrors().size());

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
    createAction.setContext(context);
    createAction.setMimeType(mimeType);
    createAction.setBody(body);
    return createAction;
  }

}
