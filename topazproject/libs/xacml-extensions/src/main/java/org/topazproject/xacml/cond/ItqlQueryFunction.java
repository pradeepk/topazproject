package org.topazproject.xacml.cond;

import java.net.URI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.configuration.Configuration;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.StringAnswer;

import org.topazproject.xacml.ServletEndpointContextAttribute;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.cond.EvaluationResult;

/**
 * A XACML extension function to execute an ITQL query. The arguments to the function are a
 * configuration identifier for the database, the query string, and followed by any bind values.
 *
 * @author Pradeep Krishnan
 */
public class ItqlQueryFunction extends DBQueryFunction {
  /**
   * The name of this function as it appears in XACML policies.
   */
  public static String FUNCTION_NAME = FUNCTION_BASE + "itql";

  /**
   * Creates an ItqlQueryFunction instance.
   */
  public ItqlQueryFunction() {
    super(FUNCTION_NAME);
  }

  /**
   * Creates an ItqlQueryFunction instance.
   *
   * @param returnType the return type of this function
   */
  public ItqlQueryFunction(URI returnType) {
    super(FUNCTION_NAME, returnType);
  }

  /**
   * Executes an ITQL query.
   *
   * @param context the xacml evaluation context
   * @param conf the configuration ID for the ITQL interpreter bean service
   * @param query ITQL query with '?' used for parameter placeholders
   * @param bindings the values that are to be bound to query parameters.
   *
   * @return Returns query results as an array of Strings
   *
   * @throws QueryException when there is an error in executing the query
   */
  public EvaluationResult executeQuery(EvaluationCtx context, String conf, String query,
                                       String[] bindings)
                                throws QueryException {
    ProtectedService service;

    Configuration    configuration = ConfigurationStore.getInstance().getConfiguration();

    if (configuration != null)
      configuration = configuration.subset(conf);

    if ((configuration == null) || (configuration.getString("uri") == null))
      throw new QueryException("Can't find configuration " + conf);

    // Get the JAX-RPC context.
    EvaluationResult result =
      context.getSubjectAttribute(ServletEndpointContextAttribute.TYPE,
                                  ServletEndpointContextAttribute.ID, 
                                  ServletEndpointContextAttribute.CATEGORY);

    // Abort policy evaluation if there is a failure in look up.
    if (result.indeterminate())
      return result;

    BagAttribute bag = (BagAttribute) result.getAttributeValue();

    HttpSession  session;

    if (bag.isEmpty()) {
      // Servlet endpoint context attribute is not found. So no session.
      session = null;
    } else {
      // Retrieve the JAX-RPC context.
      ServletEndpointContext epc =
        ((ServletEndpointContextAttribute) bag.iterator().next()).getValue();

      session = epc.getHttpSession();
    }

    try {
      service = ProtectedServiceFactory.createService(configuration, session);
    } catch (java.io.IOException e) {
      throw new QueryException("Unable to obtain an itql service configuration instance", e);
    }

    // Now execute the query against the ITQL service
    return executeQuery(service, query, bindings);
  }

  private EvaluationResult executeQuery(ProtectedService service, String query, String[] bindings)
                                 throws QueryException {
    String     serviceUri = service.getServiceUri();

    ItqlHelper itql;

    // Create an ItqlHelper
    try {
      itql = new ItqlHelper(service);
    } catch (Exception e) {
      throw new QueryException("Unable to initialize connector to ITQL interpreter bean service at "
                               + serviceUri, e);
    }

    // Create the query
    query = bindStatic(query, bindings);

    StringAnswer answer;

    // Execute the query
    try {
      answer = new StringAnswer(itql.doQuery(query));
    } catch (Exception e) {
      throw new QueryException("query '" + query + "' execution failed.", e);
    }

    // Convert the results to a single column
    List     answers = answer.getAnswers();
    Iterator it      = answers.iterator();
    List     results = new ArrayList();

    for (int i = 0; it.hasNext(); i++) {
      Object o = it.next();

      if (!(o instanceof StringAnswer.StringQueryAnswer))
        continue; 

      StringAnswer.StringQueryAnswer result  = (StringAnswer.StringQueryAnswer) o;
      List                           rows    = result.getRows();
      String[]                       columns = result.getVariables();

      if (columns.length != 1)
        throw new QueryException("query '" + query + "' execution returned " + columns.length
                                 + " columns. Expects only 1 column in query results.");

      for (Iterator i2 = rows.iterator(); i2.hasNext();) {
        String[] row = (String[]) i2.next();
        results.add(row[0]);
      }
    }

    return makeResult((String[]) results.toArray(new String[results.size()]));
  }

  private String bindStatic(String query, String[] bindings)
                     throws QueryException {
    String[] parts = query.split("\\?");

    if ((parts.length - 1) != bindings.length)
      throw new QueryException("query '" + query + "' requires " + (parts.length - 1)
                               + " parameters; got " + bindings.length + " parameters instead.");

    if (parts.length == 1)
      return parts[0];

    StringBuffer s = new StringBuffer(512);

    int          i;

    for (i = 0; i < bindings.length; i++)
      s.append(parts[i]).append(bindings[i]);

    s.append(parts[i]);

    return s.toString();
  }
}
