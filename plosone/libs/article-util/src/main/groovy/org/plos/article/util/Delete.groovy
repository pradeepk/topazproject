/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.util

args = ToolHelper.fixArgs(args)

def cli = new CliBuilder(usage: 'Delete [-c config-overrides.xml] article-uris ...')
cli.h(longOpt:'help', "help (this message)")
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')
cli.i(args:0, 'ignore errors')

// Display help if requested
def opt = cli.parse(args); if (opt.h) { cli.usage(); return }

// Load configuration before ArticleUtil is instantiated
CONF = ToolHelper.loadConfiguration(opt.c)

// We may want to ignore errors if something needs to be cleaned up
ignore = opt.i
def process(c) {
  try { c() } catch (Exception e) {
    if (ignore) { println "${e.getClass()}: ${e}" }
    else { throw e }
  }
}

// Get directories zip files are stashed in
def queueDir    = CONF.getString('pub.spring.ingest.source', '/var/spool/plosone/ingestion-queue')
def ingestedDir = CONF.getString('pub.spring.ingest.destination', '/var/spool/plosone/ingested')

def util = new ArticleUtil()
opt.arguments().each() { uri ->
  // Call ArticleUtil.delete() to remove from mulgara, fedora & lucene
  process() { util.delete(uri) }

  // Clean up /var/spool/plosone
  def ant = new AntBuilder()
  def ingestedXmlFile = new File(ingestedDir, uri.replaceAll('[:/.]', '_') + '.xml')
  process() { ant.delete(file: ingestedXmlFile.toString()) }

  if (!queueDir.equals(ingestedDir)) {
    def fname    = uri[25..-1] + ".zip"
    def fromFile = new File(ingestedDir, fname).toString()
    def toFile   = new File(queueDir,    fname).toString()
    process() { ant.move(file: fromFile, tofile: toFile) }
  }
  println "Deleted article $uri"
}

println "Deleted ${opt.arguments().size()} article(s)"
