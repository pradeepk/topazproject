/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/article-util/src/main/groovy/#$
 * $Id: Delete.groovy 2686 2007-05-15 08:22:38Z ebrown $
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
import org.plos.article.util.ToolHelper
import org.plos.models.Article
import org.plos.models.Category
import org.topazproject.otm.SessionFactory
import org.topazproject.otm.ModelConfig
import org.topazproject.otm.stores.ItqlStore
import org.topazproject.xml.transform.cache.CachedSource
import groovy.xml.StreamingMarkupBuilder

// Use ToolHelper (currently in wrong place -- article-util) to patch args
args = ToolHelper.fixArgs(args)

def cli = new CliBuilder(usage: 'migration [-c config-overrides.xml] article-uris ...')
cli.h(longOpt:'help', "help (this message)")
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')
cli.a(args:1, 'article (i.e. doi:10.1371/journal.pone.nnnnnnn)')

// Display help if requested
def opt = cli.parse(args); if (opt.h) { cli.usage(); return }

// Load configuration before ArticleUtil is instantiated
CONF = ToolHelper.loadConfiguration(opt.c)

// Get the article doi from the command line
def doi = opt.a

// Create the slurper object and add entity resolver (so reading articles are reasonably fast)
def slurper = new XmlSlurper()
slurper.setEntityResolver(CachedSource.getResolver())

def fedoraUri = CONF.getString("topaz.services.fedora.base-url") + "get"
def articleUrl = "$fedoraUri/doi:${URLEncoder.encode(doi.substring(9))}/XML"

// Ask slurper to read article from fedora
def slurpedArticle = slurper.parse(new URL(articleUrl).getContent())
def articleMeta = slurpedArticle.front.'article-meta'
def journalMeta = slurpedArticle.front.'journal-meta'

// Setup OTM
def factory = new SessionFactory();
def itql = new ItqlStore(URI.create(CONF.getString("topaz.services.itql.uri")))
def ri = new ModelConfig("ri", URI.create(CONF.getString("topaz.models.articles")), null);
factory.setTripleStore(itql)
factory.addModel(ri);
factory.preload(Article.class);
factory.preload(Category.class);
def session = factory.openSession();
def tx = session.beginTransaction();

// Read article from mulgara
def article = session.get(Article.class, doi)

// Build lists of affiliations and authors (TODO: ordered contributors too?)
Set affiliations = new HashSet()
articleMeta.aff.institution.each() { aff -> affiliations.add(aff.toString()) }
List authors = new ArrayList()
articleMeta.'contrib-group'.contrib.each() {
  def name = ((it.name.@'name-style' == "eastern") 
                   ? "${it.name.surname} ${it.name.'given-names'}"
                   : "${it.name.'given-names'} ${it.name.surname}")
  switch(it.@'contrib-type') {
  case 'author': authors += name.toString(); break
  }
}

// Update article
article.articleType        = slurpedArticle.'@article-type'
article.volume             = articleMeta.volume.toString().toInteger()
article.issue              = articleMeta.issue.toString().toInteger()
article.journalTitle       = journalMeta.'journal-title'
article.publisherName      = journalMeta.'publisher'.'publisher-name'
article.copyrightStatement = articleMeta.'copyright-statement'
article.copyrightYear      = articleMeta.'copyright-year'.toString().toInteger()
article.pageCount          = articleMeta.counts.'page-count'.'@count'.toString().toInteger()
article.publisherName      = journalMeta.publisher.'publisher-name'
article.affiliations       = affiliations
article.orderedAuthors     = authors
article.body               = new StreamingMarkupBuilder().bind { mkp.yield(slurpedArticle.body) }

session.saveOrUpdate(article)
tx.commit()
session.close()

// TODO: Deal with errors (i.e. catch exceptions)
