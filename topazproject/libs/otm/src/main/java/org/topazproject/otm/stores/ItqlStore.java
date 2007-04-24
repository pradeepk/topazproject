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

import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.AnswerSet;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.criterion.Conjunction;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.CriterionBuilder;
import org.topazproject.otm.criterion.Disjunction;
import org.topazproject.otm.criterion.Junction;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.PredicateCriterion;
import org.topazproject.otm.criterion.SubjectCriterion;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.query.QueryException;
import org.topazproject.otm.query.QueryInfo;
import org.topazproject.otm.query.Results;

/** 
 * An iTQL based store.
 * 
 * @author Ronald Tschalär
 */
public class ItqlStore implements TripleStore {
  private static final Log log = LogFactory.getLog(ItqlStore.class);
  private static final Map<Object, List<ItqlHelper>> conCache = new HashMap();
  private        final URI serverUri;
  private Map<String, CriterionBuilder> critBuilders = new HashMap<String, CriterionBuilder>();

  /** 
   * Create a new itql-store instance. 
   * 
   * @param server  the uri of the iTQL server.
   */
  public ItqlStore(URI server) {
    serverUri = server;
  }

  public Connection openConnection() {
    return new ItqlStoreConnection(serverUri);
  }

  public void closeConnection(Connection con) {
    ((ItqlStoreConnection) con).close();
  }

  public void insert(ClassMetadata cm, String id, Object o, Transaction txn) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();

    StringBuilder insert = new StringBuilder(500);
    insert.append("insert ");
    buildInsert(insert, cm, id, o, txn.getSession());
    insert.append("into <").append(getModelUri(cm.getModel(), txn)).append(">;");

