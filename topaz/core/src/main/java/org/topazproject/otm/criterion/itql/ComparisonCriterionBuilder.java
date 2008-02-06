/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.criterion.itql;

import java.net.URI;
import java.util.List;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.CriterionBuilder;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.mapping.Mapper;

/**
 * Criterion Builder for comparison operations.
 *
 * @author Eric Brown
 * @author Pradeep Krishnan
 */
public class ComparisonCriterionBuilder implements CriterionBuilder {
  private static final URI RSLV_MODEL_TYPE =
                                      URI.create("http://topazproject.org/models#StringCompare");
  private URI              resolverModelType;

  /**
   * Creates a new ComparisonCriterionBuilder object.
   *
   * @param resolverModelType the model type uri for the resolver that implements comparison
   */
  public ComparisonCriterionBuilder(URI resolverModelType) {
    this.resolverModelType = resolverModelType;
  }

  /**
   * Creates a new ComparisonCriterionBuilder object using a default resolver model.
   */
  public ComparisonCriterionBuilder() {
    this(RSLV_MODEL_TYPE);
  }

  /*
   * inherited javadoc
   */
  public Criterion create(String func, Object[] args) throws OtmException {
    if (args.length < 2)
      throw new IllegalArgumentException(func + ": expecting at least 2 arguments");

    if (!(args[0] instanceof String))
      throw new IllegalArgumentException(func
                                         + ": argument 1 is 'name' and is expected to be a String");

    if (args[1] == null)
      throw new NullPointerException(func + ": argument 2 can not be null");

    return new ComparisonCriterion((String) args[0], args[1], "<topaz:" + func + ">",
                                   resolverModelType);
  }

  /**
   * A criterion for a triple pattern where a predicate values satisfy a comparison operator.
   *
   * @author Eric Brown
   * @author Pradeep Krishnan
   */
  public static class ComparisonCriterion extends Criterion {
    private URI    resolverModelType;
    private String name;
    private Object value;
    private String operator;

    /**
     * Creates a new ComparisonCriterion object.
     *
     * @param name field/predicate name
     * @param value field/predicate value
     * @param operator the comparison operator
     * @param resolverModelType the model type uri for the resolver that implements comparison
     */
    public ComparisonCriterion(String name, Object value, String operator, URI resolverModelType) {
      this.name              = name;
      this.value             = value;
      this.operator          = operator;
      this.resolverModelType = resolverModelType;
    }

    /*
     * inherited javadoc
     */
    public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                  throws OtmException {
      ClassMetadata cm = criteria.getClassMetadata();
      Mapper        m  = cm.getMapperByName(name);

      if (m == null)
        throw new OtmException("'" + name + "' does not exist in " + cm);

      if (m.typeIsUri())
        throw new OtmException("'" + name + "' invalid comparison - cannot compare URIs");

      String val = serializeValue(value, criteria, name);

      String model = m.getModel();

      if (model == null)
        model = "";
      else {
        ModelConfig conf = criteria.getSession().getSessionFactory().getModel(model);

        if (conf == null)
          throw new OtmException("Model/Graph '" + model + "' is not configured in SessionFactory");

        model = " in <" + conf.getUri() + ">";
      }

      List<ModelConfig> resolverModels =
          criteria.getSession().getSessionFactory().getModels(resolverModelType);
      if (resolverModels == null)
        throw new OtmException("No model for type '" + resolverModelType + "' has been configured" +
                               " in SessionFactory");
      String resolverModel = "<" + resolverModels.get(0).getUri() + ">";

      return "(" + subjectVar + " <" + m.getUri() + "> " + varPrefix + model + " and "
               + varPrefix + " " + operator + " " + val + " in " + resolverModel + ")";
    }

    /*
     * inherited javadoc
     */
    public String toOql(Criteria criteria, String subjectVar, String varPrefix)
                  throws OtmException {
      String res;

      if (operator.equals("<topaz:gt>"))
        res = "gt(";
      else if (operator.equals("<topaz:ge>"))
        res = "ge(";
      else if (operator.equals("<topaz:lt>"))
        res = "lt(";
      else if (operator.equals("<topaz:le>"))
        res = "le(";
      else
        throw new OtmException("Internal error: unknown operator '" + operator + "'");

      res += subjectVar + "." + name + ", " + serializeValue(value, criteria, name) + ")";

      return res;
    }

    /*
     * inherited javadoc
     */
    public void onPreInsert(DetachedCriteria dc, ClassMetadata cm) {
      throw new UnsupportedOperationException("Not meant to be persisted");
    }

    /*
     * inherited javadoc
     */
    public void onPostLoad(DetachedCriteria dc, ClassMetadata cm) {
      throw new UnsupportedOperationException("Not meant to be persisted");
    }
  }
}
