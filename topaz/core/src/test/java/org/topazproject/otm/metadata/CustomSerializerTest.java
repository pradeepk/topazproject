/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2011 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.otm.metadata;

import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.CascadeType;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.serializer.Serializer;
import org.topazproject.otm.serializer.SerializerFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Predicate.PropType;
import org.topazproject.otm.annotations.UriPrefix;

public class CustomSerializerTest extends TestCase {

  private void common(boolean sub) throws Exception {
    SessionFactory sf = new SessionFactoryImpl();
    sf.getSerializerFactory().setSerializer(MyClass.class, Rdf.xsd + "myType", new MySerializer(), sub);
    sf.preload(MyClassUser.class);
    sf.validate();
    RdfDefinition def = (RdfDefinition) sf.getClassMetadata(MyClassUser.class).getMapperByName("myClass1").getDefinition();
    assertFalse(def.isAssociation());
    def = (RdfDefinition) sf.getClassMetadata(MyClassUser.class).getMapperByName("myClass2").getDefinition();
    assertFalse(def.isAssociation());
    def = (RdfDefinition) sf.getClassMetadata(MyClassUser.class).getMapperByName("myClass3").getDefinition();
    assertFalse(def.isAssociation());
  }

  public void test01() throws Exception {
    common(true);
  }

  public void test02() throws Exception {
    common(false);
  }

  @Entity(graph="test", name="RdfNode")
  @UriPrefix("a:")
  public static interface RdfNode {
    public String getId();
    @Id
    public void setId(String s);
  }

  @Entity(graph="test", name="Base")
  @UriPrefix("a:")
  public static class Base implements RdfNode {
    public String getId() {return null;}
    public void setId(String s) {}
  }

  public static class MyClass {
    public String v1, v2;

    public String  toString () {
      return v1 + ":" + v2;
    }

    public static MyClass fromString(String s) {
      MyClass o = new MyClass();
      String v[] = s.split(":");
      if (v.length > 1)
        o.v2 = v[1];
      if (v.length > 0)
        o.v1 = v[0];
      return o;
    }

  }

  public static class MySerializer implements Serializer<MyClass> {

    public String serialize(MyClass o) throws Exception {
      return (o == null) ? null : o.toString();
    }

    public MyClass deserialize(String o, Class c) throws Exception {
      return MyClass.fromString(o);
    }
  }

  @Entity(name="MyClassUser")
  public static class MyClassUser extends Base {
    public MyClass getMyClass1() {return null;}
    @Predicate(type = PropType.DATA)
    public void setMyClass1(MyClass s) {}

    public MyClass getMyClass2() {return null;}
    @Predicate(type = PropType.DATA, dataType = Rdf.xsd + "myClass")
    public void setMyClass2(MyClass s) {}

    public MyClass getMyClass3() {return null;}
    @Predicate(type = PropType.DATA, dataType = Predicate.UNTYPED)
    public void setMyClass3(MyClass s) {}
  }

}

