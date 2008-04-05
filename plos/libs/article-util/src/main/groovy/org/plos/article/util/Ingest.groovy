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

import javax.activation.DataHandler;
import javax.activation.URLDataSource
import org.plos.article.util.Zip
import org.plos.util.ToolHelper

args = ToolHelper.fixArgs(args)

def cli = new CliBuilder(usage: 'Ingest [-c config-overrides.xml] zip')
cli.h(longOpt:'help', "help (this message)")
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')

// Display help if requested
def opt = cli.parse(args); if (opt.h) { cli.usage(); return }

// Load configuration before ArticleUtil is instantiated
CONF = ToolHelper.loadConfiguration(opt.c)

def util = new ArticleUtil()

opt.arguments().each() {
  // Zip.FileZip
/*
  def zip = new Zip.FileZip(it)
  def doi = util.ingest(zip)
  println "Ingested $it: $doi w/Zip.FileZip"
*/

  // Zip.DataSource
  def zip = new Zip.DataSourceZip(new URLDataSource(it.toURL()));
  def doi = util.ingest(zip)
  println "Ingested $it: $doi w/Zip.DataSourceZip"

}

println "Ingested ${opt.arguments().size()} article(s)"
