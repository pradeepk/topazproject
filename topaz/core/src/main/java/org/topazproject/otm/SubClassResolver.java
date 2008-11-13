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

import java.util.Collection;

/**
 * An application specific resolver that can resolve to the most specific sub-class based on
 * rdf statements about it.
 *
 * @see SessionFactory#getSubClassMetadata
 *
 * @author Pradeep Krishnan
 */
public interface SubClassResolver {
  /**
   * Resolve the sub-class to use based on a set of RDF statements loaded. Note that
   * this resolver will be called only if the SessionFactory detects multiple candidates
   * all matching a set of rdf:type values.
   *
   * @param superEntity      the starting point (or could be null). If not null, the
   *                         set of rdf:types returned by {@link ClassMetadata#getTypes()}
   *                         is a subset of the rdf:type values supplied in the <code>typeUris</code>.
   * @param instantiatableIn the EntityMode in which an instance can be created.
   *                         This means an
   *                         This means {@link ClassMetadata#getEntityBinder(EntityMode)} must be
   *                         non-null and also
   *                         {@link org.topazproject.otm.mapping.EntityBinder#isInstantiable()}
   *                         must return true for that EntityBinder.
   * @param sf               the SessionFactory to do any additional lookups
   * @param typeUris         collection of rdf:type uris
   * @param statements       the set of RDF statements
   *
   * @return                 the resolved ClassMetadata or null. The resolved ClassMetadata
   *                         must be a sub-class of the <code>superEntity</code>
   */
  public ClassMetadata resolve(ClassMetadata superEntity, EntityMode instantiatableIn,
                    SessionFactory sf, Collection<String> typeUris, TripleStore.Result statements);
}
