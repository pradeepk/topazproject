package org.topazproject.otm.criterion;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.mapping.Mapper;

/**
 * A criterion for a triple pattern where the predicate and value are known.
 *
 * @author Pradeep Krishnan
 */
public class PredicateCriterion implements Criterion {
  private String name;
  private String value;

/**
   * Creates a new PredicateCriterion object.
   *
   * @param name DOCUMENT ME!
   * @param value DOCUMENT ME!
   */
  public PredicateCriterion(String name, String value) {
    this.name    = name;
    this.value   = value;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getName() {
    return name;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getValue() {
    return value;
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    ClassMetadata cm = criteria.getClassMetadata();
    Mapper        m  = cm.getMapperByName(getName());

    if (m == null)
      throw new OtmException("'" + getName() + "' does not exist in " + cm);

    String val;

    if (m.typeIsUri())
      val = "<" + ItqlHelper.validateUri(getValue(), getName()) + ">";
    else
      val = "'" + ItqlHelper.escapeLiteral(getValue()) + "'";

    if (!m.hasInverseUri())
      return subjectVar + " <" + m.getUri() + "> " + val;

    String model = m.getInverseModel();

    if (model != null) {
      ModelConfig conf = criteria.getSession().getSessionFactory().getModel(model);

      if (conf == null)
        throw new OtmException("Model/Graph '" + model + "' is not configured in SessionFactory");

      model = " in <" + conf.getUri() + ">";
    }

    String uri = criteria.getSession().getSessionFactory().getInverseUri(m.getUri());

    if (uri == null)
      throw new OtmException("No inverse uri for '" + m.getUri()
                             + "' configured in SessionFactory");

    String query = val + " <" + uri + "> " + subjectVar;

    if (model != null)
      query += model;

    return query;
  }
}
