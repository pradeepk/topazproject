
package org.topazproject.xacml;

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
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.Subject;

/**
 * A superclass for simple XACML PEP's.
 *
 * @author Pradeep Krishnan
 * @author Ronald Tschalär
 */
public abstract class AbstractSimplePEP extends DenyBiasedPEP {
  private static Map         envAttrsMap         = new HashMap();
  private static Map         actionAttrsMap      = new HashMap();
  private static Map         knownObligationsMap = new HashMap();

  protected final Set subjectAttrs;

  /** 
   * Initialize the list of supported actions and their obligations. 
   * 
   * @param clazz       the subclass for which the actions and obligations are being initialized.
   *                    Note that {@link #evaluate evaluate} uses <code>getClass()</code> to look
   *                    up these actions and obligations.
   * @param actions     the list of known/supported actions
   * @param obligations the obligations for the actions. May be null in which case no action has any
   *                    obligations; otherwise it must an array of the same length as
   *                    <var>actions</var>. Each element of the array lists the obligations for the
   *                    action at the same index in the <var>actions</var> array; an element may be
   *                    null to indicate no obligations for that action.
   */
  protected synchronized static void init(Class clazz, String[] actions, String[][] obligations) {
    envAttrsMap.put(clazz, Collections.EMPTY_SET);

    Map aa = new HashMap();
    for (int idx = 0; idx < actions.length; idx++)
      aa.put(actions[idx], Util.createActionAttrs(actions[idx]));
    actionAttrsMap.put(clazz, aa);

    if (obligations == null)
      obligations = new String[actions.length][0];

    Map ko = new HashMap();
    for (int idx = 0; idx < actions.length; idx++)
      ko.put(actions[idx], toURISet(obligations[idx]));
    knownObligationsMap.put(clazz, ko);
  }

  private static final Set toURISet(String[] uris) {
    if (uris == null || uris.length == 0)
      return Collections.EMPTY_SET;

    Set res = new HashSet();
    for (int idx = 0; idx < uris.length; idx++)
      res.add(URI.create(uris[idx]));
    return res;
  }

  private synchronized static Set envAttrs(Class clazz) {
    return (Set) envAttrsMap.get(clazz);
  }

  private synchronized static Map actionAttrs(Class clazz) {
    return (Map) actionAttrsMap.get(clazz);
  }

  private synchronized static Map knownObligations(Class clazz) {
    return (Map) knownObligationsMap.get(clazz);
  }

  /**
   * Creates a new PEP object.
   *
   * @param context The JAX-RPC end point context to which this PEP is associated with.
   * @throws IOException when a PDP could configuration file could not be accessed
   * @throws ParsingException on an error in parsing a PDP configuration
   * @throws UnknownIdentifierException when an unknown identifier was found in PDP configuration
   */
  protected AbstractSimplePEP(PDP pdp, Set subjAttrs)
      throws IOException, ParsingException, UnknownIdentifierException {
    setPDP(pdp);
    subjectAttrs = subjAttrs;
  }

  /** 
   * Check if the requested operation is allowed on the given resources.
   * 
   * @param action        the operation being attempted
   * @param resourceAttrs the resources being operated on
   * @return the set of XACML obligations that need to be satisfied
   * @throws SecurityException if the operation is not permitted
   */
  protected Set checkAccess(String action, Set resourceAttrs) throws SecurityException {
    Set actionAttrs = (Set) actionAttrs(getClass()).get(action);
    if (actionAttrs == null)
      throw new SecurityException("Unknown action '" + action + "' for PEP");

    return evaluate(new RequestCtx(subjectAttrs, resourceAttrs, actionAttrs, envAttrs(getClass())),
                    (Set) knownObligations(getClass()).get(action));
  }

  /** 
   * Check if the requested operation is allowed on the given resource.
   * 
   * @param action   the operation being attempted
   * @param resource the resource being operated on
   * @return the set of XACML obligations that need to be satisfied
   * @throws SecurityException if the operation is not permitted
   */
  public Set checkAccess(String action, URI resource) throws SecurityException {
    Set resourceAttrs = new HashSet();
    resourceAttrs.add(new Attribute(Util.RESOURCE_ID, null, null, new AnyURIAttribute(resource)));

    return checkAccess(action, resourceAttrs);
  }
}
