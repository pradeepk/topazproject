package org.topazproject.ws.annotation.impl;

import java.io.IOException;

import java.util.Set;

import org.topazproject.xacml.AbstractSimplePEP;
import org.topazproject.xacml.Util;

import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for Annotation Web Service.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationsPEP extends AbstractSimplePEP {
  /**
   * The action that represents a createAnnotation operation in XACML policies.
   */
  public static final String CREATE_ANNOTATION = "createAnnotation";

  /**
   * The action that represents a deleteAnnotation operation in XACML policies.
   */
  public static final String DELETE_ANNOTATION = "deleteAnnotation";

  /**
   * The action that represents a getAnnotation operation in XACML policies.
   */
  public static final String GET_ANNOTATION_INFO = "getAnnotationInfo";

  /**
   * The action that represents a setAnnotation operation in XACML policies.
   */
  public static final String SET_ANNOTATION_INFO = "setAnnotationInfo";

  /**
   * The action that represents a listAnnotations operation in XACML policies.
   */
  public static final String LIST_ANNOTATIONS = "listAnnotations";

  /**
   * The action that represents a listAnnotations operation in XACML policies.
   */
  public static final String LIST_ANNOTATIONS_IN_STATE = "listAnnotationsInState";

  /**
   * The action that represents a listAnnotations operation in XACML policies.
   */
  public static final String SET_ANNOTATION_STATE = "setAnnotationState";

  /**
   * The obligation that represents the query used in listAnnotations in XACML policies.
   */
  public static final String LIST_ANNOTATIONS_QUERY_OBLIGATION =
    "urn:topazproject:names:tc:xacml:1.0:obligation:list-annotations-query";

  /**
   * The list of all supported actions
   */
  public static final String[] SUPPORTED_ACTIONS =
    new String[] {
                   CREATE_ANNOTATION, DELETE_ANNOTATION, GET_ANNOTATION_INFO, SET_ANNOTATION_INFO,
                   LIST_ANNOTATIONS, LIST_ANNOTATIONS_IN_STATE, SET_ANNOTATION_STATE
    };

  /**
   * The list of all supported obligations
   */
  public static final String[][] SUPPORTED_OBLIGATIONS =
    new String[][] {null,
                     null,
                     null,
                     null,
                     { LIST_ANNOTATIONS_QUERY_OBLIGATION },
                     null,
                     null
    };

  /*
   *    *@see org.topazproject.xacml.AbstractSimplePEP
   *
   */
  protected AnnotationsPEP(PDP pdp, Set subjAttrs)
                   throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }
}
