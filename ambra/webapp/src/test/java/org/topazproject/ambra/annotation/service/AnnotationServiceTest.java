/* $HeadURL$
 * $Id$
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

package org.topazproject.ambra.annotation.service;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.MinorCorrection;
import org.topazproject.ambra.cache.MockCache;
import org.topazproject.ambra.cache.CacheManager;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.otm.Session;
import org.topazproject.otm.Query;
import org.topazproject.otm.query.Results;

import org.easymock.classextension.IMocksControl;
import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.isA;

import com.sun.xacml.PDP;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;

import java.net.URI;

/**
 * @author Dragisa Krsmanovic
 */
public class AnnotationServiceTest {

  @Test
  public void listAnnotations() {

    AnnotationService annotationService = new AnnotationService();

    IMocksControl ctl = createControl();

    PDP pdp = ctl.createMock(PDP.class);
    Session session = ctl.createMock(Session.class);
    Query query = ctl.createMock(Query.class);
    Results results = ctl.createMock(Results.class);
    CacheManager cacheManager = ctl.createMock(CacheManager.class);
    MockCache cache = new MockCache();


    String target = "info:doi/10.1371/journal.pone.0002250";
    String queryString = "select an.id from ArticleAnnotation an where an.annotates = :target and an.mediator = :mediator;";
    String applicationId = "test-app";
    URI annotation1id = URI.create("info:doi/10.1371/annotation1");
    URI annotation2id = URI.create("info:doi/10.1371/annotation2");
    URI annotation3id = URI.create("info:doi/10.1371/annotation3");

    Comment comment = new Comment();
    comment.setId(annotation1id);
    FormalCorrection formalCorrection = new FormalCorrection();
    formalCorrection.setId(annotation2id);
    MinorCorrection minorCorrection = new MinorCorrection();
    minorCorrection.setId(annotation3id);

    ArticleAnnotation[] expected = {comment, formalCorrection, minorCorrection};

    cacheManager.registerListener(isA(AbstractObjectListener.class));
    expectLastCall().times(0,1);

    expect(pdp.evaluate(isA(EvaluationCtx.class)))
        .andReturn(new ResponseCtx(new Result(Result.DECISION_PERMIT)))
        .anyTimes();

    expect(session.getFlushMode())
        .andReturn(Session.FlushMode.always);
    session.flush();

    expect(session.createQuery(queryString))
        .andReturn(query);
    expect(query.setParameter("target", target))
        .andReturn(query);
    expect(query.setParameter("mediator", applicationId))
        .andReturn(query);
    expect(query.execute())
        .andReturn(results);

    expect(results.next())
        .andReturn(true).times(3)
        .andReturn(false);
    expect(results.getURI(0))
        .andReturn(annotation1id)
        .andReturn(annotation2id)
        .andReturn(annotation3id);


    expect(session.get(ArticleAnnotation.class, annotation1id.toString()))
        .andReturn(comment);
    expect(session.get(ArticleAnnotation.class, annotation2id.toString()))
        .andReturn(formalCorrection);
    expect(session.get(ArticleAnnotation.class, annotation3id.toString()))
        .andReturn(minorCorrection);

    ctl.replay();

    cache.setCacheManager(cacheManager);

    annotationService.setArticleAnnotationCache(cache);
    annotationService.setAnnotationsPdp(pdp);
    annotationService.setOtmSession(session);
    annotationService.setApplicationId(applicationId);

    ArticleAnnotation[] annotations = annotationService.listAnnotations(
        target,
        AnnotationService.ALL_ANNOTATION_CLASSES);

    assertEquals(annotations, expected);

    ctl.verify();
  }
}
