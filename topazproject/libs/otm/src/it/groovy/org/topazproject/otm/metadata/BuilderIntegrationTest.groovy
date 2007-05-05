/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.metadata;

import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.stores.ItqlStore;

/**
 * Integration tests for groovy-builder.
 */
public class BuilderIntegrationTest extends GroovyTestCase {
  def rdf;

  void setUp() {
    def store =
        new ItqlStore("http://localhost:9091/mulgara-service/services/ItqlBeanService".toURI())
    rdf = new RdfBuilder(
        sessFactory:new SessionFactory(tripleStore:store), defModel:'ri', defUriPrefix:'topaz:')

    def ri = new ModelConfig("ri", "local:///topazproject#otmtest1".toURI(), null)
    rdf.sessFactory.addModel(ri);

    try {
      store.dropModel(ri)
    } catch (Throwable t) {
    }
    store.createModel(ri)
  }

  void testBuilderSimple() {
    Class t1 = rdf.class("Test1", uriPrefix:'http://rdf.topazproject.org/RDF/') {
      uri   (isId:true)
      state (pred:'accountState', type:'xsd:int')
      name {
        givenName () 'Peter'
        surname   ()
      }
      goals (maxCard:-1, colMapping:'RdfBag')
    }

    def i1 = t1.newInstance(uri:'foo:1', state:1,
                            name:[id:'foo:n1'.toURI(), givenName:'John', surname:'Muir'],
                            goals:['one', 'two'])

    doInTx { s -> s.saveOrUpdate(i1) }
    doInTx { s -> assert s.get(t1, "foo:1") == i1 }

    i1 = t1.newInstance(uri:'foo:1', state:1, name:[id:'foo:n1'.toURI()], goals:['one', 'two'])

    doInTx { s -> s.saveOrUpdate(i1) }
    doInTx { s -> assert s.get(t1, "foo:1") == i1 }

    doInTx { s -> s.delete(i1) }
    doInTx { s -> assert s.get(t1, "foo:1") == null }
  }

  void testBuilderIdGenerator() {
    // default gen
    Class cls = rdf.class('Test1') {
      state (type:'xsd:int')
    }

    def obj = cls.newInstance(id:'foo:1'.toURI(), state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assert obj.id == 'foo:1'.toURI()

    obj = cls.newInstance(state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assert obj.id != null

    // explicit gen
    cls = rdf.class('Test2', idGenerator:'GUID') {
      state (type:'xsd:int')
    }

    obj = cls.newInstance(id:'foo:1'.toURI(), state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assert obj.id == 'foo:1'.toURI()

    obj = cls.newInstance(state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assert obj.id != null

    // no gen
    cls = rdf.class('Test3', idGenerator:null) {
      state (type:'xsd:int')
    }

    obj = cls.newInstance(id:'foo:1'.toURI(), state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assert obj.id == 'foo:1'.toURI()

    obj = cls.newInstance(state:42)
    shouldFail(OtmException, { doInTx { s -> s.saveOrUpdate(obj) } })

    // inherited (no) gen
    Class base = rdf.class('Base4', idGenerator:null) {
    }

    cls = rdf.class('Test4', extendsClass:'Base4') {
      state (type:'xsd:int')
    }

    obj = cls.newInstance(id:'foo:1'.toURI(), state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assert obj.id == 'foo:1'.toURI()

    obj = cls.newInstance(state:42)
    shouldFail(OtmException, { doInTx { s -> s.saveOrUpdate(obj) } })
  }

  private def doInTx(Closure c) {
    Session s = rdf.sessFactory.openSession()
    s.beginTransaction()
    try {
      c(s)
    } finally {
      s.transaction.commit()
      s.close()
    }
  }
}
