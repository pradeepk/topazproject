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

import java.io.IOException;
import java.net.URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlClient;
import org.topazproject.mulgara.itql.ItqlClientFactory;
import org.topazproject.mulgara.itql.DefaultItqlClientFactory;
import org.topazproject.otm.AbstractConnection;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.Connection;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.Filter;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.RdfUtil;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.itql.ComparisonCriterionBuilder;
import org.topazproject.otm.filter.AbstractFilterImpl;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.query.GenericQueryImpl;
import org.topazproject.otm.query.QueryException;
import org.topazproject.otm.query.QueryInfo;
import org.topazproject.otm.query.Results;

/** 
 * An iTQL based store.
 * 
 * @author Ronald Tschalär
 */
public class ItqlStore extends AbstractTripleStore {
  private static final Log log = LogFactory.getLog(ItqlStore.class);
  private        final Map<Object, List<ItqlClient>> conCache = new HashMap();
  private        final ItqlClientFactory itqlFactory;
  private        final URI               serverUri;

  /** 
   * Create a new itql-store instance. 
   * 
   * @param server  the uri of the iTQL server.
   */
  public ItqlStore(URI server) {
    this(server, new DefaultItqlClientFactory());
  }

  /** 
   * Create a new itql-store instance. 
   * 
   * @param server  the uri of the iTQL server.
   * @param icf     the itql-client-factory to use
   */
  public ItqlStore(URI server, ItqlClientFactory icf) {
    serverUri   = server;
    itqlFactory = icf;

    //XXX: configure these
    ComparisonCriterionBuilder cc = new ComparisonCriterionBuilder();
    critBuilders.put("gt", cc);
    critBuilders.put("ge", cc);
    critBuilders.put("lt", cc);
    critBuilders.put("le", cc);
  }

  public Connection openConnection(SessionFactory sf) throws OtmException {
    return new ItqlStoreConnection(sf);
  }

  public <T> void insert(ClassMetadata<T> cm, Collection<Mapper> fields, String id, T o, 
                         Transaction txn) throws OtmException {
    Map<String, List<Mapper>> mappersByModel = groupMappersByModel(cm, fields);
    StringBuilder insert = new StringBuilder(500);

    // for every model create an insert statement
    for (String m : mappersByModel.keySet()) {
      insert.append("insert ");
      int startLen = insert.length();

      if (m.equals(cm.getModel())) {
        for (String type : cm.getTypes())
          addStmt(insert, id, Rdf.rdf + "type", type, null, true);
      }

      buildInsert(insert, mappersByModel.get(m), id, o, txn.getSession());

      if (insert.length() > startLen)
        insert.append("into <").append(getModelUri(m, txn)).append(">;");
      else
        insert.setLength(insert.length() - 7);
    }

    if (log.isDebugEnabled())
      log.debug("insert: " + insert);

    if (insert.length() == 0)
      return;

    try {
      ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection(this);
      isc.getItqlClient().doUpdate(insert.toString());
    } catch (IOException ioe) {
      throw new OtmException("error performing update", ioe);
    }
  }

  private static Map<String, List<Mapper>> groupMappersByModel(ClassMetadata<?> cm, 
                                                               Collection<Mapper> fields) {
    Map<String, List<Mapper>> mappersByModel = new HashMap<String, List<Mapper>>();

    if (cm.getTypes().size() > 0)
      mappersByModel.put(cm.getModel(), new ArrayList<Mapper>());

    for (Mapper p : fields) {
      String m = (p.getModel() != null) ? p.getModel() : cm.getModel();
      List<Mapper> pList = mappersByModel.get(m);
      if (pList == null)
        mappersByModel.put(m, pList = new ArrayList<Mapper>());
      pList.add(p);
    }

    return mappersByModel;
  }


