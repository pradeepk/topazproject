/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

package org.topazproject.ambra.admin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.annotation.service.AnnotationConverter;
import org.topazproject.ambra.annotation.service.ArticleAnnotationService;
import org.topazproject.ambra.annotation.service.Flag;
import org.topazproject.ambra.models.Annotea;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.models.Reply;
import org.topazproject.otm.Query;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.Results;

/**
 * @author alan
 * Manage documents on server. Ingest and access ingested documents.
 */
public class FlagManagementService {
  private static final Log log = LogFactory.getLog(FlagManagementService.class);
  private ArticleAnnotationService articleAnnotationService;
  private AnnotationConverter converter;
  private Session session;

  @Transactional(readOnly = true)
  public Collection<FlaggedCommentRecord> getFlaggedComments() throws ApplicationException {
    ArrayList<FlaggedCommentRecord> commentrecords = new ArrayList<FlaggedCommentRecord>();

    // Note that Annotea does not have an rdf:type. So we check exists(annotea:body).
    // FIXME: the 'order by' hack is really to force a check like exists(a.body)
    Query query = session.createQuery(
        "select a, (select f.id from Comment f where f.annotates = a), b "
        + "from Annotea a where b := a.body order by b;");

    Results r = query.execute();

    while (r.next()) {
      Results f = r.getSubQueryResults(1);
      List<Flag> flags = new ArrayList<Flag>();
      while (f.next()) {
        String id = f.getString(0);
        try {
          ArticleAnnotation ann = articleAnnotationService.getAnnotation(id);
          if (ann != null)
            flags.add(new Flag(converter.convert(ann, true, true)));
        } catch (SecurityException e) {
          if (log.isInfoEnabled())
            log.info("No permission to load Flag: " + id, e);
        }
      }

      if (flags.size() == 0)
        continue;

      Annotea<?> a = (Annotea<?>) r.get(0);
      String title = (a instanceof Rating) ? ((Rating)a).getBody().getCommentTitle()
                                           : a.getTitle();
      String root  = (a instanceof Reply) ? ((Reply)a).getRoot() : null;
      String wt    = a.getWebType();

      for (Flag flag : flags) {
        FlaggedCommentRecord fcr =
          new FlaggedCommentRecord(
              flag.getId(),
              flag.getAnnotates(),
              title,
              flag.getComment(),
              flag.getCreated(),
              flag.getCreatorName(),
              flag.getCreator(),
              root,
              flag.getReasonCode(),
              wt);
        commentrecords.add(fcr);
      }
    }

    Collections.sort(commentrecords);
    return commentrecords;
  }

  public void setAnnotationService(
      ArticleAnnotationService articleAnnotationService) {
    this.articleAnnotationService = articleAnnotationService;
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  @Required
  public void setAnnotationConverter(AnnotationConverter converter) {
    this.converter = converter;
  }

}
