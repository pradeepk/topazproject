/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.stores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.AnswerSet;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.Filter;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.filter.AbstractFilterImpl;
import org.topazproject.otm.mapping.Mapper;

/**
 * A helper class to build ITQL query for a Criteria.
 *
 * @author Pradeep Krishnan
 */
public class ItqlCriteria {
  private Criteria criteria;

/**
   * Creates a new ItqlCriteria object.
   *
   * @param criteria the criteria
   */
  ItqlCriteria(Criteria criteria) {
    this.criteria = criteria;
  }

  /**
   * Builds an ITQL query.
   *
   * @return the query string
   *
   * @throws OtmException on an exception
   */
  String buildUserQuery() throws OtmException {
    StringBuilder qry   = new StringBuilder(500);
    ClassMetadata cm    = criteria.getClassMetadata();
    String        model = getModelUri(cm.getModel());

    qry.append("select $s");

    int len = qry.length();
    buildProjections(criteria, qry, "$s");

    boolean hasOrderBy = len != qry.length();

    qry.append(" from <").append(model).append("> where ");

    len = qry.length();
    buildWhereClause(criteria, qry, "$s");

    if (qry.length() == len)
      throw new OtmException("No criterion list to filter by and the class '" + cm
                             + "' does not have an rdf:type.");

    qry.setLength(qry.length() - 4);

    if (hasOrderBy) {
      qry.append("order by ");

      List<String> orders = new ArrayList();
      buildOrderBy(criteria, orders, "$s");

      for (String o : orders)
        qry.append(o);
    }

    if (criteria.getMaxResults() > 0)
      qry.append(" limit " + criteria.getMaxResults());

    if (criteria.getFirstResult() >= 0)
      qry.append(" offset " + criteria.getFirstResult());

    qry.append(";");

    return qry.toString();
  }

  private void buildProjections(Criteria criteria, StringBuilder qry, String subject) {
    int    i      = 0;
    String prefix = " " + subject + "o";

    for (Order o : criteria.getOrderList())
      qry.append(prefix + i++);

    i = 0;

    for (Criteria cr : criteria.getChildren())
      buildProjections(cr, qry, subject + "c" + i++);
  }

  private void buildWhereClause(Criteria criteria, StringBuilder qry, String subject)
                         throws OtmException {
    ClassMetadata cm    = criteria.getClassMetadata();
    String        model = getModelUri(cm.getModel());

    if (cm.getType() != null)
      qry.append(subject).append(" <rdf:type> <").append(cm.getType()).append("> in <").append(model)
          .append("> and ");

    //XXX: there is problem with this auto generated where clause
    //XXX: it could potentially limit the result set returned by a trans() criteriom
    int    i      = 0;
    String object = subject + "o";

    for (Order o : criteria.getOrderList())
      buildPredicateWhere(cm, o.getName(), subject, object + i++, qry, model);

    i = 0;

    String tmp = subject + "t";

    for (Criterion c : criteria.getCriterionList())
      qry.append(c.toItql(criteria, subject, tmp + i++)).append(" and ");

    for (Filter f : criteria.getFilters())
      for (Criterion c : getCriterionList(f))
        qry.append(c.toItql(criteria, subject, tmp + i++)).append(" and ");

    i = 0;

    for (Criteria cr : criteria.getChildren()) {
      String child = subject + "c" + i++;
      buildPredicateWhere(cm, cr.getMapping().getName(), subject, child, qry, model);
      buildWhereClause(cr, qry, child);
    }
  }

  private void buildPredicateWhere(ClassMetadata cm, String name, String subject, String object,
                                   StringBuilder qry, String model)
                            throws OtmException {
    Mapper m = cm.getMapperByName(name);

    if (m == null)
      throw new OtmException("No field with the name '" + name + "' in " + cm);

    String mUri = (m.getModel() != null) ? getModelUri(m.getModel()) : model;

    if (m.hasInverseUri()) {
      String tmp = object;
      object    = subject;
      subject   = tmp;
    }

    qry.append(subject).append(" <").append(m.getUri()).append("> ").append(object).append(" in <")
        .append(mUri).append("> and ");
  }

  private void buildOrderBy(Criteria criteria, List<String> orders, String subject) {
    int    i      = 0;
    String prefix = subject + "o";

    for (Order o : criteria.getOrderList()) {
      int pos = criteria.getOrderPosition(o);

      while (pos >= orders.size())
        orders.add("");

      orders.set(pos, prefix + i++ + (o.isAscending() ? " asc " : " desc "));
    }

    i = 0;

    for (Criteria cr : criteria.getChildren())
      buildOrderBy(cr, orders, subject + "c" + i++);
  }

  private String getModelUri(String modelId) throws OtmException {
    ModelConfig mc = criteria.getSession().getSessionFactory().getModel(modelId);

    if (mc == null) // Happens if using a Class but the model was not added
      throw new OtmException("Unable to find model '" + modelId + "'");

    return mc.getUri().toString();
  }

  private List<Criterion> getCriterionList(Filter f) throws OtmException {
    ClassMetadata cm =
      criteria.getSession().getSessionFactory()
               .getClassMetadata(f.getFilterDefinition().getFilteredClass());

    if (cm == null)
      return Collections.emptyList();

    if (!cm.getSourceClass().isAssignableFrom(criteria.getClassMetadata().getSourceClass()))
      return Collections.emptyList();

    return ((AbstractFilterImpl) f).getCriteria().getCriterionList();
  }

  /**
   * Create the results list from an Itql Query response.
   *
   * @param a the ITQL query response
   *
   * @return list of results
   *
   * @throws OtmException on an error
   */
  List createResults(String a) throws OtmException {
    // parse
    List          results = new ArrayList();
    ClassMetadata cm      = criteria.getClassMetadata();

    try {
      AnswerSet ans = new AnswerSet(a);

      // check if we got something useful
      ans.beforeFirst();

      if (!ans.next())
        return Collections.emptyList();

      if (!ans.isQueryResult())
        throw new OtmException("query failed: " + ans.getMessage());

      // go through the rows and build the results
      AnswerSet.QueryAnswerSet qa = ans.getQueryResults();

      qa.beforeFirst();

      while (qa.next()) {
        String s = qa.getString("s");
        Object o = criteria.getSession().get(cm.getSourceClass(), s);

        if (o != null)
          results.add(o);
      }
    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }

    return results;
  }
}
