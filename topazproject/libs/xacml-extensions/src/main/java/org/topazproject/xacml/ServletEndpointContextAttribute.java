package org.topazproject.xacml;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.rpc.server.ServletEndpointContext;

import com.sun.xacml.attr.AttributeValue;

/**
 * AttributeType used for looking up the servlet endpoint context. Used as a Subject Attribute  by
 * a web-service and submitted in the request to PDP. Not for use directly in policies.  An
 * <code>AttributeFinderModule</code> that needs values from <code>ServletEndPointContext</code>
 * will look it up in the <code>EvaluationCtx</code>.
 *
 * @author Pradeep Krishnan
 */
public class ServletEndpointContextAttribute extends AttributeValue {
  /**
   * The type (attribute-type) of this AttributeValue.
   */
  public static final URI TYPE = Util.createUri(ServletEndpointContext.class.getName());

  /**
   * A convenient id for use by attribute finder modules.
   */
  public static final URI        ID    = TYPE;
  private ServletEndpointContext value;

  /**
   * Creates a new ServletEndPointContextAttribute object.
   *
   * @param value the value represented
   */
  public ServletEndpointContextAttribute(ServletEndpointContext value) {
    super(TYPE);
    this.value = value;
  }

  /**
   * @see com.sun.xacml.attr.AttributeValue#encode
   */
  public String encode() {
    throw new UnsupportedOperationException("Cannot be used in a XACML policy");
  }

  /**
   * Returns the value represented by this object.
   *
   * @return the value
   */
  public ServletEndpointContext getValue() {
    return value;
  }
}
