/* $HeadURL::                                                                                    $
 * $Id: $
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.plos.article.util

import org.plos.util.ToolHelper
import org.plos.configuration.ConfigurationStore
import org.topazproject.xml.transform.cache.CachedSource

/**
 * Wrap a fedora article in an object to extract additional information.
 *
 * @author Eric Brown
 */
class FedoraArticle {
  def article
  String url
  List authors = new ArrayList()
  List contributors = new ArrayList()
  int volume
  int issue

  /**
   * Construct a fedora article object.
   *
   * @param doi The article doi to read in
   */
  FedoraArticle(String doi) {
    // Get an XmlSlurper object and use our resolver (without resolver, parsing takes forever)
    def slurper = new XmlSlurper()
    slurper.setEntityResolver(CachedSource.getResolver())

    // Get configuration information we need
    def conf = ConfigurationStore.getInstance().configuration
    def fedoraUri = conf.getString("ambra.services.topaz.fedora.base-url") + "get"

    this.url = "$fedoraUri/doi:${URLEncoder.encode(doi.substring(9))}/XML"
    this.article = slurper.parse(new URL(this.url).getContent())
    article.front.'article-meta'.'contrib-group'.contrib.each() {
      def name = ((it.name.@'name-style' == "eastern") 
                       ? "${it.name.surname} ${it.name.'given-names'}"
                       : "${it.name.'given-names'} ${it.name.surname}")
      switch(it.@'contrib-type') {
      case 'author': this.authors += name.toString(); break
      case 'contributor': this.contributors += name.toString(); break
      }
    }
    this.volume = Integer.valueOf(article.front.'article-meta'.volume.toString())
    this.issue = Integer.valueOf(article.front.'article-meta'.issue.toString())
  }

  /**
   * Command line to test FedoraArticle.
   */
  static void main(String[] args) {
    ConfigurationStore.getInstance().loadDefaultConfiguration()
    args = ToolHelper.fixArgs(args)
    // TODO: accept and parse real arguments
    def fa = new FedoraArticle(args[0])
    println "Url: " + fa.url
    println "Authors: " + fa.authors
    println "Contributors: " + fa.contributors
    println "Volume/Issue: ${fa.volume}/${fa.issue}"
//    new XmlNodePrinter().print(new XmlParser().parseText(fa.content))
  }
}