  private void buildInsert(StringBuilder buf, List<Mapper> pList, String id, Object o, Session sess)
      throws OtmException {
    int i = 0;
    for (Mapper p : pList) {
      if (!p.isEntityOwned())
        continue;
      if (p.isPredicateMap()) {
        Map<String, List<String>> pMap = (Map<String, List<String>>) p.getRawValue(o, true);
        for (String k : pMap.keySet())
          for (String v : pMap.get(k))
            addStmt(buf, id, k, v, null, false); // xxx: uri or literal?
      } else if (!p.isAssociation())
        addStmts(buf, id, p.getUri(), (List<String>) p.get(o) , p.getDataType(), p.typeIsUri(), 
                 p.getColType(), "$s" + i++ + "i", p.hasInverseUri());
      else
        addStmts(buf, id, p.getUri(), sess.getIds(p.get(o)) , null,true, p.getColType(), 
                 "$s" + i++ + "i", p.hasInverseUri());
    }
  }

  private static void addStmts(StringBuilder buf, String subj, String pred, List<String> objs, 
      String dt, boolean objIsUri, CollectionType mt, String prefix, boolean inverse) {
    int i = 0;
    switch (mt) {
    case PREDICATE:
      for (String obj : objs)
        if (!inverse)
          addStmt(buf, subj, pred, obj, dt, objIsUri);
        else
          addStmt(buf, obj, pred, subj, dt, true);
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
      String rdfType = (CollectionType.RDFBAG == mt) ? "<rdf:Bag> " :
                       ((CollectionType.RDFSEQ == mt) ? "<rdf:Seq> " : "<rdf:Alt> ");
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
      buf.append("> '").append(RdfUtil.escapeLiteral(obj));
      if (dt != null)
        buf.append("'^^<").append(dt).append("> ");
      else
        buf.append("' ");
    }
  }

  public <T> void delete(ClassMetadata<T> cm, Collection<Mapper> fields, String id, T o, 
                         Transaction txn) throws OtmException {
    final boolean partialDelete = (cm.getFields().size() != fields.size());
    Map<String, List<Mapper>> mappersByModel = groupMappersByModel(cm, fields);
    StringBuilder delete = new StringBuilder(500);

    // for every model create a delete statement
    for (String m : mappersByModel.keySet()) {
      int len = delete.length();
      delete.append("delete ");
      if (buildDeleteSelect(delete, getModelUri(m, txn), mappersByModel.get(m),
                        !partialDelete && m.equals(cm.getModel()) && cm.getTypes().size() > 0, id))
        delete.append("from <").append(getModelUri(m, txn)).append(">;");
      else
        delete.setLength(len);
    }

    if (log.isDebugEnabled())
      log.debug("delete: " + delete);

    if (delete.length() == 0)
      return;

    try {
      ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection(this);
      isc.getItqlClient().doUpdate(delete.toString());
    } catch (IOException ioe) {
      throw new OtmException("error performing update: " + delete.toString(), ioe);
    }
  }

