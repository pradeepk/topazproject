/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.topazproject.otm.query.Results;
import org.topazproject.otm.mapping.Mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * This represents view object which is based on an OQL query. Instances are obtained via {@link
 * Session#createView Session.createView()}.
 * 
 * @author Ronald Tschalär
 */
public class View<T> implements Parameterizable<View<T>> {
  private static final Log log = LogFactory.getLog(View.class);

  private final Session          sess;
  private final ClassMetadata<T> cm;
  private final Query            query;

  /** 
   * Create a new view instance. 
   * 
   * @param sess    the session this is attached to
   * @param clazz   the view class
   * @param filters the filters that should be applied to this view
   */
  View(Session sess, Class<T> clazz, Collection<Filter> filters) throws OtmException {
    this.sess = sess;

    this.cm = sess.getSessionFactory().getClassMetadata(clazz);
    if (cm == null)
      throw new IllegalArgumentException("class '" + clazz.getName() + "' has not been registered");
    if (cm.getQuery() == null)
      throw new IllegalArgumentException("class '" + clazz.getName() + "' is not a View");

    this.query = new Query(sess, cm.getQuery(), filters);
  }

  /** 
   * Execute the query.
   * 
   * @return the view instances
   * @throws OtmException
   */
  public List<T> list() throws OtmException {
    Results r = query.execute();

    List<T> res = new ArrayList<T>();
    while (r.next())
      res.add(createInstance(cm, r));

    return res;
  }

  private <S> S createInstance(ClassMetadata<S> cm, Results r) throws OtmException {
    S obj;
    try {
      obj = cm.getSourceClass().newInstance();
    } catch (Exception e) {
      throw new OtmException("Failed to create instance of '" + cm.getSourceClass().getName() + "'",
                             e);
    }

    for (Mapper m : cm.getFields()) {
      int    idx = r.findVariable(m.getProjectionVar());
      Object val = getValue(r, idx, m.getComponentType());
      if (val instanceof List)
        m.set(obj, (List) val);
      else
        m.setRawValue(obj, val);
    }

    return obj;
  }

  private Object getValue(Results r, int idx, Class<?> type) throws OtmException {
    switch (r.getType(idx)) {
      case CLASS:
        return (type == String.class) ? r.getString(idx) : r.get(idx);

      case LITERAL:
        return (type == String.class) ? r.getString(idx) : r.getLiteralAs(idx, type);

      case URI:
        return (type == String.class) ? r.getString(idx) : r.getURIAs(idx, type);

      case SUBQ_RESULTS:
        ClassMetadata<?> scm = sess.getSessionFactory().getClassMetadata(type);
        List<Object> vals = new ArrayList<Object>();

        Results sr = r.getSubQueryResults(idx);
        sr.beforeFirst();
        while (sr.next())
          vals.add(scm != null ? createInstance(scm, sr) : getValue(sr, 0, type));

        return vals;

      default:
        throw new Error("unknown type " + r.getType(idx) + " encountered");
    }
  }

  public Set<String> getParameterNames() {
    return query.getParameterNames();
  }

  public View<T> setParameter(String name, Object val) throws OtmException {
    query.setParameter(name, val);
    return this;
  }

  public View<T> setUri(String name, URI val) throws OtmException {
    query.setUri(name, val);
    return this;
  }

  public View<T> setPlainLiteral(String name, String val, String lang) throws OtmException {
    query.setPlainLiteral(name, val, lang);
    return this;
  }

  public View<T> setTypedLiteral(String name, String val, URI dataType) throws OtmException {
    query.setTypedLiteral(name, val, dataType);
    return this;
  }
}
