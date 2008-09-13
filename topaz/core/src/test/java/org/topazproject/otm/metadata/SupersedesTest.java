/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

public class SupersedesTest extends TestCase {
  private SessionFactory sf = new SessionFactoryImpl();
  private RdfDefinition assoc, extended;

  public void setUp() throws OtmException {
    sf.preload(B.class);
    sf.preload(Extended.class);

    assoc = (RdfDefinition)sf.getDefinition("A:assoc");
    extended = (RdfDefinition)sf.getDefinition("B:assoc");

    extended.resolveReference(sf);
  }

  private void compare(RdfDefinition def, Object[] vals) {
    String message = "Testing '" + def.getName() + "': ";
    assertEquals(message, vals[0], def.getUri());
    assertEquals(message, vals[1], def.isEntityOwned());
    assertEquals(message, vals[2], def.getModel());
    assertEquals(message, vals[3], def.hasInverseUri());
    assertEquals(message, vals[4], def.typeIsUri());
    assertEquals(message, vals[5], def.getDataType());
    assertEquals(message, vals[6], def.getAssociatedEntity());
    assertEquals(message, vals[7], def.getColType());
    assertEquals(message, vals[8], def.getFetchType());
    assertEquals(message, vals[9], def.getCascade());
    assertEquals(message, vals[10], def.isAssociation());
  }

  public void test01() {
    Object[] vals = new Object[] {"a:assoc", true, "test", false, true, null, "Assoc",
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), true};

    compare(assoc, vals);
    vals[6] = "Extended";
    compare(extended, vals);

    assertTrue(extended.refersSameGraphNodes(assoc));
    assertTrue(extended.refersSameRange(assoc));
  }

  public void test02() {
    ClassMetadata cm = sf.getClassMetadata("A");
    assertEquals(1, cm.getRdfMappers().size());
    assertEquals("assoc", cm.getRdfMappers().iterator().next().getName());
    assertEquals("Assoc", cm.getRdfMappers().iterator().next().getAssociatedEntity());

    cm = sf.getClassMetadata("B");
    assertEquals(1, cm.getRdfMappers().size());
    assertEquals("assoc", cm.getRdfMappers().iterator().next().getName());
    assertEquals("Extended", cm.getRdfMappers().iterator().next().getAssociatedEntity());
  }

  @Entity(model="test", name="Base")
  public static class Base {
    public String getId() {return null;}
    @Id
    public void setId(String s) {}
  }

  @UriPrefix("a:")
  @Entity(name="A")
  public static class A extends Base {
    public Assoc getAssoc() {return null;}
    @Predicate(model="test")
    public void setAssoc(Assoc a) {}
  }

  @UriPrefix("b:")
  @Entity(name="B")
  public static class B extends A {
    @Override
    public Extended getAssoc() {return null;}
    @Predicate
    public void setAssoc(Extended a) {}
  }

  @Entity(type="t:assoc", name="Assoc")
  public static class Assoc extends Base {
  }

  @Entity(type="t:extended", name="Extended")
  public static class Extended extends Assoc {
  }
}
