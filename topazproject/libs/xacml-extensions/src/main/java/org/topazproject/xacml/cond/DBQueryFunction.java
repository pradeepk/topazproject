package org.topazproject.xacml.cond;

import java.net.URI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.topazproject.xacml.Util;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.cond.Function;
import com.sun.xacml.ctx.Status;

/**
 * An abstract class that implements a XACML extension function to execute a database query. The
 * arguments to the function are a configuration identifier for the database, the query string,
 * and followed by any bind values.
 *
 * @author Pradeep Krishnan
 */
public abstract class DBQueryFunction implements Function {
  /**
   * A base URI for all DB Query functions.
   */
  public static final String FUNCTION_BASE = "urn:topazproject:names:tc:xacml:1.0:function:query:";

  // URI version of StringAttribute's identifier
  private static final URI STRING_TYPE = Util.createUri(StringAttribute.identifier);

  // The identifier for this function.
  private URI identifier;

  // A List used by makeProcessingError() to save some steps.
  private static List processingErrList = null;

  /**
   * Creates a new DBQueryFunction object.
   *
   * @param functionName The function name as it appears in XACML policies
   */
  public DBQueryFunction(String functionName) {
    identifier = Util.createUri(functionName);
  }

  /*
   * @see com.sun.xacml.cond.Function#checkInputs
   */
  public final void checkInputs(List inputs) {
    if ((inputs == null) || (inputs.size() < 2))
      throw new IllegalArgumentException("not enough arguments to " + identifier);

    Iterator it = inputs.iterator();

    for (int i = 0; it.hasNext(); i++) {
      Evaluatable eval = (Evaluatable) it.next();

      if (eval.evaluatesToBag())
        throw new IllegalArgumentException("illegal argument type. bags are not supported");

      // conf and query must be strings
      if ((i < 2) && (!eval.getType().equals(STRING_TYPE)))
        throw new IllegalArgumentException("illegal argument type. must be "
                                           + StringAttribute.identifier);
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

    // First parameter is the config id
    EvaluationResult result = ((Evaluatable) it.next()).evaluate(context);

    if (result.indeterminate())
      return result;

    String conf = result.getAttributeValue().encode();

    // Second parameter is the query
    result = ((Evaluatable) it.next()).evaluate(context);

    if (result.indeterminate())
      return result;

    String query = result.getAttributeValue().encode();

    // Rest are all bindings
    String[] bindings = new String[inputs.size() - 2];

    for (int i = 0; it.hasNext(); i++) {
      result = ((Evaluatable) it.next()).evaluate(context);

      if (result.indeterminate())
        return result;

      bindings[i] = result.getAttributeValue().encode();
    }

    try {
      // Execute the query
      String[] results = executeQuery(conf, query, bindings);

      // Convert that to a result
      result = makeResult(results);
    } catch (Exception e) {
      // Create a processing error with the exception message
      result = makeProcessingError(e.getMessage());

      // xxx: log may be?
    }

    return result;
  }

  /**
   * Executes a database specific query. The query currently is supposed to return only a single
   * column of results.
   *
   * @param conf A database specific configuration identifier; eg. a connect String
   * @param query A database specific query
   * @param bindings An array of values that may be bound to the query before executing it.
   *
   * @return an array of query results. (single column for now)
   *
   * @throws Exception when there is a failure in executing the query
   */
  public abstract String[] executeQuery(String conf, String query, String[] bindings)
                                 throws Exception;

  /*
   * @see com.sun.xacml.cond.Function#getIdentifier
   */
  public final URI getIdentifier() {
    return identifier;
  }

  /**
   * Gets the return type of this function. Query results are string type. So always return string
   * type.
   *
   * @return returns <code>StringAttribute.identifier</code> as a URI
   *
   * @see com.sun.xacml.cond.Function#getReturnType
   */
  public final URI getReturnType() {
    return STRING_TYPE;
  }

  /**
   * Checks to see if this function returns a bag of results.  Query results are a bag. So always
   * returns true.
   *
   * @return returns true
   *
   * @see com.sun.xacml.cond.Function#returnsBag
   */
  public final boolean returnsBag() {
    return true;
  }

  /**
   * Create an <code>EvaluationResult</code> that indicates a processing error with the specified
   * message.
   *
   * @param message a description of the error (<code>null</code> if none)
   *
   * @return the desired <code>EvaluationResult</code>
   */
  private EvaluationResult makeProcessingError(String message) {
    // Build up the processing error Status.
    if (processingErrList == null) {
      String[] errStrings = { Status.STATUS_PROCESSING_ERROR };
      processingErrList = Arrays.asList(errStrings);
    }

    Status           errStatus       = new Status(processingErrList, message);
    EvaluationResult processingError = new EvaluationResult(errStatus);

    return processingError;
  }

  /**
   * Create an <code>EvaluationResult</code> that contains the results of the query.
   *
   * @param results an array of result strings
   *
   * @return the desired <code>EvaluationResult</code>
   */
  private EvaluationResult makeResult(String[] results) {
    // Create a bag with the results
    ArrayList bag = new ArrayList(results.length);

    for (int i = 0; i < results.length; i++)
      bag.add(new StringAttribute(results[i]));

    BagAttribute attr = new BagAttribute(STRING_TYPE, bag);

    return new EvaluationResult(attr);
  }
}
