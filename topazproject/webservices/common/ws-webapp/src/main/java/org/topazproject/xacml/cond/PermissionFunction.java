/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.xacml.cond;

import java.net.URI;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import org.topazproject.common.impl.SimpleTopazContext;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.permissions.Permissions;
import org.topazproject.ws.permissions.impl.PermissionsImpl;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.cond.Function;
import com.sun.xacml.ctx.Status;

/**
 * A XACML extension function to execute a Permission service check. The arguments to the function
 * are:
 * 
 * <ul>
 * <li>
 * config: the configuration identifier for the itql service
 * </li>
 * <li>
 * resource: the resource being accessed
 * </li>
 * <li>
 * permission: the action being performed
 * </li>
 * <li>
 * principal: the principal accessing the resource
 * </li>
 * </ul>
 * 
 *
 * @author Pradeep Krishnan
 */
public abstract class PermissionFunction implements Function {
  /**
   * XACML function name for {@link org.topazproject.ws.permissions.Permissions#isGranted}
   * function.
   */
  public static final String FUNCTION_IS_GRANTED =
    "urn:topazproject:names:tc:xacml:1.0:function:is-granted";

  /**
   * XACML function name for {@link org.topazproject.ws.permissions.Permissions#isRevoked}
   * function.
   */
  public static final String FUNCTION_IS_REVOKED =
    "urn:topazproject:names:tc:xacml:1.0:function:is-revoked";

  /**
   * URI version of StringAttribute's identifier
   */
  protected static final URI STRING_TYPE = URI.create(StringAttribute.identifier);

  /**
   * URI version of BooleanAttribute's identifier
   */
  protected static final URI BOOLEAN_TYPE = URI.create(BooleanAttribute.identifier);

  // shared logger
  private static final Log log = LogFactory.getLog(PermissionFunction.class);

  // The identifier for this function.
  private URI identifier;

  // A List used by makeProcessingError() to save some steps.
  private static List            processingErrList = null;
  private static KeyedObjectPool pool;

  static {
    GenericKeyedObjectPool.Config poolConfig = new GenericKeyedObjectPool.Config();
    poolConfig.maxActive                       = 20;
    poolConfig.maxIdle                         = 20;
    poolConfig.maxTotal                        = 20;
    poolConfig.maxWait                         = 1000; // time to wait for handle if none available
    poolConfig.minEvictableIdleTimeMillis      = 5 * 60 * 1000; // 5 min
    poolConfig.minIdle                         = 0;
    poolConfig.numTestsPerEvictionRun          = 5;
    poolConfig.testOnBorrow                    = false;
    poolConfig.testOnReturn                    = false;
    poolConfig.testWhileIdle                   = true;
    poolConfig.timeBetweenEvictionRunsMillis   = 5 * 60 * 1000; // 5 min
    poolConfig.whenExhaustedAction             = GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK;

    pool = new GenericKeyedObjectPool(new PermissionsImplFactory(), poolConfig);
  }

  /**
   * Creates a new PermissionFunction object.
   *
   * @param functionName The function name as it appears in XACML policies
   */
  public PermissionFunction(String functionName) {
    identifier = URI.create(functionName);
  }

  /*
   * @see com.sun.xacml.cond.Function#checkInputs
   */
  public final void checkInputs(List inputs) {
    if ((inputs == null) || (inputs.size() < 4))
      throw new IllegalArgumentException("not enough arguments to " + identifier);

    Iterator it = inputs.iterator();

    for (int i = 0; it.hasNext(); i++) {
      Evaluatable eval = (Evaluatable) it.next();

      if (eval.evaluatesToBag())
        throw new IllegalArgumentException("illegal argument type. bags are not supported for "
                                           + identifier);
    }
  }

  /*
   * @see com.sun.xacml.cond.Function#checkInputsNoBag
   */
  public final void checkInputsNoBag(List inputs) {
    checkInputs(inputs);
  }

