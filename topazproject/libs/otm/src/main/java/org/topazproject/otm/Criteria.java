package org.topazproject.otm;

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.mapping.Mapper;

/**
 * An API for retrieving objects based on filtering and ordering conditions specified  using
 * {@link org.topazproject.otm.criterion.Criterion}.
 *
 * @author Pradeep Krishnan
 */
public class Criteria {
  private Session         session;
  private ClassMetadata   classMetadata;
  private Criteria        parent;
  private Mapper          mapping;
  private List<Criterion> criterions = new ArrayList<Criterion>();
  private List<Order>     orders     = new ArrayList<Order>();
  private List<Criteria>  children   = new ArrayList<Criteria>();

/**
   * Creates a new Criteria object. Called by {@link Session#createCriteria}.
   *
   * @param session The session that created it
   * @param parent The parent criteria for which this is a sub-criteria
   * @param mapping The mapping of the association field in parent 
   * @param classMetadata The class meta-data of this criteria
   */
  public Criteria(Session session, Criteria parent, Mapper mapping, ClassMetadata classMetadata) {
    this.session         = session;
    this.parent          = parent;
    this.mapping         = mapping;
    this.classMetadata   = classMetadata;
  }

  /**
   * Creates a new sub-criteria for an association.
   *
   * @param path DOCUMENT ME!
   *
   * @return the newly created sub-criteria
   */
  public Criteria createCriteria(String path) throws OtmException {
    Criteria c = session.createCriteria(parent, path);
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
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Mapper getMapping() {
    return mapping;
  }

  /**
   * DOCUMENT ME!
   *
   * @param criterion DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Criteria add(Criterion criterion) {
    criterions.add(criterion);

    return this;
  }

  /**
   * DOCUMENT ME!
   *
   * @param order DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Criteria addOrder(Order order) {
    orders.add(order);

    return this;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public List list() throws OtmException {
    return (parent != null) ? parent.list() : session.list(this);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public List<Criteria> getChildren() {
    return children;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public List<Criterion> getCriterionList() {
    return criterions;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public List<Order> getOrderList() {
    return orders;
  }
}
