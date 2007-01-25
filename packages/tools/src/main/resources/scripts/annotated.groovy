/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.Answer;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

// Constants
DEFAULT_HOST = "localhost"
DEFAULT_PORT = 9091
DEFAULT_LOC = "/mulgara-service/services/ItqlBeanService"
PREFS       = "<local:///topazproject#filter:model=preferences>"
LIMIT       = 50000
MAX         = 10000000
EMAIL       = 'alertsEmailAddress'
JOURNALS    = 'alertsJournals'
CATEGORIES  = 'alertsCategories'

def cli = new CliBuilder(usage: 'annotated --start <date> --end <date>')
cli.h(longOpt:'help', 'usage information')
cli.v(longOpt:'verbose', 'turn on verbose mode')
cli.s(args:1, 'Date to start with')
cli.e(args:1, 'Date to end with')
cli.U(args:1, 'mulgara uri')
cli.H(args:1, 'mulgara host')
cli.P(args:1, 'mulgara port')

if (args.length == 1 && args[0].indexOf(' ') > 0)
  args = new StrTokenizer(args[0], StrMatcher.trimMatcher(), StrMatcher.quoteMatcher())
                  .getTokenArray()
def opt = cli.parse(args)
if (!opt) return
if (opt.h) { cli.usage(); return }
def host = (opt.H) ? opt.H : DEFAULT_HOST
def port = (opt.P) ? opt.P : DEFAULT_PORT
def uri  = (opt.U) ? opt.U : "http://${host}:${port}${DEFAULT_LOC}"
def verbose = opt.v
if (verbose)
  println "Mulgara URI: $uri"

// Globals
RI_MODEL='<local:///topazproject#ri>'
STR_MODEL='<local:///topazproject#str>'

itql = new ItqlHelper(new URI(uri))
def aliases = ItqlHelper.getDefaultAliases()
aliases['a'] = 'http://www.w3.org/2000/10/annotation-ns#'

restrict = ""
if (opt.s)
  restrict += " and \$created <topaz:ge> '${opt.s}' in ${STR_MODEL}"
if (opt.e)
  restrict += " and \$created <topaz:le> '${opt.e}' in ${STR_MODEL}"

query = """
  select \$article
    subquery(select \$article \$title from ${RI_MODEL}
             where \$article <dc:subject> \$title)
    count(select \$ann from ${RI_MODEL}
          where \$ann <rdf:type> <a:Annotation>
            and \$ann <a:annotates> \$article
            and \$ann <a:created> \$created
            ${restrict})
    from ${RI_MODEL}
    where \$s <rdf:type> <a:Annotation>
      and \$s <a:annotates> \$article
      and \$s <a:created> \$created
      ${restrict}
"""

if (verbose)
  println "Query: $query"

def results = itql.doQuery(query + ";", aliases);
if (verbose)
  println "Results: $results"

println "article, title, count"
def ans = new XmlSlurper().parseText(results)
ans.query[0].solution.each() {
  println """\"${it.article.'@resource'}","${it.k0.solution.title.text()}",${it.k1}"""
}
