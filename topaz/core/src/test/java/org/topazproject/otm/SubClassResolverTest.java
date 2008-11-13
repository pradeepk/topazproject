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

package org.topazproject.otm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.impl.SessionFactoryImpl;

public class SubClassResolverTest extends TestCase {
  private SessionFactory sf = new SessionFactoryImpl();

  public void testSubClassMetadata() {
    sf.preload(Sub.class);
    sf.preload(ExtBase.class);
    sf.preload(Spoiler.class);
    sf.validate();

    ClassMetadata nt = sf.getClassMetadata(NoType.class);
    ClassMetadata base = sf.getClassMetadata(Base.class);
    ClassMetadata ext = sf.getClassMetadata(ExtBase.class);
    ClassMetadata sub = sf.getClassMetadata(Sub.class);
    ClassMetadata spoiler = sf.getClassMetadata(Spoiler.class);

    assertTrue(nt.isAssignableFrom(base, EntityMode.POJO));
    assertTrue(nt.isAssignableFrom(sub, EntityMode.POJO));
    assertTrue(nt.isAssignableFrom(ext, EntityMode.POJO));
    assertTrue(base.isAssignableFrom(base, EntityMode.POJO));
    assertTrue(base.isAssignableFrom(sub, EntityMode.POJO));
    assertTrue(base.isAssignableFrom(ext, EntityMode.POJO));
    assertTrue(sub.isAssignableFrom(sub, EntityMode.POJO));
    assertFalse(sub.isAssignableFrom(ext, EntityMode.POJO));
    assertFalse(sub.isAssignableFrom(base, EntityMode.POJO));
    assertTrue(ext.isAssignableFrom(ext, EntityMode.POJO));
    assertFalse(ext.isAssignableFrom(sub, EntityMode.POJO));
    assertFalse(ext.isAssignableFrom(base, EntityMode.POJO));

    TripleStore.Result r = new EmptyResult();

    List<String> baseT = Collections.singletonList("base:type");
    List<String> subT = Collections.singletonList("sub:type");
    List<String> junkT = Collections.singletonList("junk:type");
    List<String> emptyT = Collections.emptyList();
    List<String> bothT = new ArrayList<String>(baseT);
    bothT.addAll(subT);
    List<String> bothR = new ArrayList<String>(bothT);
    Collections.reverse(bothR);

    Set<ClassMetadata> randoms = new HashSet<ClassMetadata>();
    for (String sel : new String[] {spoiler.getName(), sub.getName()}) {
      String m1 = "sel = " + sel;
      for (String ent : new String[] {null, nt.getName(), base.getName(),
                                      sub.getName(), spoiler.getName()}) {
        String m2 = m1 + ", ent = " + ent;
        SubSelector selector = new SubSelector(sel);
        sf.addSubClassResolver(ent, selector);
        // resolves to most specific when sub:type is present
        for (List<String> types : new List[] {bothT, bothR, subT}) {
          String m3 = m2 + ", types = " + types;
          for (ClassMetadata cm : new ClassMetadata[] {base, nt, null}) {
            String m4 = m3 + ", cm = " + ((cm == null) ? "null" : cm.getName());
            assertEquals(m4, sf.getClassMetadata(sel), 
                             sf.getSubClassMetadata(cm, EntityMode.POJO, types, r));
          }
          assertEquals(m3, sub, sf.getSubClassMetadata(sub, EntityMode.POJO, types, r));
          assertEquals(m3, spoiler, sf.getSubClassMetadata(spoiler, EntityMode.POJO, types, r));
        }
        sf.removeSubClassResolver(selector);
        assertEquals(m2, 0, sf.listRegisteredSubClassResolvers(ent).size());
      }
      // add an unrelated resolver
      SubSelector selector = new SubSelector(sel);
      sf.addSubClassResolver(ext.getName(), selector);
      randoms.add(sf.getSubClassMetadata(base, EntityMode.POJO, subT, r));
      sf.removeSubClassResolver(selector);
      assertEquals(m1, 0, sf.listRegisteredSubClassResolvers(ext.getName()).size());
    }

    // no resolvers - so random. However all 3 randoms must be the same
    randoms.add(sf.getSubClassMetadata(base, EntityMode.POJO, subT, r));
    assertEquals(1, randoms.size());
    assertNotNull(randoms.iterator().next());

    // resolve to most specific when just the base:type value present
    for (ClassMetadata cm : new ClassMetadata[] {ext, base, nt, null})
      assertEquals(ext, sf.getSubClassMetadata(cm, EntityMode.POJO, baseT, r));

    // resolve untyped
    assertEquals(nt, sf.getSubClassMetadata(nt, EntityMode.POJO, emptyT, r));

    // ignore unrelated rdf:type values
    assertEquals(nt, sf.getSubClassMetadata(nt, EntityMode.POJO, junkT, r));
    for (List<String> types : new List[] {bothT, bothR})
      assertEquals(ext, sf.getSubClassMetadata(ext, EntityMode.POJO, types, r));

    // insufficient types to make assertion
    assertNull(sf.getSubClassMetadata(sub, EntityMode.POJO, baseT, r));

    // in the absence of base:type, sub:type is not sufficient to make inference (no OWL yet)
    assertNull(sf.getSubClassMetadata(ext, EntityMode.POJO, subT, r));

    // missing base:type
    for (List<String> types : new List[] {junkT, emptyT, null})
      assertNull(sf.getSubClassMetadata(base, EntityMode.POJO, types, r));

    assertEquals(nt, sf.getSubClassMetadata(null, EntityMode.POJO, emptyT, r));

  }

  @Entity(name="NoType")
  public static class NoType {
    @Id
    public String getId() {return null;}
    public void setId(String id) {}
  }

  @Entity(name="Base", types = {"base:type"})
  public static class Base extends NoType {
  }

  @Entity(name="ExtBase")
  public static class ExtBase extends Base {
  }

  @Entity(name="Sub", types = {"sub:type"})
  public static class Sub extends Base {
  }

  @Entity(name="Spoiler", types = {"sub:type"})
  public static class Spoiler extends Base {
  }

  public static class SubSelector implements SubClassResolver {
    private final String selection;

    public SubSelector(String s) {
      selection = s;
    }

    public ClassMetadata resolve(ClassMetadata superEntity, EntityMode instantiatableIn,
                                 SessionFactory sf, Collection<String> typeUris, 
                                 TripleStore.Result statements) {
      if (typeUris.contains("sub:type"))
        return sf.getClassMetadata(selection);
      return null;
    }
  }

  public static class EmptyResult implements TripleStore.Result {
    public Map<String, List<String>> getFValues() {return Collections.emptyMap();}
    public Map<String, List<String>> getRValues() {return Collections.emptyMap();}
  }

}
