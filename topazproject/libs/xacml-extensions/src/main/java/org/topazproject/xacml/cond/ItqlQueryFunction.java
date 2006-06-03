package org.topazproject.xacml.cond;

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
   * @see org.topazproject.xacml.cond.DBQueryFuntion#executeQuery
   */
  public String[] executeQuery(String conf, String query, String[] bindings)
                        throws Exception {
    return new String[0];
  }
}
