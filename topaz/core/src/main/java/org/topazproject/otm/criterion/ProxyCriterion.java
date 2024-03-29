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
package org.topazproject.otm.criterion;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

/**
 * A proxy criterion for executing store specific functions
 *
 * @author Pradeep Krishnan
 */
public class ProxyCriterion extends Criterion {
  private String    func;
  private Object[]  args;
  private Criterion criterion = null;

  /**
   * Creates a new ProxyCriterion object.
   *
   * @param func the function
   * @param args arguments
   */
  public ProxyCriterion(String func, Object... args) {
    this.func   = func;
    this.args   = args;
  }

  /**
   * Gets the function name.
   *
   * @return function name
   */
  public String getFunction() {
    return func;
  }

  /**
   * Gets the function arguments.
   *
   * @return arguments
   */
  public Object[] getArguments() {
    return args;
  }

  /*
   * inherited javadoc
   */
  public String toQuery(Criteria criteria, String subjectVar, String varPrefix, QL ql)
                throws OtmException {
    if (criterion == null) {
      CriterionBuilder cb = getCriterionBuilder(criteria);

      if (cb != null)
        criterion = cb.create(func, args);
    }

    if (criterion == null)
      throw new OtmException("Function '" + func + "' is unsupported by the triple-store");

    return criterion.toQuery(criteria, subjectVar, varPrefix, ql);
  }

  /**
   * Look up a builder for the 'real' Criteria.
   *
   * @param criteria the criteria context
   *
   * @return the Criterion builder that is registerd with the
   *         {@link org.topazproject.otm.TripleStore}
   */
  protected CriterionBuilder getCriterionBuilder(Criteria criteria) throws OtmException {
    return criteria.getSession().getSessionFactory().getTripleStore().getCriterionBuilder(func);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(15 + args.length * 10);
    sb.append("Proxy[").append(func).append("(");

    for (Object o : args)
      sb.append(o).append(", ");
    if (args.length > 0)
      sb.setLength(sb.length() - 2);

    sb.append(")]");
    return sb.toString();
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    throw new UnsupportedOperationException("Not meant to be persisted");
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    throw new UnsupportedOperationException("Not meant to be persisted");
  }
}
