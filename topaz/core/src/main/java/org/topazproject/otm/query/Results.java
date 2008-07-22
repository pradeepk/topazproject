/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.otm.query;

import java.net.URI;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.serializer.Serializer;

/** 
 * This holds the results from a query. It is structured similar to a jdbc ResultSet.
 * 
 * @author Ronald Tschalär
 */
public abstract class Results {
  private final String[]       variables;
  private final String[]       warnings;
  private final SessionFactory sf;

  protected       int      pos = -1;
  protected       boolean  eor = false;
  protected       Type[]   types;
  protected final Object[] curRow;
  protected       boolean  autoClose = true;

  /** possible result types */
  public enum Type { CLASS, LITERAL, URI, BLANK_NODE, SUBQ_RESULTS, UNKNOWN };

  /** a literal in a result */
  public static class Literal {
    private final String value;
    private final String lang;
    private final URI    dtype;

    /** 
     * Create a new literal instance. 
     * 
     * @param value  the literal's value
     * @param lang   the literal's language tag; may be null
     * @param dtype   the literal's datatype; may be null
     * @throws NullPointerException if <var>value</var> is null
     * @throws IllegalArgumentException if both <var>lang</var> and <var>dtype</var> are non-null
     */
    public Literal(String value, String lang, URI dtype) {
      if (value == null)
        throw new NullPointerException("value may not be null");
      if (lang != null && dtype != null)
        throw new IllegalArgumentException("only one of language or datatype may be given");

      this.value = value;
      this.lang  = lang;
      this.dtype = dtype;
    }

    /** 
     * Get the literal's value. 
     * 
     * @return the literal's value
     */
    public String getValue() {
      return value;
    }

    /** 
     * Get the literal's language tag. 
     * 
     * @return the literal's language tag, or null if there is none
     */
    public String getLanguage() {
      return lang;
    }

    /** 
     * Get the literal's datatype. 
     * 
     * @return the literal's datatype, or null if this is an untyped literal
     */
    public URI getDatatype() {
      return dtype;
    }
  }

  protected Results(String[] variables, Type[] types, String[] warnings, SessionFactory sf) {
    if (variables.length != types.length)
      throw new IllegalArgumentException("the number variables does not match the number of " +

                                         "types: " + variables.length + " != " + types.length);
    this.variables = variables;
    this.types     = types;
    this.warnings  = (warnings != null && warnings.length > 0) ? warnings : null;
    this.sf        = sf;
    this.curRow    = new Object[variables.length];
  }

  /** 
   * Subclasses must do the work of loading a new row of results here. They are expected to
   * <ul>
   *   <li>Advance to the next row, or set <var>eor</var> if no more rows are available.</li>
   *   <li>populate <var>curRow</var> with the current row; alternatively the subclass may
   *       override {@link #getRow getRow()} and {@link #get(int, boolean) get(idx, eager)} and
   *       provide the results at that time instead.</li>
   * </ul>
   * 
   * @throws OtmException 
   */
  protected abstract void loadRow() throws OtmException;

  /**
   * Get the list of warnings generated while processing the query.
   *
   * @return the warnings, or null if there were none
   */
  public String[] getWarnings() {
    return (warnings != null) ? warnings.clone() : null;
  }

  /** 
   * Set the auto-close flag. This flag is true by default.
   * 
   * @param flag true if this result should be closed automatically upon reaching the end of the
   *             results; false if the result should be left open.
   */
  public void setAutoClose(boolean flag) {
    autoClose = flag;
  }

  /** 
   * Position cursor before the first row. 
   * 
   * @throws OtmException 
   */
  public void beforeFirst() throws OtmException {
    pos = -1;
    eor = false;
  }

  /** 
   * Move the cursor to the next row. 
   * 
   * @return true if the cursor is on a valid row; false if we just hit the end
   * @throws OtmException if this is called after having returned false
   */
  public boolean next() throws OtmException {
    if (eor)
      throw new QueryException("already at end of results");

    pos++;
    loadRow();

    if (eor && autoClose)
      close();

    return !eor;
  }

  /** 
   * Close the results. No other methods may be invoked once the result has been closed, and in
   * particular {@link #beforeFirst} cannot be used to restart the results after this.
   */
  public void close() {
  }

  /** 
   * Return the current row number. The number is zero based. 
   * 
   * @return the current row number, or -1 if not on a valid row
   */
  public int getRowNumber() {
    if (pos < 0 || eor)
      return -1;
    return pos;
  }

  /** 
   * Get the list of variables (columns) in the answer. 
   * 
   * @return the list of variables
   */
  public String[] getVariables() {
    return variables;
  }

  /** 
   * Return in the index of the given variable. 
   * 
   * @param var the variable
   * @return the index
   * @throws OtmException if <var>var</var> is not a variable in this result 
   */
  public int findVariable(String var) throws OtmException {
    for (int idx = 0; idx < variables.length; idx++) {
      if (variables[idx].equals(var))
        return idx;
    }

    throw new QueryException("no variable named '" + var + "' in the result");
  }