  private boolean buildDeleteSelect(StringBuilder qry, String model, List<Mapper> mappers,
                                 boolean delTypes, String id)
      throws OtmException {
    qry.append("select $s $p $o from <").append(model).append("> where ");
    int len = qry.length();

    // build forward statements
    boolean found = delTypes;
    boolean predicateMap = false;
    for (Mapper p : mappers) {
      if (!p.hasInverseUri() && p.isEntityOwned()) {
        if (p.isPredicateMap()) {
          predicateMap = true;
          found = false;
          break;
        }
        found = true;
      }
    }

    if (predicateMap)
      qry.append("($s $p $o and $s <mulgara:is> <").append(id).append(">) ");

    if (found) {
      qry.append("($s $p $o and $s <mulgara:is> <").append(id).append("> and (");
      if (delTypes)
        qry.append("$p <mulgara:is> <rdf:type> or ");
      for (Mapper p : mappers) {
        if (!p.hasInverseUri() && p.isEntityOwned())
          qry.append("$p <mulgara:is> <").append(p.getUri()).append("> or ");
      }
      qry.setLength(qry.length() - 4);
      qry.append(") ) ");
    }

    // build reverse statements
    boolean needDisj = found | predicateMap;
    found = false;
    for (Mapper p : mappers) {
      if (p.hasInverseUri() && p.isEntityOwned()) {
        found = true;
        break;
      }
    }

    if (found) {
      if (needDisj)
        qry.append("or ");
      qry.append("($s $p $o and $o <mulgara:is> <").append(id).append("> and (");
      for (Mapper p : mappers) {
        if (p.hasInverseUri() && p.isEntityOwned())
          qry.append("$p <mulgara:is> <").append(p.getUri()).append("> or ");
      }
      qry.setLength(qry.length() - 4);
      qry.append(") ) ");
    }

    // build rdf:List, rdf:Bag, rdf:Seq or rdf:Alt statements
    Collection<Mapper> col = null;
    for (Mapper p : mappers) {
      if (p.getColType() == CollectionType.RDFLIST) {
        if (col == null)
          col = new ArrayList<Mapper>();
        col.add(p);
      }
    }

    if (col != null) {
      qry.append("or ($s $p $o and <").append(id).append("> $x $c ")
        .append("and (trans($c <rdf:rest> $s) or $c <rdf:rest> $s or <").append(id).append("> $x $s)")
        .append(" and $s <rdf:type> <rdf:List> and (");
      for (Mapper p : col)
        qry.append("$x <mulgara:is> <").append(p.getUri()).append("> or ");
      qry.setLength(qry.length() - 4);
      qry.append(") ) ");
    }

    col = null;
    for (Mapper p : mappers) {
      if ((p.getColType() == CollectionType.RDFBAG) || 
          (p.getColType() == CollectionType.RDFSEQ) ||
          (p.getColType() == CollectionType.RDFALT)) {
        if (col == null)
          col = new ArrayList<Mapper>();
        col.add(p);
      }
    }

    if (col != null) {
      qry.append("or ($s $p $o and <").append(id).append("> $x $s ")
        .append(" and ($s <rdf:type> <rdf:Bag> ")
        .append("or $s <rdf:type> <rdf:Seq> or $s <rdf:type> <rdf:Alt>) and (");
      for (Mapper p : col)
        qry.append("$x <mulgara:is> <").append(p.getUri()).append("> or ");
      qry.setLength(qry.length() - 4);
      qry.append(") ) ");
    }

    return (qry.length() > len);
  }

  public <T> T get(ClassMetadata<T> cm, String id, T instance, Transaction txn,
                   List<Filter> filters, boolean filterObj) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection(this);
    SessionFactory      sf  = txn.getSession().getSessionFactory();

    // TODO: eager fetching

    // do query
    String get = buildGetSelect(cm, id, txn, filters, filterObj);
    if (log.isDebugEnabled())
      log.debug("get: " + get);

    List<Answer> ans;
    try {
      ans = isc.getItqlClient().doQuery(get);
    } catch (IOException ioe) {
      throw new OtmException("error performing query: " + get, ioe);
    } catch (AnswerException ae) {
      throw new OtmException("error performing query: " + get, ae);
    }

    // parse
    Map<String, List<String>> fvalues = new HashMap<String, List<String>>();
    Map<String, List<String>> rvalues = new HashMap<String, List<String>>();
    Map<String, Set<String>> types = new HashMap<String, Set<String>>();
    try {
      for (Answer qa : ans) {
        if (qa.getVariables() == null)
          throw new OtmException("query failed: " + qa.getMessage());

        // collect the results, grouping by predicate
        qa.beforeFirst();
        while (qa.next()) {
          String s = qa.getString("s");
          String p = qa.getString("p");
          String o = qa.getString("o");
          boolean inverse = (!id.equals(s) && id.equals(o));

          if (!inverse) {
            List<String> v = fvalues.get(p);
            if (v == null)
              fvalues.put(p, v = new ArrayList<String>());
            v.add(o);
          } else {
            List<String> v = rvalues.get(p);
            if (v == null)
              rvalues.put(p, v = new ArrayList<String>());
            v.add(s);
          }

          updateTypeMap(qa.getSubQueryResults(3), inverse ? s : o, types);
        }
      }
    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }

