package org.topazproject.xacml.cond;

import java.net.URI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import javax.xml.rpc.server.ServletEndpointContext;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.xacml.ServletEndpointContextAttribute;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.StringAttribute;
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

    // Get the JAX-RPC context.
    EvaluationResult result =
      context.getSubjectAttribute(ServletEndpointContextAttribute.TYPE,
                                  ServletEndpointContextAttribute.ID, null);

    // Abort policy evaluation if there is a failure in look up.
    if (result.indeterminate())
      return result;

    BagAttribute bag = (BagAttribute) result.getAttributeValue();

    if (bag.isEmpty()) {
      // Servlet endpoint context attribute is not found. So try accessing unprotected.
      service = ProtectedServiceFactory.createService(conf, null, null);
    } else {
      // Retrieve the JAX-RPC context.
      ServletEndpointContext epc =
        ((ServletEndpointContextAttribute) bag.iterator().next()).getValue();

      HttpSession            session = epc.getHttpSession();

      try {
        service =
          ProtectedServiceFactory.createCASService(conf,
                                                   ProtectedServiceFactory.getCASReceipt(session));
      } catch (java.io.IOException e) {
        throw new QueryException("Unable to obtain a CAS proxy ticket to authenticate to "
                                 + "ITQL interpreter bean service", e);
      }
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
    } catch (java.net.MalformedURLException e) {
      throw new QueryException(serviceUri
                               + " is not a valid URL to the ITQL interpreter bean service.", e);
    } catch (javax.xml.rpc.ServiceException e) {
      throw new QueryException("Unable to initialize connector to ITQL interpreter bean service at "
                               + serviceUri, e);
    } catch (java.rmi.RemoteException e) {
      throw new QueryException("Unable to initialize connector to ITQL interpreter bean service at "
                               + serviceUri, e);
    }

    // Create the query
    query = bindStatic(query, bindings);

    Answer answer;

    // Execute the query
    try {
      answer = new Answer(itql.doQuery(query));
    } catch (org.topazproject.mulgara.itql.service.ItqlInterpreterException e) {
      throw new QueryException("query '" + query + "' execution failed.", e);
    } catch (java.rmi.RemoteException e) {
      throw new QueryException("query '" + query + "' execution failed.", e);
    } catch (org.topazproject.mulgara.itql.AnswerException e) {
      throw new QueryException("query '" + query + "' execution failed.", e);
    }

    // Convert the results to a single column
    List     answers = answer.getAnswers();
    Iterator it      = answers.iterator();
    List     results = new ArrayList();

    for (int i = 0; it.hasNext(); i++) {
      Object o = it.next();

      if (!(o instanceof Answer.QueryAnswer))
        throw new QueryException("query '" + query + "' execution returned a message: " + o);

      Answer.QueryAnswer result  = (Answer.QueryAnswer) o;
      List               rows    = result.getRows();
      String[]           columns = result.getVariables();

      if (columns.length != 1)
        throw new QueryException("query '" + query + "' execution returned " + columns.length
                                 + " columns. Expects only 1 column in query results.");

      for (Iterator i2 = rows.iterator(); i2.hasNext();) {
        String[] row = (String[]) i2.next();
        results.add(new StringAttribute(row[0]));
      }
    }

    return new EvaluationResult(new BagAttribute(STRING_TYPE, results));
  }

  private String bindStatic(String query, String[] bindings)
                     throws QueryException {
    // xxx escapes?
    String[] parts = query.split("?");

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
