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

/**
 * Annotation related grants.
 * Copied from org.topazproject.ws.annotation.impl.AnnotationsPEP
 * Copied from org.topazproject.ws.annotation.impl.RepliesPEP
 */
public interface AnnotationPermission {
  String ALL_PRINCIPALS = "http://rdf.topazproject.org/RDF/permissions#all";

  interface Annotation {
  /**
   * The action that represents a createAnnotation operation in XACML policies.
   */
    String CREATE = "annotations:createAnnotation";

    /**
     * The action that represents a deleteAnnotation operation in XACML policies.
     */
    String DELETE = "annotations:deleteAnnotation";

    /**
     * The action that represents a getAnnotation operation in XACML policies.
     */
    String GET_INFO = "annotations:getAnnotationInfo";

    /**
     * The action that represents a supersede operation in XACML policies.
     */
    String SUPERSEDE = "annotations:supersede";

    /**
     * The action that represents a listAnnotations operation in XACML policies.
     * Note that this permission is checked against the a:annotates resource.
     */
    String LIST = "annotations:listAnnotations";

    /**
     * The action that represents a listAnnotations operation in XACML policies.
     * Note that this permission is checked against the base uri of annotations.
     */
    String LIST_IN_STATE = "annotations:listAnnotationsInState";

    /**
     * The action that represents a listAnnotations operation in XACML policies.
     */
    String SET_STATE = "annotations:setAnnotationState";
  }

  interface Reply {
  /**
   * The action that represents a createReply operation in XACML policies.
   */
    String CREATE = "replies:createReply";

    /**
     * The action that represents a deleteReply operation in XACML policies.
     */
    String DELETE = "replies:deleteReply";

    /**
     * The action that represents a getReply operation in XACML policies.
     */
    String GET_INFO = "replies:getReplyInfo";

    /**
     * The action that represents a listReplies operation in XACML policies.
     */
    String LIST = "replies:listReplies";

    /**
     * The action that represents a listAllReplies operation in XACML policies.
     */
    String LIST_ALL = "replies:listAllReplies";

    /**
     * The action that represents a listReplies operation in XACML policies. Note that this
     * permission is checked against the base uri of annotations.
     */
    String LIST_IN_STATE = "replies:listRepliesInState";

    /**
     * The action that represents a setReplyState operation in XACML policies.
     */
    String SET_STATE = "replies:setReplyState";
  }
}
