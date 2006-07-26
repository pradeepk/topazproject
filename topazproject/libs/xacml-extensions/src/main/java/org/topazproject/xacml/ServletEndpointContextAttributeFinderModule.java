package org.topazproject.xacml;

import java.net.URI;

import java.security.Principal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import javax.xml.rpc.server.ServletEndpointContext;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.finder.AttributeFinderModule;

/**
 * An AttributeFinderModule that can lookup attributes from ServletEndpointContext.
 *
 * @author Pradeep Krishnan
 */
public class ServletEndpointContextAttributeFinderModule extends AttributeFinderModule {
  // Default Subject Category
  public static final URI SUBJECT_CATEGORY_DEFAULT_URI =
    URI.create(AttributeDesignator.SUBJECT_CATEGORY_DEFAULT);

  // HttpSession Subject Category
  public static final URI SUBJECT_CATEGORY_HTTPSESSION_URI =
    URI.create(HttpSession.class.getName());

  // ServletContext Subject Category
  public static final URI  SUBJECT_CATEGORY_SERVLETCONTEXT_URI =
    URI.create(ServletContext.class.getName());
  public static final URI  SUBJECT_ID_URI =
    URI.create("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
  private static final Map resolvers                = new HashMap();
  private static final Set supportedDesignatorTypes =
    Collections.singleton(new Integer(AttributeDesignator.SUBJECT_TARGET));

  {
    resolvers.put(SUBJECT_CATEGORY_DEFAULT_URI, new DefaultResolver());
    resolvers.put(SUBJECT_CATEGORY_HTTPSESSION_URI, new HttpSessionResolver());
    resolvers.put(SUBJECT_CATEGORY_SERVLETCONTEXT_URI, new ServletContextResolver());
  }

  /**
   * Supports attribute designators.
   *
   * @return Returns true always.
   */
  public boolean isDesignatorSupported() {
    return true;
  }

  /**
   * Returns the attribute designator types supported by this module.
   *
   * @return Returns a singleton set indicating Subject Attribute Designator support.
   */
  public Set getSupportedDesignatorTypes() {
    return supportedDesignatorTypes;
  }

  /**
   * @see com.sun.xacml.finder.AttributeFinderModule#findAttribute
   */
  public EvaluationResult findAttribute(URI type, URI id, URI issuer, URI category,
                                        EvaluationCtx context, int designatorType) {
    // Issuer must be null to match our attributes
    if (issuer != null)
      return new EvaluationResult(BagAttribute.createEmptyBag(type));

    // null category is to be treated as default
    if (category == null)
      category = SUBJECT_CATEGORY_DEFAULT_URI;

    // See if we have a resolver
    Resolver resolver = (Resolver) resolvers.get(category);

    if (resolver == null)
      return new EvaluationResult(BagAttribute.createEmptyBag(type));

    // Get the JAX-RPC context.
    EvaluationResult result =
      context.getSubjectAttribute(ServletEndpointContextAttribute.TYPE,
                                  ServletEndpointContextAttribute.ID, null);

    // Abort policy evaluation if there is a failure in look up.
    if (result.indeterminate())
      return result;

    BagAttribute bag = (BagAttribute) result.getAttributeValue();

    // Return an empty bag if JAX-RPC context could not be located.
    // Note: We don't abort the processing here. Let us give
    //       other AttributeFinderModules a chance.
    if (bag.isEmpty())
      return new EvaluationResult(BagAttribute.createEmptyBag(type));

    // Retrieve the JAX-RPC context.
    ServletEndpointContext epc =
      ((ServletEndpointContextAttribute) bag.iterator().next()).getValue();

    AttributeValue         value;

    try {
      // Now resolve a value for the id.
      value = resolver.resolve(epc, type, id);
    } catch (Exception e) {
      // Abort the policy evaluation. For a deny-biased PEP, this will result in an
      // access-denied.
      return Util.processingError(e.getMessage(), type, id);
    }

    // Return an empty bag if a value could not be resolved.
    if (value == null)
      return new EvaluationResult(BagAttribute.createEmptyBag(type));

    // Create a bag for singletons.
    if (!value.isBag())
      value = new BagAttribute(type, Collections.singleton(value));

    // Now return the bag as our result.
    return new EvaluationResult(value);
  }

  private static interface Resolver {
    /**
     * Resolves the id to a value. Couple of things to note:
     * 
     * <ul>
     * <li>
     * If an exception is thrown, PDP will abort the policy evaluation even if  the
     * <code>mustBePresent</code> option is false.
     * </li>
     * <li>
     * The attribute finder should therefore assume that the value look-up was optional and return
     * null/empty-bag values as far as possible.
     * </li>
     * <li>
     * Throw the UnknownIdentifierException only when you want to convey to the policy author that
     * clearly there is an error in the type-URI used in policy.
     * </li>
     * <li>
     * Throw the ParsingException only when you want to convey to the policy author that there is a
     * mismatch between the way a value is used in a policy and is avaliable in code. ie. a value
     * cannot be parsed back to an appropiate AttributeValue object.
     * </li>
     * </ul>
     * 
     *
     * @param context The ServletEndpointContext
     * @param type The data type of the value
     * @param id The id that is to be resolved
     *
     * @return Returns the attribute value or null
     *
     * @throws UnknownIdentifierException when the type used in policy does not match the data type
     *         that we have and a reliable type conversion is not possible.
     * @throws ParsingException when the value could not be converted to an AttributeValue
     */
    AttributeValue resolve(ServletEndpointContext context, URI type, URI id)
                    throws UnknownIdentifierException, ParsingException;
  }

  /**
   * Default subject category resolver. Currently resolves SUBJECT_ID to a User Principal.
   */
  private static class DefaultResolver implements Resolver {
    public AttributeValue resolve(ServletEndpointContext context, URI type, URI id)
                           throws UnknownIdentifierException, ParsingException {
      if (!SUBJECT_ID_URI.equals(id))
        return null;

      Principal principal = context.getUserPrincipal();

      if (principal == null)
        return null;

      return Util.toAttributeValue(type, principal.getName());
    }
  }

  /**
   * Resolver that makes attributes in HttpSession available for use in XACML policies.
   */
  private static class HttpSessionResolver implements Resolver {
    public AttributeValue resolve(ServletEndpointContext context, URI type, URI id)
                           throws UnknownIdentifierException, ParsingException {
      HttpSession session = context.getHttpSession();

      if (session == null)
        return null;

      return Util.toAttributeValue(type, session.getAttribute(id.toString()));
    }
  }

  /**
   * Resolver that makes attributes in ServletContext available for use in XACML policies.
   */
  private static class ServletContextResolver implements Resolver {
    public AttributeValue resolve(ServletEndpointContext epc, URI type, URI id)
                           throws UnknownIdentifierException, ParsingException {
      ServletContext context = epc.getServletContext();

      if (context == null)
        return null;

      return Util.toAttributeValue(type, context.getAttribute(id.toString()));
    }
  }
}
