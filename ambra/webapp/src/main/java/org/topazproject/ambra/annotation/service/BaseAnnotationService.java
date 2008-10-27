/* $HeadURL::                                                                            $
 * $Id$
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
package org.topazproject.ambra.annotation.service;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.models.Annotea;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.Correction;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.MinorCorrection;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.permission.service.PermissionsService;
import org.topazproject.otm.Session;

/**
 * Base class for Annotaion and Reply web service wrappers
 */
public abstract class BaseAnnotationService {
  private static final Log log = LogFactory.getLog(BaseAnnotationService.class);

  private String defaultType;
  private String encodingCharset = "UTF-8";
  private String applicationId;
  private boolean isAnonymous;
  protected Session session;
  protected PermissionsService permissionsService;

  public final Set<Class<? extends ArticleAnnotation>> CORRECTION_SET =
  new HashSet<Class<? extends ArticleAnnotation>>();

  public final Set<Class<? extends ArticleAnnotation>> COMMENT_SET =
  new HashSet<Class<? extends ArticleAnnotation>>();
  public static final String WEB_TYPE_COMMENT = "Comment";
  public static final String WEB_TYPE_NOTE = "Note";
  public static final String WEB_TYPE_FORMAL_CORRECTION = "FormalCorrection";
  public static final String WEB_TYPE_MINOR_CORRECTION = "MinorCorrection";
  public static final String WEB_TYPE_REPLY = "Reply";
  public static final String WEB_TYPE_RATING = "Rating";

  {
    CORRECTION_SET.add(Correction.class);
    COMMENT_SET.add(Comment.class);
  }

  /**
   * Set the default annotation type.
   * @param defaultType defaultType
   */
  public void setDefaultType(final String defaultType) {
    this.defaultType = defaultType;
  }

  /**
   * Set the id of the application
   * @param applicationId applicationId
   */
  @Required
  public void setApplicationId(final String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   * @return the encoding charset
   */
  public String getEncodingCharset() {
    return encodingCharset;
  }

  /**
   * @param encodingCharset charset for encoding the data to be persisting in
   */
  public void setEncodingCharset(final String encodingCharset) {
    this.encodingCharset = encodingCharset;
  }

  /**
   * Set whether the user isAnonymous.
   * @param isAnonymous true if user isAnonymous
   */
  public void setAnonymous(final boolean isAnonymous) {
    this.isAnonymous = isAnonymous;
  }

  public boolean isAnonymous() {
    return isAnonymous;
  }

  /**
   * @return the default type for the annotation or reply
   */
  public String getDefaultType() {
    return defaultType;
  }

  /**
   * @return the application id
   */
  public String getApplicationId() {
    return applicationId;
  }

  protected String getContentType(final String mimeType) {
    return mimeType + ";charset=" + getEncodingCharset();
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

  /**
   * Set the PermissionsService
   *
   * @param permissionsService permissionWebService
   */
  @Required
  public void setPermissionsService(final PermissionsService permissionsService) {
    this.permissionsService = permissionsService;
  }

  /**
   * Returns the PubApp type name for the given Annotea object.
   * @param ann the Annotea base class
   * @return the type
   */
  @Transactional(readOnly = true)
  public static String getWebType(Annotea<?> ann) {
    if (ann == null || ann.getType() == null){
      return null;
    }

    if (ann.getType().equals(MinorCorrection.RDF_TYPE)) {
      return WEB_TYPE_MINOR_CORRECTION;
    }
    if (ann.getType().equals(FormalCorrection.RDF_TYPE)) {
      return WEB_TYPE_FORMAL_CORRECTION;
    }
    if (ann.getType().equals(Rating.RDF_TYPE)) {
      return WEB_TYPE_RATING;
    }
    if (ann.getType().equals(Reply.RDF_TYPE)) {
      return WEB_TYPE_REPLY;
    }
    if (ann.getType().equals(Comment.RDF_TYPE)) {
      if (((ArticleAnnotation)ann).getContext() != null) {
        return WEB_TYPE_NOTE;
      } else {
        return WEB_TYPE_COMMENT;
      }
    }

    log.error("Unable to determine annotation WEB_TYPE. Annotation ID='" + ann.getId() +
              "' ann.getType() = '" + ann.getType() + "'");
    return null;
  }
}
