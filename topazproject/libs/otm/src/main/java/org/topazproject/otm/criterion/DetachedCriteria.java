/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.criterion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.mapping.Mapper;

/**
 * An API for retrieving objects based on filtering and ordering conditions specified  using
 * {@link org.topazproject.otm.criterion.Criterion}.
 *
 * @author Pradeep Krishnan
 */
public class DetachedCriteria {
  private final String                 alias;
  private final DetachedCriteria       parent;
  private Integer                      maxResults;
  private Integer                      firstResult;
  private final List<Criterion>        criterions    = new ArrayList<Criterion>();
  private final List<Order>            orders        = new ArrayList<Order>();
  private final List<DetachedCriteria> children      = new ArrayList<DetachedCriteria>();
  private final List<Order>            orderPosition = new ArrayList();

  /**
   * Creates a new DetachedCriteria object.
   *
   * @param entity the entity for which this alias is created
   */
  public DetachedCriteria(String alias) {
    this.alias    = alias;
    this.parent   = null;
  }

  private DetachedCriteria(DetachedCriteria parent, String path) {
    this.alias    = path;
    this.parent   = parent;
  }

  /**
   * Creates an executable criteria in the given session. Must be called only  on a root
   * criteria.
   *
   * @param session the execution context
   *
   * @return a newly created Criteria object
   *
   * @throws OtmException on an error
   * @throws UnsupportedOperationException if called on a child
   */
  public Criteria getExecutableCriteria(Session session)
                                 throws OtmException, UnsupportedOperationException {
    if (parent != null)
      throw new UnsupportedOperationException("Can't create executable Criteria from a detached child criteria");

    ClassMetadata cm = session.getSessionFactory().getClassMetadata(alias);

    if (cm == null)
      throw new OtmException("Entity name '" + alias + "' is not found in session factory");

    Criteria c = session.createCriteria(cm.getSourceClass());
    copyTo(c);
    c.getOrderPositions().clear();
    c.getOrderPositions().addAll(orderPosition);

    return c;
  }

  private void copyTo(Criteria c) throws OtmException {
    if (maxResults != null)
      c.setMaxResults(maxResults);

    if (firstResult != null)
      c.setFirstResult(firstResult);

    for (Criterion cr : criterions)
      c.add(cr);

    for (Order or : orders)
      c.addOrder(or);

    for (DetachedCriteria dc : children)
      dc.copyTo(c.createCriteria(dc.alias));
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
  public DetachedCriteria createCriteria(String path) throws OtmException {
    DetachedCriteria c = new DetachedCriteria(this, path);
    children.add(c);

    return c;
  }

  /**
   * Get parent.
   *
   * @return parent as Criteria.
   */
  public DetachedCriteria getParent() {
    return parent;
  }

  /**
   * Adds a Criterion.
   *
   * @param criterion the criterion to add
   *
   * @return this for method call chaining
   */
  public DetachedCriteria add(Criterion criterion) {
    criterions.add(criterion);

    return this;
  }

  /**
   * Adds an ordering criterion.
   *
   * @param order the order definition
   *
   * @return this for method call chaining
   */
  public DetachedCriteria addOrder(Order order) {
    orders.add(order);
    getRoot().orderPosition.add(order);

    return this;
  }

  /**
   * Gets the list of child Criteria.
   *
   * @return list of child Criteria
   */
  public List<DetachedCriteria> getChildren() {
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
   * Set a limit upon the number of objects to be retrieved.
   *
   * @param maxResults the maximum number of results
   *
   * @return this (for method chaining)
   */
  public DetachedCriteria setMaxResults(Integer maxResults) {
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
  public DetachedCriteria setFirstResult(Integer firstResult) {
    this.firstResult = firstResult;

    return this;
  }

  /**
   * Get a limit upon the number of objects to be retrieved.
   *
   * @return the maximum number of results
   */
  public Integer getMaxResults() {
    return maxResults;
  }

  /**
   * Get the first result to be retrieved.
   *
   * @return the first result to retrieve, numbered from <tt>0</tt>
   */
  public Integer getFirstResult() {
    return firstResult;
  }

  private DetachedCriteria getRoot() {
    return (parent == null) ? this : parent.getRoot();
  }
}