    if (fvalues.size() == 0 && rvalues.size() == 0)
      return null;

    // figure out class to instantiate
    Class clazz = cm.getSourceClass();
    if (fvalues.get(Rdf.rdf + "type") == null) {
      if (cm.getType() != null)
        return null;
    } else {
      clazz = sf.mostSpecificSubClass(clazz, fvalues.get(Rdf.rdf + "type"));
      if (clazz == null)
        return null;
      cm    = sf.getClassMetadata(clazz);

      fvalues.get(Rdf.rdf + "type").removeAll(cm.getTypes());
    }

    if (cm.getIdField() == null)
      throw new OtmException("No id field in class '" + clazz 
        + "' and therefore cannot be instantiated.");

    // pre-process for special constructs (rdf:List, rdf:bag, rdf:Seq, rdf:Alt)
    String modelUri = getModelUri(cm.getModel(), txn);
    for (String p : fvalues.keySet()) {
      Mapper m = cm.getMapperByUri(p, false, null);
      if (m == null)
        continue;
      String mUri = (m.getModel() != null) ? getModelUri(m.getModel(), txn) : modelUri;
      if (CollectionType.RDFLIST == m.getColType())
        fvalues.put(p, getRdfList(id, p, mUri, txn, types, m, sf, filters));
      else if (CollectionType.RDFBAG == m.getColType() ||
          CollectionType.RDFSEQ == m.getColType() || 
          CollectionType.RDFALT == m.getColType())
        fvalues.put(p, getRdfBag(id, p, mUri, txn, types, m, sf, filters));
    }