  /** 
   * Get the column type of the current row. The value is undefined
   * if the cursor is not on a valid row.
   * 
   * @param var  the variable identifying the column
   * @return the type
   * @throws OtmException if the variable does not exist
   */
  public Type getType(String var) throws OtmException {
    return getType(findVariable(var));
  }

  /** 
   * Get the column type of the current row. The value is undefined
   * if the cursor is not on a valid row.
   * 
   * @param idx  the column for which to get the type
   * @return the type
   */
  public Type getType(int idx) {
    return types[idx];
  }

  public Object[] getRow() throws OtmException {
    if (eor)
      throw new QueryException("at end of results");

    for (int idx = 0; idx < curRow.length; idx++)
      get(idx, true);

    return curRow.clone();
  }

  public final Object get(String var) throws OtmException {
    return get(var, true);
  }

  public final Object get(int idx) throws OtmException {
    return get(idx, true);
  }

  public Object get(String var, boolean eager) throws OtmException {
    return get(findVariable(var), eager);
  }

  public Object get(int idx, boolean eager) throws OtmException {
    if (eor)
      throw new QueryException("at end of results");

    return curRow[idx];
  }

  public String getString(String var) throws OtmException {
    return getString(findVariable(var));
  }

  public String getString(int idx) throws OtmException {
    switch (types[idx]) {
      case CLASS:
        return get(idx).toString();
      case LITERAL:
        return ((Literal) get(idx)).getValue();
      case URI:
        return ((URI) get(idx)).toString();
      case BLANK_NODE:
        return (String) get(idx);
      case SUBQ_RESULTS:
        throw new QueryException("cannot convert subquery result to a string");

      default:
        throw new Error("unknown type " + types[idx] + " encountered");
    }
  }

  public Literal getLiteral(String var) throws OtmException {
    return getLiteral(findVariable(var));
  }

  public Literal getLiteral(int idx) throws OtmException {
    switch (types[idx]) {
      case LITERAL:
        return (Literal) get(idx);

      case CLASS:
      case URI:
      case BLANK_NODE:
      case SUBQ_RESULTS:
        throw new QueryException("result object is not a literal; type=" + types[idx]);

      default:
        throw new Error("unknown type " + types[idx] + " encountered");
    }
  }

  public <T> T getLiteralAs(String var, Class<T> type) throws OtmException {
    return getLiteralAs(findVariable(var), type);
  }

  public <T> T getLiteralAs(int idx, Class<T> type) throws OtmException {
    Literal lit = getLiteral(idx);
    String  dt  = (lit.getDatatype() != null) ? lit.getDatatype().toString() : null;

    Serializer<T> serializer = sf.getSerializerFactory().getSerializer(type, dt);
    if (serializer == null)
      throw new OtmException("No serializer found for class '" + type.getName() +
                             "' and datatype '" + dt + "'");

    try {
      return serializer.deserialize(lit.getValue(), type);
    } catch (OtmException oe) {
      throw oe;
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new OtmException("Error deserializing '" + lit.getValue() + "'", e);
    }
  }

  public URI getURI(String var) throws OtmException {
    return getURI(findVariable(var));
  }

  public URI getURI(int idx) throws OtmException {
    switch (types[idx]) {
      case URI:
        return (URI) get(idx);

      case LITERAL:
      case CLASS:
      case BLANK_NODE:
      case SUBQ_RESULTS:
        throw new QueryException("result object is not a uri; type=" + types[idx]);

      default:
        throw new Error("unknown type " + types[idx] + " encountered");
    }
  }

  public <T> T getURIAs(String var, Class<T> type) throws OtmException {
    return getURIAs(findVariable(var), type);
  }

  public <T> T getURIAs(int idx, Class<T> type) throws OtmException {
    if (type == URI.class)
      return (T) getURI(idx);

    URI     uri = getURI(idx);
    String  dt  = sf.getSerializerFactory().getDefaultDataType(type);

    Serializer<T> serializer = sf.getSerializerFactory().getSerializer(type, dt);
    if (serializer == null)
      throw new OtmException("No serializer found for class '" + type.getName() +
                             "' and datatype '" + dt + "'");

    try {
      return serializer.deserialize(uri.toString(), type);
    } catch (OtmException oe) {
      throw oe;
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new OtmException("Error deserializing '" + uri + "'", e);
    }
  }

  public Results getSubQueryResults(String var) throws OtmException {
    return getSubQueryResults(findVariable(var));
  }

  public Results getSubQueryResults(int idx) throws OtmException {
    switch (types[idx]) {
      case SUBQ_RESULTS:
        return (Results) get(idx);

      case LITERAL:
      case CLASS:
      case BLANK_NODE:
      case URI:
        throw new QueryException("result object is not a subquery result; type=" + types[idx]);

      default:
        throw new Error("unknown type " + types[idx] + " encountered");
    }
  }
}