  /*
   * @see com.sun.xacml.cond.Function#evaluate
   */
  public final EvaluationResult evaluate(List inputs, EvaluationCtx context) {
    Iterator it = inputs.iterator();

    // First parameter is the config id for Itql service
    EvaluationResult result = ((Evaluatable) it.next()).evaluate(context);

    if (result.indeterminate())
      return result;

    String conf = result.getAttributeValue().encode();

    // Second parameter is the resource
    result = ((Evaluatable) it.next()).evaluate(context);

    if (result.indeterminate())
      return result;

    String resource = result.getAttributeValue().encode();

    // Third parameter is the permission
    result = ((Evaluatable) it.next()).evaluate(context);

    if (result.indeterminate())
      return result;

    String permission = result.getAttributeValue().encode();

    // Fourth parameter is the principal
    result = ((Evaluatable) it.next()).evaluate(context);

    if (result.indeterminate())
      return result;

    String      principal = result.getAttributeValue().encode();

    Permissions impl = null;

    try {
      impl = (Permissions) pool.borrowObject(conf);

      boolean ret = execute(impl, resource, permission, principal);

      return new EvaluationResult(BooleanAttribute.getInstance(ret));
    } catch (Exception e) {
      String msg =
        "Failed to execute " + identifier + "(" + resource + ", " + permission + ", " + principal
        + ")";
      log.warn(msg, e);

      return makeProcessingError(msg + ". " + e.getMessage());
    } finally {
      try {
        if (impl != null)
          pool.returnObject(conf, impl);
      } catch (Throwable t) {
        log.warn("Failed to return object back to pool", t);
      }
    }
  }

  /**
   * The permission service function to execute.
   *
   * @param impl the service impl
   * @param resource the resource for access check
   * @param permission the permission to be checked
   * @param principal the user who is requesting the permission
   *
   * @return the return value from permission service
   *
   * @throws Exception on an error
   */
  protected abstract boolean execute(Permissions impl, String resource, String permission,
                                     String principal)
                              throws Exception;

  /*
   * @see com.sun.xacml.cond.Function#getIdentifier
   */
  public final URI getIdentifier() {
    return identifier;
  }

  /**
   * Gets the return type of this function.
   *
   * @return returns {@link com.sun.xacml.attr.BooleanAttribute#identifier}
   *
   * @see com.sun.xacml.cond.Function#getReturnType
   */
  public final URI getReturnType() {
    return BOOLEAN_TYPE;
  }

  /**
   * Checks to see if this function returns a bag of results.
   *
   * @return returns false since permission evals are not bags
   *
   * @see com.sun.xacml.cond.Function#returnsBag
   */
  public final boolean returnsBag() {
    return false;
  }

  /**
   * Create an <code>EvaluationResult</code> that indicates a processing error with the specified
   * message.
   *
   * @param message a description of the error (<code>null</code> if none)
   *
   * @return the desired <code>EvaluationResult</code>
   */
  protected EvaluationResult makeProcessingError(String message) {
    if (processingErrList == null) {
      String[] errStrings = { Status.STATUS_PROCESSING_ERROR };
      processingErrList = Arrays.asList(errStrings);
    }

    Status           errStatus       = new Status(processingErrList, message);
    EvaluationResult processingError = new EvaluationResult(errStatus);

    return processingError;
  }

  public static class IsGranted extends PermissionFunction {
    public IsGranted() {
      super(FUNCTION_IS_GRANTED);
    }

    protected boolean execute(Permissions impl, String resource, String permission, String principal)
                       throws Exception {
      return impl.isGranted(resource, permission, principal);
    }
  }

  public static class IsRevoked extends PermissionFunction {
    public IsRevoked() {
      super(FUNCTION_IS_REVOKED);
    }

    protected boolean execute(Permissions impl, String resource, String permission, String principal)
                       throws Exception {
      return impl.isRevoked(resource, permission, principal);
    }
  }

  private static class PermissionsImplFactory extends BaseKeyedPoolableObjectFactory {
    private Configuration configuration = ConfigurationStore.getInstance().getConfiguration();
    private Map           ctxMap = Collections.synchronizedMap(new HashMap());

    public Object makeObject(Object key) throws Exception {
      Configuration subset = configuration.subset((String) key);

      if ((subset == null) || (subset.getString("uri") == null))
        throw new ConfigurationException("Can't find configuration " + key);

      ProtectedService   service =
        ProtectedServiceFactory.createService(subset, (HttpSession) null);

      ItqlHelper         itql = new ItqlHelper(service);

      SimpleTopazContext ctx = new SimpleTopazContext(itql, null, null);

      Permissions        o = new PermissionsImpl(null, ctx);

      ctxMap.put(o, ctx);

      return o;
    }

    public void destroyObject(Object key, Object obj) throws Exception {
      obj = ctxMap.remove(obj);

      ItqlHelper itql = ((SimpleTopazContext) obj).getItqlHelper();
      itql.close();
    }
  }
}