    return instantiate(txn.getSession(), instance, cm, id, fvalues, rvalues, types);
  }

  private String buildGetSelect(ClassMetadata<?> cm, String id, Transaction txn,
                                List<Filter> filters, boolean filterObj) throws OtmException {
    SessionFactory        sf     = txn.getSession().getSessionFactory();
    Set<ClassMetadata<?>> fCls   = listFieldClasses(cm, sf);
    String                models = getModelsExpr(Collections.singleton(cm), txn);
    String                tmdls  = fCls.size() > 0 ? getModelsExpr(fCls, txn) : models;
    List<Mapper>          assoc  = listAssociations(cm, sf);

    StringBuilder qry = new StringBuilder(500);

    qry.append("select $s $p $o subquery (select $t from ").append(tmdls).append(" where ");
    qry.append("$o <rdf:type> $t) ");
    qry.append("from ").append(models).append(" where ");
    qry.append("$s $p $o and $s <mulgara:is> <").append(id).append(">");
    if (filterObj)
      applyObjectFilters(qry, cm.getSourceClass(), "$s", filters, sf);
    applyFieldFilters(qry, assoc, true, id, filters, sf);

    qry.append("; select $s $p $o subquery (select $t from ").append(models).append(" where ");
    qry.append("$s <rdf:type> $t) ");
    qry.append("from ").append(models).append(" where ");
    qry.append("$s $p $o and $o <mulgara:is> <").append(id).append(">");
    if (filterObj)
      applyObjectFilters(qry, cm.getSourceClass(), "$o", filters, sf);
    applyFieldFilters(qry, assoc, false, id, filters, sf);
    qry.append(";");

    return qry.toString();
  }

  private void applyObjectFilters(StringBuilder qry, Class cls, String var, List<Filter> filters,
                                  SessionFactory sf) throws OtmException {
    // avoid work if possible
    if (filters == null || filters.size() == 0)
      return;

    // find applicable filters
    filters = new ArrayList<Filter>(filters);
    for (Iterator<Filter> iter = filters.iterator(); iter.hasNext(); ) {
      Filter f = iter.next();
      ClassMetadata<?> fcm = sf.getClassMetadata(f.getFilterDefinition().getFilteredClass());
      if (!fcm.getSourceClass().isAssignableFrom(cls))
        iter.remove();
    }

    if (filters.size() == 0)
      return;

    // apply filters
    qry.append(" and (");

    int idx = 0;
    for (Filter f : filters) {
      qry.append("(");
      ItqlCriteria.buildFilter((AbstractFilterImpl) f, qry, var, "$gof" + idx++);
      qry.append(") or ");
    }

    qry.setLength(qry.length() - 4);
    qry.append(")");
  }

  private void applyFieldFilters(StringBuilder qry, List<Mapper> assoc, boolean fwd, String id,
                                 List<Filter> filters, SessionFactory sf)
        throws OtmException {
    // avoid work if possible
    if (filters == null || filters.size() == 0 || assoc.size() == 0)
      return;

    // build predicate->filter map
    Map<String, Set<Filter>> applicFilters = new HashMap<String, Set<Filter>>();
    for (Mapper m : assoc) {
      Class mc = m.getComponentType();

      for (Filter f : filters) {
        ClassMetadata cm = sf.getClassMetadata(f.getFilterDefinition().getFilteredClass());
        if (cm == null)
          continue;       // bug???

        Class fc = cm.getSourceClass();
        if (fc.isAssignableFrom(mc)) {
          Set<Filter> fset = applicFilters.get(m.getUri());
          if (fset == null)
            applicFilters.put(m.getUri(), fset = new HashSet<Filter>());
          fset.add(f);
        }
      }
    }

    if (applicFilters.size() == 0)
      return;

    // a little optimization: try to group filters
    Map<Set<Filter>, Set<String>> filtersToPred = new HashMap<Set<Filter>, Set<String>>();
    for (String p : applicFilters.keySet()) {
      Set<Filter> fset = applicFilters.get(p);
      Set<String> pset = filtersToPred.get(fset);
      if (pset == null)
        filtersToPred.put(fset, pset = new HashSet<String>());
      pset.add(p);
    }

    // apply filters
    StringBuilder predList = new StringBuilder(500);
    qry.append(" and (");

    int idx = 0;
    for (Set<Filter> fset : filtersToPred.keySet()) {
      qry.append("(");

      qry.append("(");
      Set<String> pset = filtersToPred.get(fset);
      for (String pred : pset) {
        String predExpr = "$p <mulgara:is> <" + pred + "> or ";
        qry.append(predExpr);
        predList.append(predExpr);
      }
      qry.setLength(qry.length() - 4);
      qry.append(") and ((");

      for (Filter f : fset) {
        ItqlCriteria.buildFilter((AbstractFilterImpl) f, qry, fwd ? "$o" : "$s", "$gff" + idx++);
        qry.append(") and (");
      }
      qry.setLength(qry.length() - 7);
      qry.append("))) or ");
    }

    predList.setLength(predList.length() - 4);
    if (fwd)
      qry.append("(<").append(id).append("> $p $any1 minus (");
    else
      qry.append("($any1 $p <").append(id).append("> minus (");
    qry.append(predList).append(")))");
  }

  private static String getModelsExpr(Set<? extends ClassMetadata<?>> cmList, Transaction txn)
      throws OtmException {
    Set<String> mList = new HashSet<String>();
    for (ClassMetadata<?> cm : cmList) {
      mList.add(getModelUri(cm.getModel(), txn));
      for (Mapper p : cm.getFields()) {
        if (p.getModel() != null)
          mList.add(getModelUri(p.getModel(), txn));
      }
    }

    StringBuilder mexpr = new StringBuilder(100);
    for (String m : mList)
      mexpr.append("<").append(m).append("> or ");
    mexpr.setLength(mexpr.length() - 4);

    return mexpr.toString();
  }

  private static Set<ClassMetadata<?>> listFieldClasses(ClassMetadata<?> cm, SessionFactory sf) {
    Set<ClassMetadata<?>> clss = new HashSet<ClassMetadata<?>>();

    for (Mapper p : cm.getFields()) {
      ClassMetadata<?> c = sf.getClassMetadata(p.getAssociatedEntity());
      if ((c != null) && ((c.getTypes().size() + c.getFields().size()) > 0))
        clss.add(c);
    }

    return clss;
  }

  private static List<Mapper> listAssociations(ClassMetadata<?> cm, SessionFactory sf) {
    List<Mapper> mappers = new ArrayList<Mapper>();

    for (ClassMetadata<?> c : allSubClasses(cm, sf)) {
      for (Mapper p : c.getFields()) {
        if (p.isAssociation())
          mappers.add(p);
      }
    }

    return mappers;
  }

  private static Set<ClassMetadata<?>> allSubClasses(ClassMetadata<?> top, SessionFactory sf) {
    Set<ClassMetadata<?>> classes = new HashSet<ClassMetadata<?>>();
    classes.add(top);

    for (ClassMetadata<?> cm : sf.listClassMetadata()) {
      if (cm.getSourceClass().isAssignableFrom(top.getSourceClass()))
        classes.add(cm);
    }

    return classes;
  }

  private List<String> getRdfList(String sub, String pred, String modelUri, Transaction txn, 
                                  Map<String, Set<String>> types, Mapper m, SessionFactory sf,
                                  List<Filter> filters)
        throws OtmException {
    String tmodel = modelUri;
    if (m.isAssociation())
      tmodel = getModelUri(sf.getClassMetadata(m.getAssociatedEntity()).getModel(), txn);
    StringBuilder qry = new StringBuilder(500);
    qry.append("select $o $s $n subquery (select $t from <").append(tmodel)
       .append("> where $o <rdf:type> $t) from <").append(modelUri).append("> where ")
       .append("(trans($c <rdf:rest> $s) or $c <rdf:rest> $s or <")
       .append(sub).append("> <").append(pred).append("> $s) and <")
       .append(sub).append("> <").append(pred).append("> $c and $s <rdf:first> $o")
       .append(" and $s <rdf:rest> $n");
    if (m.isAssociation())
      applyObjectFilters(qry, m.getComponentType(), "$o", filters, sf);
    qry.append(";");

    return execCollectionsQry(qry.toString(), txn, types);
  }

  private List<String> getRdfBag(String sub, String pred, String modelUri, Transaction txn,
                                 Map<String, Set<String>> types, Mapper m, SessionFactory sf,
                                 List<Filter> filters)
        throws OtmException {
    String tmodel = modelUri;
    if (m.isAssociation())
      tmodel = getModelUri(sf.getClassMetadata(m.getAssociatedEntity()).getModel(), txn);
    StringBuilder qry = new StringBuilder(500);
    qry.append("select $o $p subquery (select $t from <").append(tmodel)
       .append("> where $o <rdf:type> $t) from <")
       .append(modelUri).append("> where ")
       .append("($s $p $o minus $s <rdf:type> $o) and <")
       .append(sub).append("> <").append(pred).append("> $s");
    if (m.isAssociation())
      applyObjectFilters(qry, m.getComponentType(), "$o", filters, sf);
    qry.append(";");

    return execCollectionsQry(qry.toString(), txn, types);
  }

  private List<String> execCollectionsQry(String qry, Transaction txn,
      Map<String, Set<String>> types) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection(this);
    SessionFactory      sf  = txn.getSession().getSessionFactory();

    log.debug("rdf:List/rdf:Bag query : " + qry);
    List<Answer> ans;
    try {
      ans = isc.getItqlClient().doQuery(qry);
    } catch (IOException ioe) {
      throw new OtmException("error performing rdf:List/rdf:Bag query", ioe);
    } catch (AnswerException ae) {
      throw new OtmException("error performing rdf:List/rdf:Bag query", ae);
    }

    List<String> res = new ArrayList();

    try {
      // check if we got something useful
      Answer qa = ans.get(0);
      if (qa.getVariables() == null)
        throw new OtmException("query failed: " + qa.getMessage());

      // collect the results,
      qa.beforeFirst();
      boolean isRdfList = qa.indexOf("n") != -1;
      if (isRdfList) {
        Map<String, String> fwd  = new HashMap<String, String>();
        Map<String, String> rev  = new HashMap<String, String>();
        Map<String, String> objs = new HashMap<String, String>();
        while (qa.next()) {
          String assoc = qa.getString("o");
          String node  = qa.getString(1);
          String next  = qa.getString(2);
          objs.put(node, assoc);
          fwd.put(node, next);
          rev.put(next, node);
          updateTypeMap(qa.getSubQueryResults(3), assoc, types);
        }

        if (objs.size() == 0)
          return res;

        String first = rev.keySet().iterator().next();
        while (rev.containsKey(first))
          first = rev.get(first);
        for (String n = first; fwd.get(n) != null; n = fwd.get(n))
          res.add(objs.get(n));
      } else {
        while (qa.next()) {
          String p = qa.getString("p");
          if (!p.startsWith(Rdf.rdf + "_"))
            continue; // an un-recognized predicate
          int i = Integer.decode(p.substring(Rdf.rdf.length() + 1)) - 1;
          while (i >= res.size())
            res.add(null);
          String assoc = qa.getString("o");
          res.set(i, assoc);
          updateTypeMap(qa.getSubQueryResults(2), assoc, types);
        }
      }
      // XXX: Type map will contain rdf:Bag, rdf:List, rdf:Seq, rdf:Alt.
      // XXX: These are left over from the rdf:type look-ahead in the main query.
      // XXX: Ideally we should clean it up. But it does not affect the
      // XXX: mostSepcificSubclass selection.

    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }
    return res;
  }

  private void updateTypeMap(Answer qa, String assoc, Map<String, Set<String>> types)
      throws AnswerException {
    qa.beforeFirst();
    while (qa.next()) {
      Set<String> v = types.get(assoc);
      if (v == null)
        types.put(assoc, v = new HashSet<String>());
      v.add(qa.getString("t"));
    }
  }

  public List list(Criteria criteria, Transaction txn) throws OtmException {
    ItqlCriteria ic = new ItqlCriteria(criteria);
    String qry = ic.buildUserQuery();
    log.debug("list: " + qry);
    List<Answer> ans;
    try {
      ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection(this);
      ans = isc.getItqlClient().doQuery(qry);
    } catch (IOException ioe) {
      throw new OtmException("error performing query: " + qry, ioe);
    } catch (AnswerException ae) {
      throw new OtmException("error performing query: " + qry, ae);
    }
    return ic.createResults(ans);
  }

  public Results doQuery(GenericQueryImpl query, Collection<Filter> filters, Transaction txn)
      throws OtmException {
    ItqlQuery iq = new ItqlQuery(query, filters, txn.getSession());
    QueryInfo qi = iq.parseItqlQuery();

    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection(this);
    List<Answer> ans;
    try {
      ans = isc.getItqlClient().doQuery(qi.getQuery());
    } catch (IOException ioe) {
      throw new QueryException("error performing query '" + query + "'", ioe);
    } catch (AnswerException ae) {
      throw new QueryException("error performing query '" + query + "'", ae);
    }

    return new ItqlOQLResults(ans.get(0), qi, iq.getWarnings().toArray(new String[0]),
                              txn.getSession());
  }

  public Results doNativeQuery(String query, Transaction txn) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection(this);
    List<Answer> ans;
    try {
      ans = isc.getItqlClient().doQuery(query);
    } catch (IOException ioe) {
      throw new QueryException("error performing query '" + query + "'", ioe);
    } catch (AnswerException ae) {
      throw new QueryException("error performing query '" + query + "'", ae);
    }

    if (ans.get(0).getMessage() != null)
      throw new QueryException("error performing query '" + query + "' - message was: " +
                               ans.get(0).getMessage());

    return new ItqlNativeResults(ans.get(0), txn.getSession());
  }

  public void doNativeUpdate(String command, Transaction txn) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection(this);
    try {
      isc.getItqlClient().doUpdate(command);
    } catch (IOException ioe) {
      throw new QueryException("error performing command '" + command + "'", ioe);
    }
  }

  private static String getModelUri(String modelId, Transaction txn) throws OtmException {
    ModelConfig mc = txn.getSession().getSessionFactory().getModel(modelId);
    if (mc == null) // Happens if using a Class but the model was not added
      throw new OtmException("Unable to find model '" + modelId + "'");
    return mc.getUri().toString();
  }

  private ItqlClient getItqlClient(URI serverUri, Map<String, String> aliases) throws OtmException {
    synchronized (conCache) {
      ItqlClient res;

      List<ItqlClient> list = conCache.get(serverUri);
      if (list != null && list.size() > 0)
        res = list.remove(list.size() - 1);
      try {
        res = itqlFactory.createClient(serverUri);
      } catch (Exception e) {
        throw new OtmException("Error talking to '" + serverUri + "'", e);
      }

      if (aliases != null)
        res.setAliases(aliases);

      return res;
    }
  }

  private void returnItqlClient(URI serverUri, ItqlClient itql) {
    synchronized (conCache) {
      List<ItqlClient> list = conCache.get(serverUri);
      if (list == null)
        conCache.put(serverUri, list = new ArrayList<ItqlClient>());
      list.add(itql);
    }
  }

  public void createModel(ModelConfig conf) throws OtmException {
    ItqlClient itql = getItqlClient(serverUri, null);
    try {
      String type = (conf.getType() == null) ? "mulgara:Model" : conf.getType().toString();
      itql.doUpdate("create <" + conf.getUri() + "> <" + type + ">;");
      returnItqlClient(serverUri, itql);
    } catch (Exception e) {
      throw new OtmException("Failed to create model <" + conf.getUri() + ">", e);
    }
  }

  public void dropModel(ModelConfig conf) throws OtmException {
    ItqlClient itql = getItqlClient(serverUri, null);
    try {
      itql.doUpdate("drop <" + conf.getUri() + ">;");
      returnItqlClient(serverUri, itql);
    } catch (Exception e) {
      throw new OtmException("Failed to drop model <" + conf.getUri() + ">", e);
    }
  }

  private class ItqlStoreConnection extends AbstractConnection {
    private final SessionFactory sf;
    private       ItqlClient     itql;

    public ItqlStoreConnection(SessionFactory sf) throws OtmException {
      this.sf = sf;
      itql = ItqlStore.this.getItqlClient(ItqlStore.this.serverUri, sf.listAliases());
      try {
        itql.beginTxn("");
      } catch (IOException ioe) {
        throw new OtmException("error starting transaction", ioe);
      }
    }

    public ItqlClient getItqlClient() {
      return itql;
    }

    protected void doPrepare() throws OtmException {
      if (itql == null) {
        log.warn("Called commit but no transaction is active", new Throwable());
        return;
      }

      try {
        itql.commitTxn("");
      } catch (IOException ioe) {
        throw new OtmException("error committing transaction", ioe);
      }
      ItqlStore.this.returnItqlClient(ItqlStore.this.serverUri, itql);
      itql = null;
    }

    protected void doRollback() throws OtmException {
      if (itql == null) {
        log.warn("Called rollback but no transaction is active", new Throwable());
        return;
      }
      abort(true);
    }

    private void abort(boolean throwEx) throws OtmException {
      try {
        itql.rollbackTxn("");
        ItqlStore.this.returnItqlClient(ItqlStore.this.serverUri, itql);
        itql = null;
      } catch (IOException ioe) {
        if (throwEx)
          throw new OtmException("error rolling back transaction", ioe);
        else
          log.warn("Error during rollback", ioe);
      } finally {
        if (itql != null) {
          try {
            itql.close();
          } finally {
            itql = null;
          }
        }
      }
    }

  }
}
