/* $HeadURL::                                                                            $
 * $Id:AnnotationActionsTest.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
 package org.plos.annotation.action;

import com.opensymphony.xwork.Action;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.BasePlosoneTestCase;
import org.plos.annotation.service.Annotation;
import org.plos.annotation.service.AnnotationPermission;
import org.plos.annotation.service.AnnotationService;
import org.plos.annotation.service.Reply;
import org.plos.permission.service.PermissionWebService;
import org.topazproject.ws.annotation.Annotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AnnotationActionsTest extends BasePlosoneTestCase {
  private static final String target = "http://here.is/viru";
  private final String body = "spmething that I always wanted to say about everything and more about nothing\n";
  final String ANON_PRINCIPAL = "anonymous:user/";
//  private final String target = "doi:10.1371/annotation/21";

  private static final Log log = LogFactory.getLog(AnnotationActionsTest.class);
  private static String annotationId = "doi:somedefaultvalue";
  private static String replyId;
  private final String PROFANE_WORD = "BUSH";
  private static String replyToReplyId;

  public void testSequencedTests() throws Exception {
    deleteAllAnnotations();
    deleteAllReplies();
    createAnnotation();
    createReply();
    listAnnotations();
    listReplies();
  }

  public void deleteAllAnnotations() throws Exception {
    final ListAnnotationAction listAnnotationAction = getListAnnotationAction();
    listAnnotationAction.setTarget(target);
    assertEquals(Action.SUCCESS, listAnnotationAction.execute());

    for (final Annotation annotation : listAnnotationAction.getAnnotations()) {
      DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotation.getId(), false);
      assertEquals(Action.SUCCESS, deleteAnnotationAction.execute());
    }
  }

  public void deleteAllReplies() throws Exception {
    DeleteReplyAction deleteReplyAction = getDeleteReplyAction();
    deleteReplyAction.setRoot(target);
    deleteReplyAction.setInReplyTo(target);
    assertEquals(Action.SUCCESS, deleteReplyAction.deleteReplyWithRootAndReplyTo());
  }

  public void testDeleteAnnotations() throws Exception {
    DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotationId, false);
    assertEquals(Action.SUCCESS, deleteAnnotationAction.execute());
    log.debug("annotation deleted with id:" + annotationId);
    assertEquals(0, deleteAnnotationAction.getActionErrors().size());

    deleteAnnotationAction = getDeleteAnnotationAction(annotationId, false);
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
    assertEquals("Should throw an error when deleting a nonexisting reply id", Action.ERROR, deleteReplyAction.deleteReplyWithId());

    assertEquals(1, deleteReplyAction.getActionErrors().size());
  }

  public void listAnnotations() throws Exception {
    final ListAnnotationAction listAnnotationAction = getListAnnotationAction();
    listAnnotationAction.setTarget(target);
    assertEquals(Action.SUCCESS, listAnnotationAction.execute());
    assertEquals(1, listAnnotationAction.getAnnotations().length);
  }

  public void createAnnotation() throws Exception {
    final String title = "Annotation1";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, body);
    final String context = createAnnotationAction.getTargetContext();
    final boolean visibility = true;
    createAnnotationAction.setPublic(visibility);
    assertEquals(Action.SUCCESS, createAnnotationAction.execute());
    final String annotationId = createAnnotationAction.getAnnotationId();
    log.debug("annotation created with id:" + annotationId);
    assertNotNull(annotationId);

    final Annotation savedAnnotation = retrieveAnnotation(annotationId);
    assertEquals(target, savedAnnotation.getAnnotates());
    assertEquals(title, savedAnnotation.getTitle());
    assertEquals(context, savedAnnotation.getContext());
    assertEquals(body, savedAnnotation.getBody());
    assertEquals(body, savedAnnotation.getBody());
    assertEquals(visibility, savedAnnotation.isPublic());

    AnnotationActionsTest.annotationId = annotationId;
  }

  public void testCreatePrivateAnnotation() throws Exception {
    final String title = "AnnotationPrivate";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, "text/plain", body);
    final String context = createAnnotationAction.getTargetContext();
    final boolean visibility = false;
    createAnnotationAction.setPublic(visibility);
    assertEquals(Action.SUCCESS, createAnnotationAction.execute());
    final String annotationId = createAnnotationAction.getAnnotationId();
    log.debug("annotation created with id:" + annotationId);
    assertNotNull(annotationId);

    final Annotation savedAnnotation = retrieveAnnotation(annotationId);
    assertEquals(target, savedAnnotation.getAnnotates());
    assertEquals(title, savedAnnotation.getTitle());
    assertEquals(context, savedAnnotation.getContext());
    assertEquals(body, savedAnnotation.getBody());
    assertEquals(body, savedAnnotation.getBody());
    assertEquals(visibility, savedAnnotation.isPublic());

    AnnotationActionsTest.annotationId = annotationId;
  }

  public void createReply() throws Exception {
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
    assertEquals(body, savedReply.getBody());

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

      assertEquals(body, savedReply.getBody());
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

      assertEquals(replyBody2, savedReply.getBody());
      AnnotationActionsTest.replyToReplyId = replyToReplyId;
    }

  }

  public void testListThreadedReplies() throws Exception {
    final ListReplyAction listReplyAction = getListReplyAction();
    listReplyAction.setRoot(annotationId);
    listReplyAction.setInReplyTo(annotationId);
    assertEquals(Action.SUCCESS, listReplyAction.listAllReplies());

    final Reply[] replies = listReplyAction.getReplies();

    final Collection<String> list = new ArrayList<String>();
    for (final Reply reply : replies) {
      list.add(reply.getId());
    }

    assertTrue(list.contains(replyId));
    assertTrue(list.contains(replyToReplyId));
  }

  public void listReplies() throws Exception {
    final ListReplyAction listReplyAction = getListReplyAction();
    listReplyAction.setRoot(annotationId);
    listReplyAction.setInReplyTo(annotationId);
    assertEquals(Action.SUCCESS, listReplyAction.execute());
    assertEquals(1, listReplyAction.getReplies().length);
  }

  public void testCreateAnnotationShouldFailDueToProfanityInBody() throws Exception {
    final String body = "something that I always wanted to say " + PROFANE_WORD;
    final String title = "Annotation1";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, "text/plain", body);
    assertEquals(Action.ERROR, createAnnotationAction.execute());
    assertTrue(createAnnotationAction.hasErrors());
  }

  public void testCreateAnnotationShouldFailDueToProfanityInTitle() throws Exception {
    final String body = "something that I always wanted to say";
    final String title = "Annotation " + PROFANE_WORD;
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, "text/plain", body);
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

  public void testGetAnnotationShouldDeclawTheBodyContentDueToSecurityImplications() throws Exception {
    final String body = "something that I always <div>document.write('Booooom');office.cancellunch('tuesday')</div>";
    final String declawedBody = "something that I always &lt;div&gt;document.write('Booooom');office.cancellunch('tuesday')&lt;/div&gt;";
//    final String body = "something that I always $div$document.write('Booooom');office.cancellunch('tuesday')$/div$";
//    final String declawedBody = "something that I always dollardivdollardocument.write('Booooom');office.cancellunch('tuesday')dollar/divdollar";

    final String title = "Annotation1";

    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, "text/plain", body);
    assertEquals(Action.SUCCESS, createAnnotationAction.execute());
    final String annotationId1 = createAnnotationAction.getAnnotationId();

    final Annotation savedAnnotation = getAnnotationService().getAnnotation(annotationId1);
    assertEquals(declawedBody, savedAnnotation.getBody());

  }

  public void testGetAnnotationShouldDeclawTheTitleDueToSecurityImplications() throws Exception {
    final String body = "something that I think";

    final String title = "Annotation1 <&>";
    final String declawedTitle = "Annotation1 &lt;&amp;&gt;";

    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, "text/plain", body);
    assertEquals(Action.SUCCESS, createAnnotationAction.execute());
    final String annotationId1 = createAnnotationAction.getAnnotationId();

    final Annotation savedAnnotation = retrieveAnnotation(annotationId1);
    assertNotNull(savedAnnotation);
    assertEquals(declawedTitle, savedAnnotation.getTitle());

  }

  public void testGetReplyShouldDeclawTheBodyContentDueToSecurityImplications() throws Exception {
    final String body = "something that I always & < >";
    final String declawedBody = "something that I always &amp; &lt; &gt;";
//    final String body = "something that I always $div$document.write('Booooom');office.cancellunch('tuesday')$/div$";
//    final String declawedBody = "something that I always dollardivdollardocument.write('Booooom');office.cancellunch('tuesday')dollar/divdollar";

    final String title = "Annotation1";

    final CreateReplyAction createAnnotationAction = getCreateReplyAction(annotationId, annotationId, title, "text/plain", body);
    assertEquals(Action.SUCCESS, createAnnotationAction.execute());
    final String id = createAnnotationAction.getReplyId();

    final Reply savedReply = getAnnotationService().getReply(id);
    assertEquals(declawedBody, savedReply.getBody());
  }

  public void testGetReplyShouldDeclawTheTitleContentDueToSecurityImplications() throws Exception {
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

  public void testPublicAnnotationShouldHaveTheRightGrantsAndRevokations() throws Exception {
    final String title = "Annotation1";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, "text/plain", body);
    final String context = createAnnotationAction.getTargetContext();
    assertEquals(Action.SUCCESS, createAnnotationAction.execute());
    final String annotationId = createAnnotationAction.getAnnotationId();
    log.debug("annotation created with id:" + annotationId);
    assertNotNull(annotationId);

    final Annotation savedAnnotation = retrieveAnnotation(annotationId);
    assertEquals(target, savedAnnotation.getAnnotates());
    assertEquals(title, savedAnnotation.getTitle());
    assertEquals(context, savedAnnotation.getContext());
    assertEquals(body, savedAnnotation.getBody());
    assertFalse(savedAnnotation.isPublic());

    final AnnotationService annotationService = getAnnotationService();
    final PermissionWebService permissionWebService = getPermissionWebService();
    annotationService.setAnnotationPublic(annotationId);

    final Annotation annotation = retrieveAnnotation(annotationId);
    assertTrue(annotation.isPublic());

    final List<String> grantsList = Arrays.asList(permissionWebService.listGrants(annotationId, AnnotationPermission.ALL_PRINCIPALS));
    assertTrue(grantsList.contains(Annotations.Permissions.GET_ANNOTATION_INFO));

    final String currentUser = ANON_PRINCIPAL;

    final List<String> revokesList = Arrays.asList(permissionWebService.listRevokes(annotationId, currentUser));
    assertTrue(revokesList.contains(Annotations.Permissions.DELETE_ANNOTATION));
    assertTrue(revokesList.contains(Annotations.Permissions.SUPERSEDE));

    //Cleanup - Reset the permissions so that these annotations can be deleted by other unit tests
    permissionWebService.cancelRevokes(
            annotationId,
            new String[] {Annotations.Permissions.DELETE_ANNOTATION, Annotations.Permissions.SUPERSEDE},
            new String[] {currentUser}
    );
    
    permissionWebService.cancelGrants(
            annotationId,
            new String[] {Annotations.Permissions.GET_ANNOTATION_INFO},
            new String[] {AnnotationPermission.ALL_PRINCIPALS}
    );
  }

  public void testAnnotatedContent() throws Exception {
//    final String testXml =
//      "<!DOCTYPE doc [<!ELEMENT testid (testid)*>"
//      + " <!ATTLIST testid id ID #REQUIRED > ] > <doc> <chapter> <title>Chapter I</title> "
//      + " <para>Hello world, indeed, <em>wonderful</em> world</para></chapter></doc>";

    String target = "http://localhost:9080/existingArticle/test.xml";

    final String startPath1    = "/para";
    final String startPath2    = "/title";
    final String startPath3    = "/";
    final String endPath1    = "/para";
    final String endPath2    = "/title";
    final String endPath3    = "/";
    final String context1Body = "My annotation content 1";
    final String context2Body = "My annotation content 2";
    final String context3Body = "My annotation content 3";
    final String title       = "Title";

//    final ListAnnotationAction listAnnotationAction = getListAnnotationAction();
//    listAnnotationAction.setTarget(target);
//    listAnnotationAction.execute();
//    Annotation[] annotations = listAnnotationAction.getAnnotations();
//
//    for (final Annotation annotation : annotations) {
//      final DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotation.getId(), true);
//      deleteAnnotationAction.execute();
//    }

    class AnnotationCreator {
      public void execute(final String target, final String startPath, final String endPath, final String title, final String body) throws Exception {
        final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, body);
        createAnnotationAction.setStartPath(startPath);
        createAnnotationAction.setEndPath(endPath);
        assertEquals(Action.SUCCESS, createAnnotationAction.execute());
      }
    }

    new AnnotationCreator().execute(target, startPath1, endPath1, title, context1Body);
    new AnnotationCreator().execute(target, startPath2, endPath2, title, context2Body);
    new AnnotationCreator().execute(target, startPath3, endPath3, title, context3Body);

    final ListAnnotationAction listAnnotationAction1 = getListAnnotationAction();
    listAnnotationAction1.setTarget(target);
    listAnnotationAction1.execute();

    final Annotation[] annotations2 = listAnnotationAction1.getAnnotations();

    final String content = getAnnotationService().getAnnotatedContent(target);
    log.debug("Annotated content = " + content);

    assertTrue(content.contains(context1Body));
    assertTrue(content.contains(context2Body));
    assertTrue(content.contains(context3Body));

    for (final Annotation annotation : annotations2) {
      final DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotation.getId(), true);
      deleteAnnotationAction.execute();
    }
  }

  private Annotation retrieveAnnotation(final String annotationId) throws Exception {
    final GetAnnotationAction getAnnotationAction = getGetAnnotationAction();
    getAnnotationAction.setAnnotationId(annotationId);
    assertEquals(Action.SUCCESS, getAnnotationAction.execute());
    return getAnnotationAction.getAnnotation();
  }

  private DeleteAnnotationAction getDeleteAnnotationAction(final String annotationId, final boolean deletePreceding) {
    final DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction();
    deleteAnnotationAction.setAnnotationId(annotationId);
    deleteAnnotationAction.setDeletePreceding(deletePreceding);
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

  private CreateAnnotationAction getCreateAnnotationAction(final String target, final String title, final String body) {
    return getCreateAnnotationAction(target, title, "text/plain", body);
  }

  private CreateAnnotationAction getCreateAnnotationAction(final String target, final String title, final String mimeType, final String body) {
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction();
    createAnnotationAction.setTitle(title);
    createAnnotationAction.setTarget(target);
    createAnnotationAction.setStartPath("/");
    createAnnotationAction.setStartOffset(0);
    createAnnotationAction.setEndPath("/");
    createAnnotationAction.setEndOffset(2);
    createAnnotationAction.setMimeType(mimeType);
    createAnnotationAction.setBody(body);
    return createAnnotationAction;
  }
}
