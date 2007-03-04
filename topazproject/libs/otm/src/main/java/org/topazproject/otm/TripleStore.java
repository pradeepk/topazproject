package org.topazproject.otm;

import java.net.URI;

import java.util.List;
import java.util.Map;

/**
 * An abstraction to represent triple stores.
 *
 * @author Pradeep Krishnan
  */
public interface TripleStore {
  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Connection openConnection();

  /**
   * DOCUMENT ME!
   *
   * @param con DOCUMENT ME!
   */
  public void closeConnection(Connection con);

  /**
   * DOCUMENT ME!
   *
   * @param cm DOCUMENT ME!
   * @param id DOCUMENT ME!
   * @param o DOCUMENT ME!
   * @param txn DOCUMENT ME!
   */
  public void insert(ClassMetadata cm, String id, Object o, Transaction txn);

  /**
   * DOCUMENT ME!
   *
   * @param cm DOCUMENT ME!
   * @param id DOCUMENT ME!
   * @param txn DOCUMENT ME!
   */
  public void delete(ClassMetadata cm, String id, Transaction txn);

  /**
   * DOCUMENT ME!
   *
   * @param cm DOCUMENT ME!
   * @param id DOCUMENT ME!
   * @param txn DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Map<String, Map<String, List<String>>> get(ClassMetadata cm, String id, Transaction txn);

  /*
     public T <Collection<T>> find(Class<T> clazz, List<Criteria> criteria,
         List<Field> orderBy, long offset, long size);
   */
}
