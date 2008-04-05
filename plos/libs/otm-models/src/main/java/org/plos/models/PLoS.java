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
package org.plos.models;

/**
 * Definitions of some standard uris.
 *
 * @author Amit Kapoor
 */
public interface PLoS {
  /** PLoS namespace */
  public static final String plos               = "http://rdf.plos.org/RDF/";
  /** Creative Commons namespace */
  public static final String creativeCommons    = "http://web.resource.org/cc/";
  /** Bibtex namespace */
  public static final String bibtex             = "http://purl.org/net/nknouf/ns/bibtex#";
  /** Prism namespace */
  public static final String prism              = "http://prismstandard.org/namespaces/1.2/basic/";

  /** Base name space for article types for PLoS */
  public static final String PLOS_ArticleType = plos + "articleType/";
  /** Base name for PLoS predicates of temporal types */
  public static final String PLoS_Temporal = plos + "temporal#";
  /** Base name for PLoS predicates for citations */
  public static final String PLoS_Citation = plos + "citation/";

  /** Base name for PLoS Citation types */
  public static final String PLoS_CitationTypes = PLoS_Citation + "type#";
}
