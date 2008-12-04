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
package org.topazproject.otm;

import java.net.URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.mapping.PropertyBinder;
import org.topazproject.otm.serializer.Serializer;
import org.topazproject.otm.query.Results;

/**
 * An API for retrieving objects based on filtering and ordering conditions specified  using
 * {@link org.topazproject.otm.criterion.Criterion}. This is convenient in situations where
 * the query that needs to be run is only constructed at run time. An example is an advanced
 * search functionality where a user is selecting the conditions that need to be placed on
 * the result set.
 * <p>
 * The conditions that are placed on the Criteria are usually built from the factory
 * class {@link org.topazproject.otm.criterion.Restrictions Restrictions}. The Criteria
 * itself is created from the {@link org.topazproject.otm.Session#createCriteria createCriteria()}
 * method on the Session. eg.
 *
 * <pre>
 * List&lt;Person&gt; people = session.createCriteria(Person.class)
 *        .add(Restrictions.eq("knows","john")
 *        .addOrder(Order.asc("age"))
 *        .list();
 * </pre>
 *
 * @see Session#createCriteria(java.lang.Class)
 * @see org.topazproject.otm.criterion.Restrictions
 * @see org.topazproject.otm.criterion.Order
 * @see org.topazproject.otm.criterion.Criterion
 * @see org.topazproject.otm.criterion.Parameter
 * @see org.topazproject.otm.criterion.DetachedCriteria a disconnected version of this API
 *
 * @author Pradeep Krishnan
 */
public class Criteria implements Parameterizable<Criteria> {
  private final Session            session;
  private final ClassMetadata      classMetadata;
  private final Criteria           parent;
  private final RdfMapper          mapping;
  private final boolean            isReferrer;
  private final Collection<Filter> filters;
  private       int                maxResults    = -1;
  private       int                firstResult   = -1;
  private final List<Criterion>    criterions    = new ArrayList<Criterion>();
  private final List<Order>        orders        = new ArrayList<Order>();
  private final List<Criteria>     children      = new ArrayList<Criteria>();
  private final List<Order>        orderPosition;
  private final Set<String>        paramNames;
  private final Map<String, Object> paramValues;

  /**
   * Creates a new Criteria object. Called by {@link Session#createCriteria}.
   *
   * @param session The session that created it
   * @param parent The parent criteria for which this is a sub-criteria
   * @param mapping The mapping of the association field
   * @param isReferrer if true then <var>mapping</var> is in this criteria and the association
   *                   points to the parent; if false then <var>mapping</var> is in
   *                   <var>parent</var> and the association points from the parent to us.
   * @param classMetadata The class meta-data of this criteria
   * @param filters The filters to apply
   */
  public Criteria(Session session, Criteria parent, RdfMapper mapping, boolean isReferrer,
                  ClassMetadata classMetadata, Collection<Filter> filters) {
    this.session                        = session;
    this.parent                         = parent;
    this.mapping                        = mapping;
    this.isReferrer                     = isReferrer;
    this.classMetadata                  = classMetadata;
    this.filters                        = filters;

    if (parent == null) {
      orderPosition = new ArrayList<Order>();
      paramNames = new HashSet<String>();
      paramValues = new HashMap<String, Object>();
    } else {
      orderPosition = null;
      paramNames = null;
      paramValues = null;
    }
  }

  /**
   * Creates a new sub-criteria for an association.
   *
   * @param path to the association
   *
   * @return the newly created sub-criteria
   *
   * @throws OtmException on an error
   */
  public Criteria createCriteria(String path) throws OtmException {
    return createCriteria(path, null);
  }

  /**
   * Creates a new sub-criteria with an explicit given type for an association. This is the same
   * as {@link #createCriteria(java.lang.String)} except that the type of the child is explicitly
   * forced instead of determined from the association metadata.
   *
   * @param path      to the association
   * @param childType the entity type of the child criteria
   *
   * @return the newly created sub-criteria
   *
   * @throws OtmException on an error
   */
  public Criteria createCriteria(String path, String childType) throws OtmException {
    Criteria c = session.createCriteria(this, null, path, childType);
    children.add(c);

    return c;
  }

