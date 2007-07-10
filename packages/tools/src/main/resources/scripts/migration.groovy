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
import org.plos.models.Reference
import org.plos.models.Citation
import org.topazproject.otm.SessionFactory
import org.topazproject.otm.ModelConfig
import org.topazproject.otm.stores.ItqlStore
import org.topazproject.xml.transform.cache.CachedSource
import groovy.xml.StreamingMarkupBuilder
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

log = LogFactory.getLog(this.getClass());

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
factory.preload(org.plos.models.Reference.class);
def session = factory.openSession();
def tx = session.beginTransaction();

// Read article from mulgara
def article = session.get(Article.class, doi)

String getName(name) {
  return ((name.@'name-style' == "eastern") 
           ? "${name.surname} ${name.'given-names'}"
           : "${name.'given-names'} ${name.surname}").toString()
}

String tostr(value) {
  String s = value.toString()
  if (s != null && s.size() == 0)
    s = null
  return s
}

def toint(value) {
  try {
    return tostr(value)?.toInteger()
  } catch (Exception e) {
    log.warn("Unexpected $e")
    return null
  }
}

// Build lists of affiliations and authors (TODO: ordered contributors too?)
Set affiliations = new HashSet()
articleMeta.aff.institution.each() { aff -> affiliations.add(aff.toString()) }
List authors = new ArrayList()
articleMeta.'contrib-group'.contrib.each() {
  switch(it.@'contrib-type') {
  case 'author': authors += getName(it.name); break
  }
}

// Update article
article.articleType        = tostr(slurpedArticle.'@article-type')
article.volume             = toint(articleMeta.volume)
article.issue              = toint(articleMeta.issue)
article.journalTitle       = tostr(journalMeta.'journal-title')
article.publisherName      = tostr(journalMeta.'publisher'.'publisher-name')
article.copyrightStatement = tostr(articleMeta.'copyright-statement')
article.copyrightYear      = toint(articleMeta.'copyright-year')
article.pageCount          = toint(articleMeta.counts.'page-count'.'@count')
article.publisherName      = tostr(journalMeta.publisher.'publisher-name')
article.affiliations       = affiliations
article.orderedAuthors     = authors
article.body               = new StreamingMarkupBuilder().bind { mkp.yield(slurpedArticle.body) }

// Handle references
slurpedArticle.back.'ref-list'.ref.each() { src ->
  def ref = new org.plos.models.Reference()
  def cit = ref.citation

  ref.id                = new URI('info:doi/10.1371/reference.' + src.'@id')
  ref.label             = tostr(src.label)
  cit.citationType      = tostr(src.citation.'@citation-type')
  cit.source            = tostr(src.citation.source)
  cit.comment           = tostr(src.citation.comment)
  cit.publisherLocation = tostr(src.citation.'publisher-loc')
  cit.publisherName     = tostr(src.citation.'publiser-name')
  cit.articleTitle      = tostr(src.citation.'article-title')
  cit.setFirstPage(toint(src.citation.fpage))
  cit.setLastPage(toint(src.citation.lpage))
  cit.setVolume(toint(src.citation.volume))
  cit.setYear(toint(src.citation.year))

  // Handle authors and editors of citations
  src.citation.'person-group'.each() { group ->
    group.name.each() { name ->
      switch (group.@'person-group-type') {
      case 'author': cit.authors.add(getName(name)); break
      case 'editor': cit.editors.add(getName(name)); break;
      default: log.warn("Unknown person-group-type: ${group.@'person-group-type'}"); break
      }
    }
  }

  article.references.add(ref)
}

println "Updating mulgara..."
session.saveOrUpdate(article)
tx.commit()
session.close()

// TODO: Deal with errors (i.e. catch exceptions)
