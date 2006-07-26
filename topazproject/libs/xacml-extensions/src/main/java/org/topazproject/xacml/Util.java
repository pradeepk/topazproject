package org.topazproject.xacml;

import java.io.IOException;

import java.net.URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.xml.rpc.server.ServletEndpointContext;

import org.topazproject.configuration.ConfigurationStore;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.ctx.StatusDetail;
import com.sun.xacml.ctx.Subject;

/**
 * XACML related Utility functions.
 *
 * @author Pradeep Krishnan
 */
public class Util {
  /**
   * Resource URI that must be present in all xacml requests.
   */
  public static final URI RESOURCE_ID = URI.create(EvaluationCtx.RESOURCE_ID);

  /**
   * The Action URI that must be present in all xacml requests.
   */
  public static final URI ACTION_ID = URI.create("urn:oasis:names:tc:xacml:1.0:action:action-id");

  /**
   * The special value for when looking up a PDP in the config to indicate the default PDP. Example
   * config entry:
   * <pre>
   *   topaz.annotations.pdpName={@value}
   * </pre>
   */
  public static final String SN_DEFAULT_PDP = "_default_";

  /**
   * Creates a set of action attributes for use in a XACML request.
   *
   * @param action The value of {@link ACTION_ID} attribute
   *
   * @return the singleton set containing the action-id attribute
   */
  public static Set createActionAttrs(String action) {
    AttributeValue value = new StringAttribute(action);
    Attribute      attr = new Attribute(ACTION_ID, null, null, value);

    return Collections.singleton(attr);
  }

  /**
   * Creates an EvaluationResult for an attribute finder processing error.
   *
   * @param message the error message
   * @param type the type-URI of the attribute
   * @param id the id of the attribute
   *
   * @return Returns the EvaluationResult with the processing error status code
   */
  public static EvaluationResult processingError(String message, URI type, URI id) {
    // Note: This form of attribute constructor is marked as deprecated.
    // However this is the exact functionality that we want. Also
    // note that even though the documentation says a null can be passed for
    // AttributeValue, the encode() method assumes the value not to be null.
    // So we use an empty StringAttribute here.
    Attribute    attr   = new Attribute(id, type, null, null, new StringAttribute(""));
    StatusDetail detail = new StatusDetail(Collections.singletonList(attr));
    Status       status =
      new Status(Collections.singletonList(Status.STATUS_PROCESSING_ERROR), message, detail);

    return new EvaluationResult(status);
  }

  /**
   * Converts an Object to an AttributeValue. This can be improved to make use of a java type to
   * XACML type conversion mapping. Currently it depends on the <code>toString</code> method
   * matching the <code>getInstance</code> method of AttributeProxy. Should work mostly for
   * string, integer and boolean types.
   *
   * @param type the xacml attribute type identifier
   * @param o the object to be converted
   *
   * @return Returns the AttributeValue or null
   *
   * @throws UnknownIdentifierException propagated from AtttributeFactory
   * @throws ParsingException propagated from AttributeFactory
   */
  public static AttributeValue toAttributeValue(URI type, Object o)
                                         throws UnknownIdentifierException, ParsingException {
    if (o == null)
      return null;

    if (o instanceof AttributeValue)
      return (AttributeValue) o;

    if (o instanceof Collection)
      return toAttributeValue(type, (Collection) o);

    if (o.getClass().isArray())
      return toAttributeValue(type, (Object[]) o);

    return AttributeFactory.getInstance().createValue(type, o.toString());
  }

  /**
   * Converts a Collection of objects to a BagAttributeValue.
   *
   * @param type the xacml attribute type identifier
   * @param o the collection to be converted
   *
   * @return Returns a bag of AttributeValues
   *
   * @throws UnknownIdentifierException propagated from AtttributeFactory
   * @throws ParsingException propagated from AttributeFactory
   */
  public static BagAttribute toAttributeValue(URI type, Collection o)
                                       throws UnknownIdentifierException, ParsingException {
    Collection c  = new ArrayList(o.size());
    Iterator   it = o.iterator();

    while (it.hasNext()) {
      c.add(toAttributeValue(type, it.next()));
    }

    return new BagAttribute(type, c);
  }

  /**
   * Converts an array of objects to a BagAttributeValue.
   *
   * @param type the xacml attribute type identifier
   * @param o the collection to be converted
   *
   * @return Returns a bag of AttributeValues
   *
   * @throws UnknownIdentifierException propagated from AtttributeFactory
   * @throws ParsingException propagated from AttributeFactory
   */
  public static BagAttribute toAttributeValue(URI type, Object[] o)
                                       throws UnknownIdentifierException, ParsingException {
    Collection c = new ArrayList(o.length);

    for (int i = 0; i < o.length; i++)
      c.add(toAttributeValue(type, o[i]));

    return new BagAttribute(type, c);
  }

  /**
   * Creates the set of subject attributes used in the xacml evaluation request from the given
   * web-server context. The only subject attribute we know of is the JAX-RPC end point context.
   *
   * @param context the web-service context
   *
   * @return the subject attributes
   */
  public static Set createSubjAttrs(ServletEndpointContext context) {
    Attribute attr =
      new Attribute(ServletEndpointContextAttribute.ID, null, null,
                    new ServletEndpointContextAttribute(context));

    return Collections.singleton(new Subject(ServletEndpointContextAttribute.CATEGORY,
                                             Collections.singleton(attr)));
  }

  /**
   * Look up the PDP for the given service.
   *
   * @param context the web-service context; used to locate the PDP configuration
   * @param pdpProp the name of config property defining name of the PDP configuration to use; the
   *        value must be the name of valid PDP configuration, or the special value {@link
   *        #SN_DEFAULT_PDP SN_DEFAULT_PDP} to indicate the default PDP config.
   *
   * @return the PDP
   *
   * @throws IOException on error in accessing the PDP config file
   * @throws ParsingException on error in parsing the PDP config file
   * @throws UnknownIdentifierException if no config entry named <var>pdpProp</var> is found or if
   *         the entry's value does not identify a valid PDP config
   */
  public static PDP lookupPDP(ServletEndpointContext context, String pdpProp)
                       throws IOException, ParsingException, UnknownIdentifierException {
    String pdpName = ConfigurationStore.getInstance().getConfiguration().getString(pdpProp, null);

    if (pdpName == null)
      throw new UnknownIdentifierException("No config entry named '" + pdpProp + "' found");

    PDPFactory factory = PDPFactory.getInstance(context.getServletContext());

    return pdpName.equals(SN_DEFAULT_PDP) ? factory.getDefaultPDP() : factory.getPDP(pdpName);
  }
}