  /**
   * Creates a new sub-criteria for an association from another object to the current object.
   * This is the same as invoking {@link
   * #createReferrerCriteria(java.lang.String, java.lang.String, boolean)
   * createReferrerCriteria(referrer, path, false)}.
   *
   * @param referrer the entity whose association points to us
   * @param path     to the association (in <var>entity</var>); this must point to this criteria's
   *                 entity
   * @return the newly created sub-criteria
   * @throws OtmException on an error
   */
  public Criteria createReferrerCriteria(String referrer, String path) throws OtmException {
    return createReferrerCriteria(referrer, path, false);
  }

  /**
   * Creates a new sub-criteria for an association from another object to the current object.
   * Whereas {@link #createCriteria} allows one to walk down associations to other objects, this
   * allows one to walk up an assocation from another object.
   *
   * @param referrer  the entity whose association points to us
   * @param path      to the association (in <var>entity</var>); this must point to this criteria's
   *                  entity
   * @param forceType if true, force the type of our association end to be the same as this
   *                  criteria's type; if false, this criteria's type must be a subtype of the
   *                  assocation end's type.
   * @return the newly created sub-criteria
   * @throws OtmException on an error
   */
  public Criteria createReferrerCriteria(String referrer, String path, boolean forceType)
      throws OtmException {
    Criteria c =
      session.createCriteria(this, referrer, path, forceType ? getClassMetadata().getName() : null);
    children.add(c);

    return c;
  }

  /**
   * Get session.
   *
   * @return session as Session.
   */
  public Session getSession() {
    return session;
  }

  /**
   * Get class metadata.
   *
   * @return classMetadata as ClassMetadata.
   */
  public ClassMetadata getClassMetadata() {
    return classMetadata;
  }

  /**
   * Get parent.
   *
   * @return parent as Criteria.
   */
  public Criteria getParent() {
    return parent;
  }

  /**
   * Gets the mapping of the association field in parent.
   *
   * @return the mapper
   */
  public RdfMapper getMapping() {
    return mapping;
  }

  /**
   * Whether this entity of this criteria is the parent or child of the association
   *
   * @return true if the association points from this criteria's entity to that of the parent
   *         criteria; false if the other way around
   */
  public boolean isReferrer() {
    return isReferrer;
  }

  /**
   * Adds a Criterion.
   *
   * @param criterion the criterion to add
   *
   * @return this for method call chaining
   */
  public Criteria add(Criterion criterion) {
    criterions.add(criterion);
    getRoot().paramNames.addAll(criterion.getParamNames());

    return this;
  }

  /**
   * Adds an ordering criterion.
   *
   * @param order the order definition
   *
   * @return this for method call chaining
   */
  public Criteria addOrder(Order order) {
    orders.add(order);
    getRoot().orderPosition.add(order);

    return this;
  }

  /**
   * Lists the objects satisfying this Criteria.
   *
   * @return the list of objects.
   *
   * @throws OtmException on an error
   */
  public List list() throws OtmException {
    return (parent != null) ? parent.list() : session.list(this);
  }

  /**
   * Gets the list of child Criteria.
   *
   * @return list of child Criteria
   */
  public List<Criteria> getChildren() {
    return children;
  }

  /**
   * Gets the list of Criterions.
   *
   * @return list of Criterions
   */
  public List<Criterion> getCriterionList() {
    return criterions;
  }

  /**
   * Gets the list of Order definitions.
   *
   * @return lis of Order dedinitions
   */
  public List<Order> getOrderList() {
    return orders;
  }

  /**
   * Gets the list of Filters.
   *
   * @return lis of filters
   */
  public Collection<Filter> getFilters() {
    return filters;
  }

  /**
   * Gets the position of this order by clause in the root Criteria. Position is determined
   * by the sequence in which the {@link #addOrder} call is made.
   *
   * @param order a previously added order entry
   *
   * @return the position or -1 if the order entry does not exist
   */
  public int getOrderPosition(Order order) {
    return getRoot().orderPosition.indexOf(order);
  }

  /**
   * Gets the list of order by clauses in root.
   *
   * @return all order by clauses in order as seen by the root Criteria
   */
  public List<Order> getOrderPositions() {
    return getRoot().orderPosition;
  }

  /**
   * Set a limit upon the number of objects to be retrieved.
   *
   * @param maxResults the maximum number of results
   *
   * @return this (for method chaining)
   */
  public Criteria setMaxResults(int maxResults) {
    this.maxResults = maxResults;

    return this;
  }

  /**
   * Set the first result to be retrieved.
   *
   * @param firstResult the first result to retrieve, numbered from <tt>0</tt>
   *
   * @return this (for method chaining)
   */
  public Criteria setFirstResult(int firstResult) {
    this.firstResult = firstResult;

    return this;
  }

