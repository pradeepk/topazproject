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
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import jline.ConsoleReader;

// Constants
MULGARA_BASE = "localhost:9091"
MULGARA_LOC = "/mulgara-service/services/ItqlBeanService"

def cli = new CliBuilder(usage: 'runitql [-M mulgarahost:port] [-f script] [-pvN]')
cli.h(longOpt:'help', 'usage information')
cli.v(longOpt:'verbose', 'turn on verbose mode')
cli.M(args:1, 'Mulgara host:port')
cli.f(args:1, 'script file')
cli.p(longOpt:'prompt', 'show the prompt even for a script file')
cli.N(longOpt:'noprettyprint', 'Do not pretty-print results')

/* If this is run from maven, re-parse arguments
 * i.e. cd head/topazproject/tools/runscripts; mvn -e -o -Prungroovy exec:java -Dargs="script args"
 */
if (args.length == 1 && args[0].indexOf(' ') > 0)
  args = new StrTokenizer(args[0], StrMatcher.trimMatcher(), StrMatcher.quoteMatcher())
                  .getTokenArray()

def opt = cli.parse(args)
if (!opt) return
if (opt.h) { cli.usage(); return }
def file = (opt.f) ? new File(opt.f).newInputStream() : System.in
bPrompt = (opt.p || !opt.f)
bPrettyPrint = !opt.N
def mulgaraBase = (opt.M) ? opt.M : MULGARA_BASE
def mulgaraUri  = "http://${mulgaraBase}${MULGARA_LOC}"
verbose = opt.v
if (verbose) {
  println "Mulgara URI: $mulgaraUri"
}

itql = new ItqlHelper(new URI(mulgaraUri))

def prompt() {
  if (bPrompt)
    print "itql> " 
}

def execute(query) {
  if (verbose)
    print "$query\n"
  try {
    result = itql.doQuery("$query;", null)
    if (bPrettyPrint)
      new XmlNodePrinter().print(new XmlParser().parseText(result))
    else
      print "$result\n"
  } catch (Throwable e) {
    print "Error running query:\n$e\n"
    if (verbose)
      e.printStackTrace()
  }
}

// Show the initial prompt
prompt()

// Queries can exist on multiple lines, so need to stash previous partial queries
String query = ""

// Use jline for some attempt at readline functionality
cr = new ConsoleReader(file, new OutputStreamWriter(System.out))
cr.setDefaultPrompt("itql> ")

while ((line = cr.readLine()) != null) { // Loop over lines with jline
//file.eachLine { line -> // Old way (without jline)
  if (line != "" && line[0] == '#') line = '' // strip comments
  while (line.indexOf(';') != -1) {
    pos = line.indexOf(';')
    query += " " + line[0..pos-1].trim()
    if (query.trim() != "")
      execute query.trim()
    query = ""
    line = line.substring(pos+1)
  }
  if (line.trim() != "")
    query += line.trim()
  prompt()
}

print "\n"
