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

package org.topazproject.otm.filter;

import java.util.Set;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.query.GenericQueryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This represents an OTM filter definition defined via a {@link
 * org.topazproject.otm.criterion.DetachedCriteria DetachedCriteria}.
 *
 * @author Ronald Tschalär
 */
public class CriteriaFilterDefinition extends AbstractFilterDefinition {
  private static final Log log = LogFactory.getLog(CriteriaFilterDefinition.class);

  private final DetachedCriteria crit;

  /** 
   * Create a new filter-definition based on the given criteria.
   * 
   * @param filterName    the name of the filter
   * @param crit          the criteria defining this filter
   */
  public CriteriaFilterDefinition(String filterName, DetachedCriteria crit) {
    super(filterName, crit.getAlias());
    this.crit = crit;
  }

  public Set<String> getParameterNames() {
    return crit.getParameterNames();
  }

  public Filter createFilter(Session sess) throws OtmException {
    return new CriteriaFilter(this, crit, sess);
  }

  public String toString() {
    return "CriteriaFilterDefinition[" + crit.toString("  ") + "]";
  }

  private static class CriteriaFilter extends AbstractFilterImpl {
    private final Criteria crit;

    private CriteriaFilter(FilterDefinition fd, DetachedCriteria crit, Session sess)
                         throws OtmException {
      super(fd, sess);
      this.crit = crit.getExecutableCriteria(sess);
    }

    public Criteria getCriteria() throws OtmException {
      crit.applyParameterValues(paramValues);
      return crit;
    }

    public GenericQueryImpl getQuery() throws OtmException {
      StringBuilder qry = new StringBuilder("select o from ");
      qry.append(crit.getClassMetadata().getName()).append(" o where ");
      toOql(qry, getCriteria(), "o", "v");
      if (qry.substring(qry.length() - 7).equals(" where "))
        qry.setLength(qry.length() - 7);
      qry.append(";");

      GenericQueryImpl q = new GenericQueryImpl(qry.toString(), CriteriaFilterDefinition.log);
      q.prepareQuery(sess.getSessionFactory());
      q.applyParameterValues(paramValues, sess.getSessionFactory());
      return q;
    }

    private static void toOql(StringBuilder qry, Criteria criteria, String var, String pfx)
                            throws OtmException{
      int idx = 0;
      for (Criterion c : criteria.getCriterionList())
        qry.append('(').append(c.toOql(criteria, var, pfx + idx++)).append(") and ");

      for (Criteria c : criteria.getChildren()) {
        String sbj = pfx + idx++;
        qry.append('(').append(sbj).append(" := ").append(var).append('.').
            append(c.getMapping().getName()).append(" and (");
        toOql(qry, c, sbj, pfx + idx++);
        qry.append(")) and ");
      }

      if (qry.substring(qry.length() - 5).equals(" and "))
        qry.setLength(qry.length() - 5);
    }
  }
}