  /**
   * Get a limit upon the number of objects to be retrieved.
   *
   * @return the maximum number of results
   */
  public int getMaxResults() {
    return maxResults;
  }

  /**
   * Get the first result to be retrieved.
   *
   * @return the first result to retrieve, numbered from <tt>0</tt>
   */
  public int getFirstResult() {
    return firstResult;
  }

  /*
   * inherited javadoc
   */
  public Set<String> getParameterNames() {
    return getRoot().paramNames;
  }

  /*
   * inherited javadoc
   */
  public Criteria setParameter(String name, Object val) throws OtmException {
    checkParameterName(name);
    getRoot().paramValues.put(name, val);
    return this;
  }

  /*
   * inherited javadoc
   */
  public Criteria setUri(String name, URI val) throws OtmException {
    checkParameterName(name);
    getRoot().paramValues.put(name, new UriParam(val));
    return this;
  }

  /*
   * inherited javadoc
   */
  public Criteria setPlainLiteral(String name, String val, String lang) throws OtmException {
    checkParameterName(name);
    getRoot().paramValues.put(name, new Results.Literal(val, lang, null));
    return this;
  }

  /*
   * inherited javadoc
   */
  public Criteria setTypedLiteral(String name, String val, URI dataType) throws OtmException {
    checkParameterName(name);
    getRoot().paramValues.put(name, new Results.Literal(val, null, dataType));
    return this;
  }

  /**
   * Apply parameter values. (eg. from a Filter)
   *
   * @param values the values to apply
   *
   * @throws OtmException on an error
   */
  public void applyParameterValues(Map<String, Object> values) throws OtmException {
    getRoot().paramValues.putAll(values);
  }

  /**
   * Resolve a parameter.
   *
   * @param name the parameter name
   * @param field the field for which the parameter is to be resolved
   *
   * @return a serialized value of the parameter
   *
   * @throws OtmException if the parameter value cannot be resolved for this field
   */
  public String resolveParameter(String name, String field) throws OtmException {
    Mapper r = classMetadata.getMapperByName(field);
    if (!(r instanceof RdfMapper))
      throw new OtmException("'" + field + "' does not exist in " + classMetadata);

    RdfMapper m = (RdfMapper) r;
    Object val = getRoot().paramValues.get(name);
    if (val == null)
      throw new OtmException("No value specified for parameter '" + name + "': field '"
          + field + "'");

    if (val instanceof UriParam) {
      if (!m.typeIsUri())
        throw new OtmException("type mismatch in parameter '" + name + "': field '" +
                        field + "' is not a URI, but parameter value is a URI");
      return val.toString();
    }

    if (val instanceof Results.Literal) {
      if (m.typeIsUri())
        throw new OtmException("type mismatch in parameter '" + name + "': field '" +
                        field + "' is a URI, but parameter value is not a URI");

      Results.Literal lit = (Results.Literal) val;
      String type = m.getDataType();
      if (lit.getDatatype() == null && type != null)
        throw new OtmException("type mismatch in parameter '" + name + "': field '" +
                        field + "' is a typed literal with data type '" +
                        type + "' but parameter value is a plain literal");

      if (lit.getDatatype() != null) {
        if (type  == null)
          throw new OtmException("type mismatch in parameter '" + name + "': field '" +
                        field +
                        "' is a plain literal but parameter value is a typed literal with type '" +
                        lit.getDatatype() + "'");
        else if (!lit.getDatatype().equals(URI.create(type)))
          throw new OtmException("type mismatch in parameter '" + name + "': field '" +
                          field + "' is a typed literal with data type '" +
                          type + "' but parameter value is a typed literal with datatype '" +
                          lit.getDatatype() + "'");

        return lit.getValue();
      }
    }

    try {
      PropertyBinder l = m.getBinder(session);
      Serializer ser = l.getSerializer();
      return (ser != null) ? ser.serialize(val) : val.toString();
    } catch (Exception e) {
      throw new OtmException("Error serializing the value for parameter '" + name + ": field '" +
          field + "' and value is '" + val + "'", e);
    }
  }

  private Criteria getRoot() {
    return (parent == null) ? this : parent.getRoot();
  }

  private void checkParameterName(String name) throws OtmException {
    if (!getParameterNames().contains(name))
      throw new OtmException("'" + name + "' is not a valid parameter name - must be one of '" +
                             getParameterNames() + "'");
  }
}
