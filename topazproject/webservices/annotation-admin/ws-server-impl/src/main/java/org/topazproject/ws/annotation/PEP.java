package org.topazproject.ws.annotation;

import java.io.IOException;

import java.net.URI;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import javax.xml.rpc.server.ServletEndpointContext;

import org.topazproject.xacml.DenyBiasedPEP;
import org.topazproject.xacml.PDPFactory;
import org.topazproject.xacml.ServletEndpointContextAttribute;
import org.topazproject.xacml.Util;

import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.Subject;

/**
 * The XACML PEP for Annotation Web Service.
 *
 * @author Pradeep Krishnan
 */
public class PEP extends DenyBiasedPEP {
  /**
   * The attribute id that represents an annotation-id in XACML policies.
   */
  public static final String ANNOTATION_ID =
    "urm:topazproject:names:tc:xacml:1.0:resource:annotation-id";

  /**
   * The attribute id that represents the resource that is annotated on in XACML policies.
   */
  public static final String ANNOTATION_ON =
    "urn:topazproject:names:tc:xacml:1.0:resource:annotation-on";

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
   * The obligation that represents the query used in listAnnotations in XACML policies.
   */
  public static final String LIST_ANNOTATIONS_QUERY_OBLIGATION =
    "urn:topazproject:names:tc:xacml:1.0:obligation:list-annotations-query";
  private static final URI   ANNOTATION_ID_URI   = URI.create(ANNOTATION_ID);
  private static final URI   ANNOTATION_ON_URI   = URI.create(ANNOTATION_ON);
  private static Set         envAttrs            = new HashSet();
  private static Map         actionAttrsMap      = new HashMap();
  private static Map         knownObligationsMap = new HashMap();

  static {
    actionAttrsMap.put(CREATE_ANNOTATION, Util.createActionAttrs(CREATE_ANNOTATION));
    actionAttrsMap.put(DELETE_ANNOTATION, Util.createActionAttrs(DELETE_ANNOTATION));
    actionAttrsMap.put(GET_ANNOTATION_INFO, Util.createActionAttrs(GET_ANNOTATION_INFO));
    actionAttrsMap.put(SET_ANNOTATION_INFO, Util.createActionAttrs(SET_ANNOTATION_INFO));
    actionAttrsMap.put(LIST_ANNOTATIONS, Util.createActionAttrs(LIST_ANNOTATIONS));

    HashSet noKnownObligations = new HashSet();
    knownObligationsMap.put(CREATE_ANNOTATION, noKnownObligations);
    knownObligationsMap.put(DELETE_ANNOTATION, noKnownObligations);
    knownObligationsMap.put(GET_ANNOTATION_INFO, noKnownObligations);
    knownObligationsMap.put(SET_ANNOTATION_INFO, noKnownObligations);

    HashSet set = new HashSet();
    set.add(URI.create(LIST_ANNOTATIONS_QUERY_OBLIGATION));
    knownObligationsMap.put(LIST_ANNOTATIONS, set);
  }

  private Set subjectAttrs;

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
    // Get the servlet context.
    ServletContext servletContext = context.getServletContext();

    // Get the PDP factory for this web-app.
    PDPFactory factory = PDPFactory.getInstance(servletContext);

    // Get the default PDP for now. May be we should make it configurable.
    PDP pdp = factory.getDefaultPDP();

    // Use this pdp to evaluate requests against. 
    setPDP(pdp);

    initSubjectAttrs(context);
  }

  /**
   * Initializes the set of subject attributes used in the xacml evaluation request. The only
   * subject attribute we know of is the JAX-RPC end point context.
   *
   * @param context
   */
  private void initSubjectAttrs(ServletEndpointContext context) {
    ServletEndpointContextAttribute value = new ServletEndpointContextAttribute(context);
    Attribute                       attr =
      new Attribute(ServletEndpointContextAttribute.ID, null, null, value);
    Subject                         sub = new Subject(Collections.singleton(attr));
    subjectAttrs = Collections.singleton(new Subject(Collections.singleton(attr)));
  }

  /**
   * Checks if access to an Annotation service operation is permitted.
   *
   * @param action The Annotation service operation
   * @param on The resource that is annotated
   * @param id The annotation id or <code>null</code>
   *
   * @return A set of XACML obligations that needs to be satisfied.
   *
   * @throws SecurityException if the operation is not permitted
   */
  public Set checkAccess(String action, String on, String id) {
    Set actionAttrs = (Set) actionAttrsMap.get(action);

    if (actionAttrs == null)
      throw new SecurityException("Unknown action '" + action + "' for PEP");

    Set resourceAttrs = new HashSet();

    if (on != null)
      resourceAttrs.add(new Attribute(ANNOTATION_ON_URI, null, null, new StringAttribute(on)));
    else
      on = "";

    if (id != null)
      resourceAttrs.add(new Attribute(ANNOTATION_ID_URI, null, null, new StringAttribute(id)));
    else
      id = "";

    // Must have a RESOURCE_ID 
    resourceAttrs.add(new Attribute(Util.RESOURCE_ID, null, null,
                                    new StringAttribute("Annotation {{" + on + "}, {" + id + "}}")));

    return evaluate(new RequestCtx(subjectAttrs, resourceAttrs, actionAttrs, envAttrs),
                    (Set) knownObligationsMap.get(action));
  }
}
