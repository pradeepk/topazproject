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

import org.topazproject.otm.metadata.RdfBuilder;
import org.topazproject.otm.stores.ItqlStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Groovy-builder offline tests.
 */
public class BuilderTest extends GroovyTestCase {
  def rdf

  void setUp() {
    rdf = new RdfBuilder(defModel:'ri', defBaseUri:'topaz:')
  }

  void testBuilderBasic() {
    Class cls = rdf.class('Test1', baseUri:'http://rdf.topazproject.org/RDF/') {
      uri   (isId:true)
      state (pred:'accountState', type:'xsd:int')
      name {
        givenName () 'Peter'
        surname   ()
      }
      goals (maxCard:-1, colType:'Set', colMapping:'RdfBag')
    }

    def obj = cls.newInstance(uri:'foo:1', state:1,
                              name:[id:'foo:n1', givenName:'John', surname:'Muir'],
                              goals:['one', 'two'] as Set)

    obj = cls.newInstance(uri:'foo:1', state:1, name:[id:'foo:n1'], goals:['one', 'two'] as Set)
    assert obj.name == obj.name.class.newInstance(id:'foo:n1', givenName:'Peter')
  }

  void testBuilderFields() {
    // all defaults, untyped literal
    Class cls = rdf.class('Test1') {
      state ()
    }
    ClassMetadata cm = rdf.sessFactory.getClassMetadata(cls)

    assert cm.type          == 'topaz:Test1'
    assert cm.model         == 'ri'
    assert cm.fields.size() == 1

    def m = cm.fields.iterator().next()
    assert m.name     == 'state'
    assert m.type     == String.class
    assert m.dataType == null
    assert m.uri      == 'topaz:state'
    assert !m.hasInverseUri()

    // relative-uri overrides, typed literal
    cls = rdf.class('Test2', type:'Test2', model:'m2') {
      state (pred:'p2', type:'xsd:int')
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assert cm.type          == 'topaz:Test2'
    assert cm.model         == 'm2'
    assert cm.fields.size() == 1

    m = cm.fields.iterator().next()
    assert m.name     == 'state'
    assert m.type     == Integer.TYPE
    assert m.dataType == 'http://www.w3.org/2001/XMLSchema#int'
    assert m.uri      == 'topaz:p2'
    assert !m.hasInverseUri()

    // absolute-uri overrides, class type
    Class cls2 = cls
    cls = rdf.class('Test3', type:'foo:Test3', model:'m3') {
      state (pred:'foo:p3', type:'Test2')
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assert cm.type          == 'foo:Test3'
    assert cm.model         == 'm3'
    assert cm.fields.size() == 1

    m = cm.fields.iterator().next()
    assert m.name     == 'state'
    assert m.type     == cls2
    assert m.dataType == null
    assert m.uri      == 'foo:p3'
    assert !m.hasInverseUri()

    // nested class type
    cls = rdf.class('Test4', type:'foo:Test4', model:'m4') {
      state (pred:'foo:p4', model:'m41', baseUri:'bar4:') {
        value ()
        history (maxCard:-1) {
          value ()
        }
      }
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assert cm.type          == 'foo:Test4'
    assert cm.model         == 'm4'
    assert cm.fields.size() == 1

    m = cm.fields.iterator().next()
    assert m.name     == 'state'
    assert m.type     == rdf.sessFactory.getClassMetadata('State').sourceClass
    assert m.dataType == null
    assert m.uri      == 'foo:p4'
    assert !m.hasInverseUri()

    cm = rdf.sessFactory.getClassMetadata('State')

    assert cm.type          == 'bar4:State'
    assert cm.model         == 'm41'
    assert cm.fields.size() == 2

    m = cm.fields.asList()[0]
    assert m.name     == 'value'
    assert m.type     == String.class
    assert m.dataType == null
    assert m.uri      == 'bar4:value'
    assert !m.hasInverseUri()

    m = cm.fields.asList()[1]
    assert m.name          == 'history'
    assert m.type          == List.class
    assert m.componentType == rdf.sessFactory.getClassMetadata('History').sourceClass
    assert m.dataType      == null
    assert m.uri           == 'bar4:history'
    assert !m.hasInverseUri()

    cm = rdf.sessFactory.getClassMetadata('History')

    assert cm.type          == 'bar4:History'
    assert cm.model         == 'm41'
    assert cm.fields.size() == 1

    m = cm.fields.asList()[0]
    assert m.name     == 'value'
    assert m.type     == String.class
    assert m.dataType == null
    assert m.uri      == 'bar4:value'
    assert !m.hasInverseUri()

    // uri type
    cls = rdf.class('Test5', type:'foo:Test5', model:'m5') {
      state (pred:'foo:p5', type:'xsd:anyURI', inverse:true)
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assert cm.type          == 'foo:Test5'
    assert cm.model         == 'm5'
    assert cm.fields.size() == 1

    m = cm.fields.iterator().next()
    assert m.name     == 'state'
    assert m.type     == URI.class
    assert m.dataType == null
    assert m.uri      == 'foo:p5'
    assert m.hasInverseUri()
  }

  void testBuilderCollections() {
    // Set, RdfBag
    Class cls = rdf.class('Test1') {
      goals (maxCard:-1, colType:'Set', colMapping:'RdfBag')
    }

    def obj = cls.newInstance(id:'foo:1', goals:['one', 'two'] as Set)
    assert obj.goals instanceof Set

    // List, RdfSeq
    cls = rdf.class('Test2') {
      goals (maxCard:-1, colType:'List', colMapping:'RdfSeq')
    }
    obj = cls.newInstance(id:'foo:1', goals:['one', 'two'])
    assert obj.goals instanceof List

    // List, RdfList
    cls = rdf.class('Test3') {
      goals (maxCard:-1, colType:'List', colMapping:'RdfList')
    }
    obj = cls.newInstance(id:'foo:1', goals:['one', 'two'])
    assert obj.goals instanceof List

    // String[], RdfAlt
    cls = rdf.class('Test4') {
      goals (maxCard:-1, colType:'Array', colMapping:'RdfAlt')
    }
    obj = cls.newInstance(id:'foo:1', goals:['one', 'two'])
    assert obj.goals instanceof String[]

    // int[], predicate
    cls = rdf.class('Test5') {
      goals (maxCard:-1, colType:'Array', type:'xsd:int', colMapping:'Predicate')
    }
    obj = cls.newInstance(id:'foo:1', goals:[1, 2])
    assert obj.goals instanceof int[]

    // illegal collection type
    assert shouldFail(OtmException.class, {
      cls = rdf.class('Test6') {
        goals (maxCard:-1, colType:'Foo', colMapping:'Predicate')
      }
    }).contains('Unknown collection type')

    // illegal collection mapping
    assert shouldFail(OtmException.class, {
      cls = rdf.class('Test7') {
        goals (maxCard:-1, colType:'List', colMapping:'cool')
      }
    }).contains('Unknown collection-mapping type')
  }

  void testBuilderIdField() {
    // explicit id-field
    Class cls = rdf.class('Test1') {
      uri (isId:true)
      state (type:'xsd:int')
    }
    def obj = cls.newInstance(uri:'foo:1', state:1)
    shouldFail(MissingPropertyException, { obj.id })

    // generated id-field
    cls = rdf.class('Test2') {
      state (type:'xsd:int')
    }
    obj = cls.newInstance(id:'foo:1', state:1)

    // id-field name collision
    assert shouldFail(OtmException, {
      cls = rdf.class('Test2') {
        id    ()
        state (type:'xsd:int')
      }
    }).contains("one field is already named 'id'")

    // multiple id-fields
    assert shouldFail(OtmException, {
      cls = rdf.class('Test2') {
        id1   (isId:true)
        id2   (isId:true)
        state (type:'xsd:int')
      }
    }).contains('more than one id-field defined')
  }

  void testBuilderClassInheritance() {
    // basic extending
    Class base = rdf.class('Base1', isAbstract:true) {
      state (type:'xsd:int')
    }

    Class ext = rdf.class('Ext1', extendsClass:'Base1') {
      color ()
    }

    shouldFail(NoSuchFieldException, { base.getDeclaredField('id') })

    def obj  = ext.newInstance(id:'foo:1', state:42, color:'blue')
    assert obj == ext.newInstance(id:'foo:1', state:42, color:'blue')
    assert obj != ext.newInstance(id:'foo:2', state:42, color:'blue')
    assert obj != ext.newInstance(id:'foo:1', state:43, color:'blue')
    assert obj != ext.newInstance(id:'foo:1', state:42, color:'red')

    assert obj.hashCode() == 'foo:1'.hashCode()

    // id inheritance
    base = rdf.class('Base2', isAbstract:true) {
      uri   (isId:true)
      state (type:'xsd:int')
    }

    ext = rdf.class('Ext2', extendsClass:'Base2') {
      color ()
    }

    obj = ext.newInstance(uri:'foo:1', state:42, color:'blue')
    shouldFail(MissingPropertyException, { obj.id })
    assert obj.hashCode() == 'foo:1'.hashCode()

    // non-abstract base
    base = rdf.class('Base3') {
      uri   (isId:true)
      state (type:'xsd:int')
    }

    ext = rdf.class('Ext3', extendsClass:'Base3') {
      color ()
    }

    obj = ext.newInstance(uri:'foo:1', state:42, color:'blue')
    shouldFail(MissingPropertyException, { obj.id })
    assert obj.hashCode() == 'foo:1'.hashCode()
  }
}
