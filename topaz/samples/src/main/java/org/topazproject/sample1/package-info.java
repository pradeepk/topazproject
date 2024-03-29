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

@Graphs({
  @Graph(id = "users", uri = "local:///topazproject#users")
})
@Aliases({
  @Alias(alias = "foaf",     value = Rdf.foaf),
  @Alias(alias = "dc",       value = Rdf.dc),
  @Alias(alias = "dcterms",  value = Rdf.dc_terms),
  @Alias(alias = "topaz",    value = Rdf.topaz)
})
package org.topazproject.sample1;

import org.topazproject.otm.annotations.Alias;
import org.topazproject.otm.annotations.Aliases;
import org.topazproject.otm.annotations.Graph;
import org.topazproject.otm.annotations.Graphs;
import org.topazproject.otm.Rdf;
