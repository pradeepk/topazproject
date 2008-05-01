/* $HeadURL::                                                                                     $
 * $Id$
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

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.stores.ItqlStore;

import org.plos.models.Article
import org.plos.models.ObjectInfo
import org.plos.models.Category
import org.plos.models.Citation
import org.plos.models.PLoS
import org.plos.models.UserProfile

import org.plos.util.ToolHelper

args = ToolHelper.fixArgs(args)

def cli = new CliBuilder(usage: 'Delete [-c config-overrides.xml] article-uris ...')
cli.h(longOpt:'help', "help (this message)")
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')
cli.i(args:0, 'ignore errors')

// Display help if requested
def opt = cli.parse(args); if (opt.h) { cli.usage(); return }

// Load configuration before ArticleUtil is instantiated
CONF = ToolHelper.loadConfiguration(opt.c)

def factory = new SessionFactoryImpl();
def itql = new ItqlStore(URI.create(CONF.getString("ambra.topaz.tripleStore.mulgara.itql.uri")))
def ri = new ModelConfig("ri", URI.create(CONF.getString("ambra.models.articles")), null)
def p = new ModelConfig("profiles", URI.create(CONF.getString("ambra.models.profiles")), null)
factory.setTripleStore(itql)
factory.addModel(ri)
factory.addModel(p)
factory.preload(Article.class)
factory.preload(Category.class)
factory.preload(Citation.class)
factory.preload(UserProfile.class)
def session = factory.openSession()

// We may want to ignore errors if something needs to be cleaned up
ignore = opt.i
def process(c) {
  try { c() } catch (Exception e) {
    if (ignore) { println "${e.getClass()}: ${e}" }
    else { throw e }
  }
}

// Get directories zip files are stashed in
def queueDir    = CONF.getString('ambra.services.documentManagement.ingestSourceDir', '/var/spool/plosone/ingestion-queue')
def ingestedDir = CONF.getString('ambra.services.documentManagement.ingestDestinationDir', '/var/spool/plosone/ingested')

opt.arguments().each() { uri ->
  // Call ArticleUtil.delete() to remove from mulgara, fedora & lucene
  print "Deleting article $uri..."
  process() {
    def tx = session.beginTransaction()
    ArticleUtil.delete(uri, session)
    tx.commit()
  }
  println "done"
}

println "Tried to delete ${opt.arguments().size()} article(s)"
