package org.topazproject.ws.annotation;

import java.io.IOException;

import javax.xml.rpc.server.ServletEndpointContext;

import org.topazproject.xacml.AbstractSimplePEP;
import org.topazproject.xacml.Util;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for Annotation Web Service.
 *
 * @author Pradeep Krishnan
 */
public class PEP extends AbstractSimplePEP {
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

  static {
    init(PEP.class,
         new String[] {
           CREATE_ANNOTATION,
           DELETE_ANNOTATION,
           GET_ANNOTATION_INFO,
           SET_ANNOTATION_INFO,
           LIST_ANNOTATIONS,
           LIST_ANNOTATIONS_IN_STATE,
           SET_ANNOTATION_STATE
         },
         new String[][] {
           null,
           null,
           null,
           null,
           { LIST_ANNOTATIONS_QUERY_OBLIGATION },
           null,
           null
         }
         );
  }

  /**
   * Creates a new PEP object.
   *
   * @param context The JAX-RPC end point context to which this PEP is associated with.
   *
   * @throws IOException when a PDP could configuration file could not be accessed
   * @throws ParsingException on an error in parsing a PDP configuration
   * @throws UnknownIdentifierException when an unknown identifier was found in PDP configuration
   */
  public PEP(ServletEndpointContext context)
      throws IOException, ParsingException, UnknownIdentifierException {
    super(Util.lookupPDP(context, "topaz.annotations.pdpName"), Util.createSubjAttrs(context));
  }
}