    log.debug("insert: " + insert);
    try {
      isc.getItqlHelper().doUpdate(insert.toString(), null);
    } catch (RemoteException re) {
      throw new OtmException("error performing update", re);
    }
  }

  private void buildInsert(StringBuilder buf, ClassMetadata cm, String id, Object o, Session sess)
      throws OtmException {
    for (String type : cm.getTypes())
      addStmt(buf, id, Rdf.rdf + "type", type, null, true);

    int i = 0;
    for (Mapper p : cm.getFields()) {
      if (p.hasInverseUri())
        continue;

      if (p.getSerializer() != null)
        addStmts(buf, id, p.getUri(), (List<String>) p.get(o) , p.getDataType(), p.typeIsUri(), 
                 p.getMapperType(), "$s" + i++ + "i");
      else
        addStmts(buf, id, p.getUri(), sess.getIds(p.get(o)) , null,true, p.getMapperType(), 
                 "$s" + i++ + "i");
    }
  }

  private static void addStmts(StringBuilder buf, String subj, String pred, List<String> objs, 
      String dt, boolean objIsUri, Mapper.MapperType mt, String prefix) {
    int i = 0;
    switch (mt) {
    case PREDICATE:
      for (String obj : objs)
          addStmt(buf, subj, pred, obj, dt, objIsUri);
      break;
    case RDFLIST:
      if (objs.size() > 0)
        buf.append("<").append(subj).append("> <").append(pred).append("> ")
           .append(prefix).append("0 ");
      for (String obj : objs) {
          addStmt(buf, prefix+i, "rdf:type", "rdf:List", null, true);
          addStmt(buf, prefix+i, "rdf:first", obj, dt, objIsUri);
          if (i > 0)
            buf.append(prefix).append(i-1).append(" <rdf:rest> ")
                .append(prefix).append(i).append(" ");
          i++;
      }
      if (i > 0)
        addStmt(buf, prefix+(i-1), "rdf:rest", "rdf:nil", null, true);
      break;
    case RDFBAG:
    case RDFSEQ:
    case RDFALT:
      String rdfType = (Mapper.MapperType.RDFBAG == mt) ? "<rdf:Bag> " :
                       ((Mapper.MapperType.RDFSEQ == mt) ? "<rdf:Seq> " : "<rdf:Alt> ");
      if (objs.size() > 0) {
        buf.append("<").append(subj).append("> <").append(pred).append("> ")
           .append(prefix).append(" ");
        buf.append(prefix).append(" <rdf:type> ").append(rdfType);
      }
      for (String obj : objs)
        addStmt(buf, prefix, "rdf:_" + ++i, obj, dt, objIsUri);
      break;
    }
  }

  private static void addStmt(StringBuilder buf, String subj, String pred, String obj, String dt,
                              boolean objIsUri) {
    if (!subj.startsWith("$"))
      buf.append("<").append(subj).append("> <").append(pred);
    else
      buf.append(subj).append(" <").append(pred);

    if (objIsUri) {
      buf.append("> <").append(obj).append("> ");
    } else {
      buf.append("> '").append(ItqlHelper.escapeLiteral(obj));
      if (dt != null)
        buf.append("'^^<").append(dt).append("> ");
      else
        buf.append("' ");
    }
  }

  public void delete(ClassMetadata cm, String id, Transaction txn) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();

    StringBuilder delete = new StringBuilder(500);
    delete.append("delete ");
    buildDeleteSelect(delete, cm, id, txn);
    delete.append("from <").append(getModelUri(cm.getModel(), txn)).append(">;");

    log.debug("delete: " + delete);
    try {
      isc.getItqlHelper().doUpdate(delete.toString(), null);
    } catch (RemoteException re) {
      throw new OtmException("error performing update: " + delete.toString(), re);
    }
  }

  private void buildDeleteSelect(StringBuilder qry, ClassMetadata cm, String id, Transaction txn)
      throws OtmException {
    String model = getModelUri(cm.getModel(), txn);

    qry.append("select $s $p $o from <").append(model).append("> where ");

    // build forward statements
    boolean found = false;
    for (Mapper p : cm.getFields()) {
      if (!p.hasInverseUri()) {
        found = true;
        break;
      }
    }

    if (found) {
      qry.append("($s $p $o and $s <mulgara:is> <").append(id).append("> and (");
      for (Mapper p : cm.getFields()) {
        if (!p.hasInverseUri())
          qry.append("$p <mulgara:is> <").append(p.getUri()).append("> or ");
      }
      qry.setLength(qry.length() - 4);
      qry.append(") ) ");
    }

    // build reverse statements
    found = false;
    for (Mapper p : cm.getFields()) {
      if (p.hasInverseUri()) {
        found = true;
        break;
      }
    }

    if (found) {
      qry.append("or (");
      for (Mapper p : cm.getFields()) {
        if (p.hasInverseUri()) {
          String invP = p.getUri();

          qry.append("($s $p $o ");

          String inverseModel = p.getInverseModel();
          if (inverseModel != null)
            qry.append("in <").append(getModelUri(inverseModel, txn)).append("> ");

          qry.append("and $o <mulgara:is> <").append(id).append("> and $p <mulgara:is> <")
             .append(invP).append(">) or ");
        }
      }
      qry.setLength(qry.length() - 4);
      qry.append(") ");
    }

    // build rdf:List, rdf:Bag, rdf:Seq or rdf:Alt statements
    found = false;
    for (Mapper p : cm.getFields()) {
      if ((p.getMapperType() == Mapper.MapperType.RDFLIST) ||
          (p.getMapperType() == Mapper.MapperType.RDFBAG) || 
          (p.getMapperType() == Mapper.MapperType.RDFSEQ) ||
          (p.getMapperType() == Mapper.MapperType.RDFALT)) {
        found = true;
        break;
      }
    }

    if (found) {
      qry.append("or ($s $p $o and <").append(id).append("> $x $c ")
        .append("and (trans($c <rdf:rest> $s) or $c <rdf:rest> $s or <").append(id).append("> $x $s)")
        .append(" and ($s <rdf:type> <rdf:List> or $s <rdf:type> <rdf:Bag> ")
        .append("or $s <rdf:type> <rdf:Seq> or $s <rdf:type> <rdf:Alt>) )");
    }

  }

  public ResultObject get(ClassMetadata cm, String id, Transaction txn) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
    SessionFactory      sf  = txn.getSession().getSessionFactory();

    // TODO: eager fetching

    // do query
    StringBuilder get = new StringBuilder(500);
    buildGetSelect(get, cm, id, txn);
    get.append(";");

    log.debug("get: " + get);
    String a;
    try {
      a = isc.getItqlHelper().doQuery(get.toString(), null);
    } catch (RemoteException re) {
      throw new OtmException("error performing query", re);
    }

    // parse
    Map<String, List<String>> fvalues = new HashMap<String, List<String>>();
    Map<String, List<String>> rvalues = new HashMap<String, List<String>>();
    try {
      AnswerSet ans = new AnswerSet(a);

      // check if we got something useful
      ans.beforeFirst();
      if (!ans.next())
        return null;
      if (!ans.isQueryResult())
        throw new OtmException("query failed: " + ans.getMessage());

      AnswerSet.QueryAnswerSet qa = ans.getQueryResults();

      // collect the results, grouping by predicate
      qa.beforeFirst();
      while (qa.next()) {
        String p = qa.getString("p");
        boolean inverse = (!id.equals(qa.getString("s")) && id.equals(qa.getString("o")));

        if (!inverse) {
          List<String> v = fvalues.get(p);
          if (v == null)
            fvalues.put(p, v = new ArrayList<String>());
          v.add(qa.getString("o"));
        } else {
          List<String> v = rvalues.get(p);
          if (v == null)
            rvalues.put(p, v = new ArrayList<String>());
          v.add(qa.getString("s"));
        }
      }
    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }

    if (fvalues.size() == 0 && rvalues.size() == 0)
      return null;

    // figure out class to instantiate
    Class clazz = cm.getSourceClass();
    if (fvalues.get(Rdf.rdf + "type") != null) {
      clazz = sf.mostSpecificSubClass(clazz, fvalues.get(Rdf.rdf + "type"));
      cm    = sf.getClassMetadata(clazz);

      fvalues.get(Rdf.rdf + "type").removeAll(cm.getTypes());
    }

    // pre-process for special constructs (rdf:List, rdf:bag, rdf:Seq, rdf:Alt)
    String modelUri = getModelUri(cm.getModel(), txn);
    for (String p : fvalues.keySet()) {
      Mapper m = cm.getMapperByUri(p, false);
      if (m == null)
        continue;
      if (Mapper.MapperType.RDFLIST == m.getMapperType())
        fvalues.put(p, getRdfList(id, p, modelUri, txn));
      else if (Mapper.MapperType.RDFBAG == m.getMapperType() ||
          Mapper.MapperType.RDFSEQ == m.getMapperType() || 
          Mapper.MapperType.RDFALT == m.getMapperType())
        fvalues.put(p, getRdfBag(id, p, modelUri, txn));
    }

    // create result
    ResultObject ro;
    try {
      ro = new ResultObject(clazz.newInstance(), id);
    } catch (Exception e) {
      throw new OtmException("Failed to instantiate " + clazz, e);
    }

    cm.getIdField().set(ro.o, Collections.singletonList(id));

    boolean inverse = false;
    for (Map<String, List<String>> values : new Map[] { fvalues, rvalues }) {
      for (String p : values.keySet()) {
        Mapper m = cm.getMapperByUri(p, inverse);
        if (m != null) {
          if (m.getSerializer() != null)
            m.set(ro.o, values.get(p));
          else
            ro.unresolvedAssocs.put(m, values.get(p));
        }
      }
      inverse = true;
    }

    // done
    return ro;
  }

  private void buildGetSelect(StringBuilder qry, ClassMetadata cm, String id, Transaction txn)
      throws OtmException {
    String model = getModelUri(cm.getModel(), txn);

    qry.append("select $s $p $o from <").append(model).append("> where ");
    qry.append("($s $p $o and $s <mulgara:is> <").append(id).append(">)");
    qry.append("or ($s $p $o and $o <mulgara:is> <").append(id).append(">)");
  }

  private List<String> getRdfList(String sub, String pred, String modelUri, Transaction txn)
      throws OtmException {
    StringBuilder qry = new StringBuilder(500);
    qry.append("select $o from <").append(modelUri).append("> where ")
       .append("(trans($c <rdf:rest> $s) or $c <rdf:rest> $s or <")
       .append(sub).append("> <").append(pred).append("> $s) and <")
       .append(sub).append("> <").append(pred).append("> $c and $s <rdf:first> $o;");

    return execCollectionsQry(qry.toString(), txn);
  }

  private List<String> getRdfBag(String sub, String pred, String modelUri, Transaction txn)
      throws OtmException {
    StringBuilder qry = new StringBuilder(500);
    qry.append("select $o $p from <").append(modelUri).append("> where ")
       .append("($s $p $o minus $s <rdf:type> $o) and <")
       .append(sub).append("> <").append(pred).append("> $s;");

    return execCollectionsQry(qry.toString(), txn);
  }

  private List<String> execCollectionsQry(String qry, Transaction txn) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
    SessionFactory      sf  = txn.getSession().getSessionFactory();

    log.debug("rdf:List/rdf:Bag query : " + qry);
    String a;
    try {
      a = isc.getItqlHelper().doQuery(qry, null);
    } catch (RemoteException re) {
      throw new OtmException("error performing rdf:List/rdf:Bag query", re);
    }

    List<String> res = new ArrayList();

    try {
      AnswerSet ans = new AnswerSet(a);

      // check if we got something useful
      ans.beforeFirst();
      if (!ans.next())
        return res;
      if (!ans.isQueryResult())
        throw new OtmException("query failed: " + ans.getMessage());

      AnswerSet.QueryAnswerSet qa = ans.getQueryResults();

      // collect the results,
      qa.beforeFirst();
      boolean sort = qa.indexOf("p") != -1;
      if (!sort) {
        while (qa.next())
          res.add(qa.getString("o"));
      } else {
        while (qa.next()) {
          String p = qa.getString("p");
          if (!p.startsWith(Rdf.rdf + "_"))
            continue; // an un-recognized predicate
          int i = Integer.decode(p.substring(Rdf.rdf.length() + 1)) - 1;
          while (i >= res.size())
            res.add(null);
          res.set(i, qa.getString("o"));
        }
      }

    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }
    return res;
  }

  public List<ResultObject> list(Criteria criteria, Transaction txn) throws OtmException {
    // do query
    String qry = buildUserQuery(criteria, txn);
    log.debug("list: " + qry);
    String a;
    try {
      ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
      a = isc.getItqlHelper().doQuery(qry, null);
    } catch (RemoteException re) {
      throw new OtmException("error performing query: " + qry, re);
    }

    // parse
    List<ResultObject> results  = new ArrayList<ResultObject>();
    ClassMetadata cm = criteria.getClassMetadata();

    try {
      AnswerSet ans = new AnswerSet(a);

      // check if we got something useful
      ans.beforeFirst();
      if (!ans.next())
        return null;
      if (!ans.isQueryResult())
        throw new OtmException("query failed: " + ans.getMessage());

      // go through the rows and build the results
      AnswerSet.QueryAnswerSet qa = ans.getQueryResults();

      qa.beforeFirst();
      while (qa.next()) {
        String s = qa.getString("s");
        results.add(get(cm, s, txn));
      }
    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }

    return results;
  }

  private String buildUserQuery(Criteria criteria, Transaction txn) throws OtmException {
    StringBuilder qry = new StringBuilder(500);

    ClassMetadata cm = criteria.getClassMetadata();
    String model = getModelUri(cm.getModel(), txn);

    qry.append("select $s");
    for (int i = 0; i < criteria.getOrderList().size(); i++)
      qry.append(" $o" + i);

    qry.append(" from <").append(model).append("> where ");

    //xxx: there is problem with this auto generated where clause
    //xxx: it could potentially limit the result set returned by a trans() criteriom
    int i = 0;
    for (Order o: criteria.getOrderList()) {
      Mapper m = cm.getMapperByName(o.getName());
      if (m == null)
        throw new OtmException("Order by: No field with the name '" + o.getName() + "' in " + cm);
      if (m.hasInverseUri())
        qry.append("$o" + i++).append(" <").append(m.getUri()).append("> $s and ");
      else
        qry.append("$s <").append(m.getUri()).append("> $o" + i++).append(" and ");
    }

    if ((i > 0) && (criteria.getCriterionList().size() == 0))
      qry.setLength(qry.length() - 4);

    i = 0;
    for (Criterion c : criteria.getCriterionList())
      qry.append(c.toItql(criteria, "$s", "$t"+i++)).append(" and ");

    if (criteria.getCriterionList().size() > 0)
      qry.setLength(qry.length() - 4);

    if (criteria.getOrderList().size() > 0) {
      qry.append("order by ");
      i = 0;
      for (Order o : criteria.getOrderList())
        qry.append("$o" + i++).append(o.isAscending() ? " asc " : " desc ");
    }

    if (criteria.getMaxResults() > 0)
      qry.append(" limit " + criteria.getMaxResults());

    if (criteria.getFirstResult() >= 0)
      qry.append(" offset " + criteria.getFirstResult());

    // XXX: handle criteria.getChildren()

    qry.append(";");

    return qry.toString();
  }

  public Results doQuery(String query, Transaction txn) throws OtmException {
    ItqlQuery iq = new ItqlQuery();
    QueryInfo qi = iq.parseItqlQuery(txn.getSession(), query);

    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
    String a;
    try {
      a = isc.getItqlHelper().doQuery(qi.getQuery(), null);
    } catch (RemoteException re) {
      throw new QueryException("error performing query '" + query + "'", re);
    }

    return new ItqlResults(a, qi, iq.getWarnings().toArray(new String[0]), txn.getSession());
  }

  private static String getModelUri(String modelId, Transaction txn) throws OtmException {
    ModelConfig mc = txn.getSession().getSessionFactory().getModel(modelId);
    if (mc == null) // Happens if using a Class but the model was not added
      throw new OtmException("Unable to find model '" + modelId + "'");
    return mc.getUri().toString();
  }

  private static ItqlHelper getItqlHelper(URI serverUri) throws OtmException {
    synchronized (conCache) {
      List<ItqlHelper> list = conCache.get(serverUri);
      if (list != null && list.size() > 0)
        return list.remove(list.size() - 1);
      try {
        return new ItqlHelper(serverUri);
      } catch (ServiceException se) {
        throw new OtmException("Error talking to '" + serverUri + "'", se);
      } catch (RemoteException re) {
        throw new OtmException("Error talking to '" + serverUri + "'", re);
      } catch (MalformedURLException mue) {
        throw new OtmException("Invalid server URI '" + serverUri + "'", mue);
      }
    }
  }

  private static void returnItqlHelper(URI serverUri, ItqlHelper itql) {
    synchronized (conCache) {
      List<ItqlHelper> list = conCache.get(serverUri);
      if (list == null)
        conCache.put(serverUri, list = new ArrayList<ItqlHelper>());
      list.add(itql);
    }
  }

  public void createModel(ModelConfig conf) throws OtmException {
    ItqlHelper itql = ItqlStore.getItqlHelper(serverUri);
    try {
      String type = (conf.getType() == null) ? "mulgara:Model" : conf.getType().toString();
      itql.doUpdate("create <" + conf.getUri() + "> <" + type + ">;", null);
      returnItqlHelper(serverUri, itql);
    } catch (Exception e) {
      throw new OtmException("Failed to create model <" + conf.getUri() + ">", e);
    }finally {
      try {
        //closeConnection(con);
      } catch (Throwable t) {
        log.warn("Close connection failed", t);
      }
    }
  }

  public void dropModel(ModelConfig conf) throws OtmException {
    ItqlHelper itql = ItqlStore.getItqlHelper(serverUri);
    try {
      itql.doUpdate("drop <" + conf.getUri() + ">;", null);
      returnItqlHelper(serverUri, itql);
    } catch (Exception e) {
      throw new OtmException("Failed to drop model <" + conf.getUri() + ">", e);
    } finally {
      try {
      } catch (Throwable t) {
        log.warn("Close connection failed", t);
      }
    }
  }

  public CriterionBuilder getCriterionBuilder(String func)
                                       throws OtmException {
    return critBuilders.get(func);
  }

  public void setCriterionBuilder(String func, CriterionBuilder builder)
                           throws OtmException {
    critBuilders.put(func, builder);
  }

  private static class ItqlStoreConnection implements Connection {
    private final URI  serverUri;
    private ItqlHelper itql;

    public ItqlStoreConnection(URI serverUri) {
      this.serverUri = serverUri;
    }

    public ItqlHelper getItqlHelper() {
      return itql;
    }

    public void beginTransaction() throws OtmException {
      if (itql != null) {
        log.warn("Starting a transaction while one is already active - rolling back old one",
                 new Throwable());
        abort();
      }

      itql = ItqlStore.getItqlHelper(serverUri);
      try {
        itql.beginTxn("");
      } catch (RemoteException re) {
        throw new OtmException("error starting transaction", re);
      }
    }

    public void commit() throws OtmException {
      if (itql == null) {
        log.warn("Called commit but no transaction is active", new Throwable());
        return;
      }

      try {
        itql.commitTxn("");
      } catch (RemoteException re) {
        throw new OtmException("error starting transaction", re);
      }
      ItqlStore.returnItqlHelper(serverUri, itql);
      itql = null;
    }

    public void rollback() throws OtmException {
      if (itql == null) {
        log.warn("Called rollback but no transaction is active", new Throwable());
        return;
      }

      try {
        itql.rollbackTxn("");
      } catch (RemoteException re) {
        throw new OtmException("error starting transaction", re);
      }
      ItqlStore.returnItqlHelper(serverUri, itql);
      itql = null;
    }

    private void abort() {
      try {
        itql.rollbackTxn("");
        ItqlStore.returnItqlHelper(serverUri, itql);
      } catch (Exception e) {
        log.warn("Error during rollback", e);
      } finally {
        itql = null;
      }
    }

    public void close() {
      if (itql != null) {
        log.warn("Closing connection with an active transaction - rolling back current one",
                 new Throwable());
        abort();
      }
    }
  }
}
