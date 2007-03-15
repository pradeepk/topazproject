package org.topazproject.otm;

import java.net.URI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.topazproject.otm.mapping.Mapper;

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
   *
   * @throws OtmException DOCUMENT ME!
   */
  public Connection openConnection() throws OtmException;

  /**
   * DOCUMENT ME!
   *
   * @param con DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void closeConnection(Connection con) throws OtmException;

  /**
   * DOCUMENT ME!
   *
   * @param cm DOCUMENT ME!
   * @param id DOCUMENT ME!
   * @param o DOCUMENT ME!
   * @param txn DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void insert(ClassMetadata cm, String id, Object o, Transaction txn)
              throws OtmException;

  /**
   * DOCUMENT ME!
   *
   * @param cm DOCUMENT ME!
   * @param id DOCUMENT ME!
   * @param txn DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void delete(ClassMetadata cm, String id, Transaction txn)
              throws OtmException;

  /**
   * DOCUMENT ME!
   *
   * @param cm DOCUMENT ME!
   * @param id DOCUMENT ME!
   * @param txn DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public ResultObject get(ClassMetadata cm, String id, Transaction txn)
                   throws OtmException;

  /**
   * DOCUMENT ME!
   *
   * @param criteria DOCUMENT ME!
   * @param txn DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public List<ResultObject> list(Criteria criteria, Transaction txn)
                          throws OtmException;

  /**
   * DOCUMENT ME!
   *
   * @param conf DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void createModel(ModelConfig conf) throws OtmException;

  /**
   * DOCUMENT ME!
   *
   * @param conf DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void dropModel(ModelConfig conf) throws OtmException;

  public static class ResultObject {
    public Object                          o;
    public String                          id;
    public Map<Mapper, List<String>>       unresolvedAssocs = new HashMap<Mapper, List<String>>();
    public Map<Mapper, List<ResultObject>> resolvedAssocs   =
      new HashMap<Mapper, List<ResultObject>>();

    public ResultObject(Object o, String id) {
      this.o    = o;
      this.id   = id;
    }
  }
}
