/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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
package org.plos.model;

import java.net.URI;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;

/**
 * Just the full name.
 */
@Entity(type = Rdf.foaf + "Person", model = "profiles")
public class UserProfileInfo {
  @Id
  public URI id;

  @Predicate(uri = Rdf.foaf + "name")
  public String realName;
}
