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
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.owl.OwlClass;
import org.topazproject.otm.owl.ObjectProperty;
import org.topazproject.otm.owl.Metadata;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import jline.ConsoleReader;
import jline.History;

// Constants
MULGARA_BASE = "localhost:9091"
MULGARA_LOC = "/mulgara-service/services/ItqlBeanService"
csv = "csv" // In case somebody runs %mode = csv instead of %mode = "csv"
table = "table reduce quote" // Allows %mode = table

// Parse command line
def cli = new CliBuilder(usage: 'runitql [-M mulgarahost:port] [-f script] [-ipvN]')
cli.h(longOpt:'help', 'usage information')
cli.v(longOpt:'verbose', 'turn on verbose mode')
cli.e(longOpt:'echo', 'echo script file when running')
cli.M(args:1, 'Mulgara host:port')
cli.f(args:1, 'script file')
cli.p(longOpt:'prompt', 'show the prompt even for a script file')
cli.N(longOpt:'noprettyprint', 'Do not pretty-print results')
cli.i(longOpt:'runinit', 'Run ~/.runitql even if running a script')
cli.m(args:1, 'mode')

// Fix crap with maven sometimes passing args of [ null ] (an array of one null)
if (args[0] == null)
  args = [ ]

/* If this is run from maven, re-parse arguments
 * i.e. cd head/topazproject/tools/runscripts; mvn -e -o -Prungroovy exec:java -Dargs="script args"
 */
if (args != null && args.length == 1 && args[0] != null)
  args = new StrTokenizer(args[0], StrMatcher.trimMatcher(), StrMatcher.quoteMatcher())
                  .getTokenArray()

def opt = cli.parse(args)
if (!opt) return
if (opt.h) { cli.usage(); return }
def file = (opt.f) ? new File(opt.f).newInputStream() : System.in
bPrompt = (opt.p || !opt.f)
bInit   = (opt.i || !opt.f)
pp = !opt.N
mode = opt.m
echo = opt.v || opt.e || !opt.f
def writer = echo ? new OutputStreamWriter(System.out) : new StringWriter()
def mulgaraBase = (opt.M) ? opt.M : MULGARA_BASE
def mulgaraUri  = "http://${mulgaraBase}${MULGARA_LOC}"
verbose = opt.v
if (verbose) {
  println "Mulgara URI: $mulgaraUri"
}

// Globals
itql = new ItqlHelper(new URI(mulgaraUri))
aliases = itql.getDefaultAliases()

metamodel = "local:///topazproject#metadata"

factory = new SessionFactory()
factory.setTripleStore(new ItqlStore(URI.create(mulgaraUri)))
factory.addModel(new ModelConfig("metadata", URI.create(metamodel), null))
factory.preload(OwlClass.class)
factory.preload(ObjectProperty.class)
session = factory.openSession()

help = new HashMap()
help[null] = '''All commands are sent to mulgara once a semicolon (;) is found except for
lines starting with #, . or %. Additional help is available via .help <topic>.

These are interpreted as follows:
  # - Comment lines
  % - Remainder of line executed as groovy. See ".help variables"
  . - Runs special commands. See ".help cmds"

Availalbe topcis:
  variables cmds .alias .meta init'''
help["cmds"] = '''The following commands are supported: .alias, .meta
Run ".help .<cmd>" for help with a specific command'''
help[".alias"] = """.alias [load|list|save|set alias uri]
  load - loads any aliases defined in <$metamodel>
  save - saves all currently defined aliases into <$metamodel>
  list - lists currently active aliases (but doesn't load them)
  set alias uri - adds an alias to the interpreter (but doesn't save it)"""
help[".meta"]  = """.meta [classes|show type]
  classes - Shows all the classes defined in <$metamodel>
  show type - Shows information about a specific OWL class
  load jar-files|dirs ... - Load owl metadata for supplied classes"""
help["variables"] = '''Variables can be used for a number of things:
  - Controlling features of the interpreter (see list of variables below)
  - As place holders in itql. 
    e.g. itql> %meta = "<''' + metamodel + '''>"
         itql> select $s $p $o from ${meta} where $s $p $o;

If a %-character is the first character of the line, the rest of the line is 
sent to the groovy interpreter. Thus, %x=3 sets x to 3. %foo=bar is an error 
because bar is not in quotes. '%println mode' will printout the value of the
variable mode.

Special variables:
  mode (str) - Sets dispaly output. General format options are: xml, csv, table
               You can also append sub-modes: quote reduce
               These quote literals and uris appropriately and/or reduce uris 
               via aliases for easier viewing. eg. %mode="table quote reduce"
  verbose    - If set to true, will output more information'''
help["init"] = "On startup, ~/.runitql is loaded."

// Various functions

def showHelp(args) {
  def desc = help[args == null ? null : args[0]]
  println (desc != null ? desc : "Invalid topic: ${args[0]}")
}

def meta(args) {
  switch(args[0]) {
  case 'classes': showClasses();      break
  case 'show':    showClass(args[1]); break
  case 'load':    Metadata.addClasses(args[1..-1]); break
  }
}

def showClasses() {
  def q = '''select $class $model from <''' + metamodel + '''>
              where $class <rdf:type> <owl:Class> and $class <topaz:inModel> $model;'''
  showTable(new XmlSlurper().parseText(itql.doQuery(q, aliases)))
}

def showClass(cls) {
  if (cls.startsWith("<")) cls = cls[1..-1]
  if (cls.endsWith(">")) cls = cls[0..-2]
  def desc = ''
  def tx = session.beginTransaction()
  try {
    desc = reduce(session.get(OwlClass.class, expand(cls)).toString())
  } finally {
    tx.commit()
  }

  println "Full URI: <${expand(cls)}>"
  println "Details: ${desc}"
  def q = """
    select \$prop
           subquery(select \$type from <$metamodel> 
                     where \$prop <rdfs:range> \$type)
      from <$metamodel>
     where (    \$prop <rdfs:domain> \$u
            and \$u \$x \$c
            and \$s <rdf:first> \$class
            and (trans(\$c <rdf:rest> \$s) or \$c <rdf:rest> \$s or \$u \$x \$s))
       and (    \$class <mulgara:is> <$cls>
            or  <$cls> <rdfs:subClassOf> \$class
            or  trans(<$cls> <rdfs:subClassOf> \$class));
"""
  def ans = new XmlSlurper().parseText(itql.doQuery(q, aliases))
  showTable(ans)
}

def alias(args) {
  switch(args[0]) {
  case 'load': loadAliases(); break
  case 'list': showAliases(); break
  case 'set' : aliases[args[1]] = URI.create(args[2]).toString(); break
  case 'save': println "Unimplemented"; break
  case 'help': println ".alias [load|list|save|set alias uri]"; break
  }
}

def loadAliases() {
  def query = """
    select \$uri \$alias 
      from <$metamodel> 
     where \$uri <http://rdf.topazproject.org/RDF/hasAlias> \$alias;
"""
  def result = itql.doQuery("$query;", aliases)
  def answer = new XmlSlurper().parseText(result)
  answer.query[0].solution.each() { sol ->
    aliases[ sol.alias.toString() ] = sol.uri."@resource".toString()
  }
}

def showAliases() {
  def len = new ArrayList(aliases.keySet())*.size().max()
  aliases.keySet().sort().each() { printf "%${len}s: %s\n", it, aliases[it] }
}

def reduceUri(uri) {
  for (alias in aliases) {
    if (uri == alias.value) return uri
    def val = uri.replace(alias.value, alias.key + ":")
    if (val != uri) return val
  }
  return uri
}

// Show the prompt. Is sometimes turned off if not running interactively
def prompt() {
  if (bPrompt)
    print "itql> "
}

def reduce(s) {
  for (alias in aliases)
    s = s.replaceAll(alias.value, alias.key + ":")
  return s
}

def expand(s) {
  for (alias in aliases)
    s = s.replaceAll(alias.key + ":", alias.value)
  return s
}

def showResults(result) {
  def ans = new XmlSlurper().parseText(result)
  if (ans.query.message.text()) {
    if (echo) println ans.query.message
  } else if (mode && mode =~ "csv" && ans.query[0].solution) {
    showCsv(ans)
  } else if (mode && mode =~ "tab" && ans.query[0].solution) {
    showTable(ans)
  } else {
    if (pp)
      new XmlNodePrinter().print(new XmlParser().parseText(result))
    else
      print "$result\n"
  }
}

def showCsv(ans) {
  def vars = [ ]
  ans.query[0].variables.children().each() { vars.add(it.name()) }
  ans.query[0].solution.each() { sol ->
    def vals = [ ]
    vars.each() { var ->
      def val = sol."$var"."@resource".toString()
      if (val) {
        if (mode =~ "red")   val = reduceUri(val)
        if (mode =~ "quote") val = "<$val>"
      } else {
        val = sol."$var"."@blank-node".toString()
        if (!val) {
          val = sol."$var".toString()
          if (mode =~ "quote")
            val = '"' + val.replace('"', '""') + '"'
        }
      }
      vals.add(val)
    }
    println vals.toString()[1..-2]
  }
}

def getVar(sol, var) {
  def val = sol."$var"."@resource".toString()
  if (val) {
    if (mode =~ "red")   val = reduceUri(val)
    if (mode =~ "quote") val = "<$val>"
  } else {
    val = sol."$var"."@blank-node".toString()
    if (!val) {
      val = sol."$var".toString()
      if (val) {
        if (mode =~ "quote")
          val = "'" + val.replace("'", "\\'") + "'"
      } else {
        // See if there is a subquery
        def subsol = sol."$var".solution
        subsol.childNodes().each() {
          val = it.attributes()["resource"]
        }
        val = sol."$var".solution.type."@resource".toString()
      }
    }
  }
  return val
}

def showTable(ans) {
  // TODO: refactor... this should be recursive (but dealing with different types)
  def vars = [ ]
  def hdrs = [ ]
  ans.query[0].variables.children().each() { vars.add(it.name()) }
  vars.each() { hdrs.add(it) }
  def lengths = vars*.size()
  def data = [ ]
  ans.query[0].solution.each() { sol ->
    def vals = [ ]
    def col = 0
    vars.each() { var ->
      def val = sol."$var"."@resource".toString()
      if (val) {
        if (mode =~ "red")   val = reduceUri(val)
        if (mode =~ "quote") val = "<$val>"
      } else {
        val = sol."$var"."@blank-node".toString()
        if (!val) {
          val = sol."$var".toString()
          if (val) {
            if (mode =~ "quote")
              val = "'" + val.replace("'", "\\'") + "'"
          } else {
            // See if there is a subquery
            def subsol = sol."$var".solution
            def subvals = [ ]
            subsol.childNodes().each() {
              // TODO: Support multiple variables in a subquery
              hdrs[col] = it.name.toString() + "(s)"
              val = it.attributes()["resource"]
              if (val) {
                if (mode =~ "red")   val = reduceUri(val)
                if (mode =~ "quote") val = "<$val>"
              } else {
                // TODO: support blank nodes?
                val = it.value
                if (mode =~ "quote") 
                  val = "'" + val.replace("'", "\\'") + "'"
              }
              subvals.add(val)
            }
            val = (subvals ? subvals.toString()[1..-2] : "")
          }
        }
      }
      vals.add(val)
      lengths[col] = [ lengths[col++], val.size() ].max()
    }
    data.add(vals)
  }
  def seps = [ ]
  lengths.each() { seps += "-"*it }
  ([ hdrs, seps ] + data).each() { row ->
    def col = 0
    def line = ""
    row.each() { val ->
      def st = val.toString()
      line += st + " "*(lengths[col++] - st.size() + 1)
    }
    println line.trim()
  }
}

/**
 * Expand ${} variables in a query string.
 *
 * This allows things like:
 * <pre>
 *   @model = "<local:///topazproject#mymodel>"
 *   select $s $p $o from ${model} where $s $p $o;
 * </pre>
 */
def expandVars(query) {
  def result = query
  (query =~ /\$\{([^}]*)}/).each() { st, var ->
    result = result.replace(st, evaluate(var))
  }
  return result
}

def execute(query) {
  try {
    query = expandVars(query)
    if (verbose)
      print "$query\n"
    result = itql.doQuery("$query;", aliases)
    showResults result
  } catch (Throwable e) {
    print "Error running query:\n$e\n"
    if (verbose)
      e.printStackTrace()
  }
}

/** 
 * Ask groovy to evaluate some string in our context
 *
 * Be very careful on refactoring that global variables (really instance variables)
 * are available to evaluate() below or things may get very messy
 */
def eval(s) {
  try {
    this.expand = { expand(it) }
    this.reduce = { reduce(it) }
    this.expandVars = { expandVars(it) }
    evaluate(s)
  } catch (Throwable e) {
    print "Error evaluating groovy: %$s\n:$e\n"
    if (verbose)
      e.printStackTrace()
  }
}

// Handle special commands that start with .
def handleCmd(s) {
  try {
    def args = s.split(/ +/)
    def cmd = args[0]
    args = (args.size() > 1 ? args[1..-1] : null)
    // Look for a matching command (allow abbreviations)
    if      ("alias".startsWith(cmd)) { alias(args) }
    else if ("meta".startsWith(cmd))  { meta(args) }
    else if ("help".startsWith(cmd))  { showHelp(args) }
    else { println "Unknown command: .$s" }
  } catch (Throwable e) {
    println "Error running command: .$s"
    if (verbose)
      e.printStackTrace()
  }
}

// Queries can exist on multiple lines, so need to stash previous partial queries
query = ""

def processLine(line, console, showPrompt) {
  if (line != "" && line[0] == '#') 
    line = '' // strip comments
  else if (line != "" && (line[0] == '%' || line[0] == '@')) { // @ is for backward compatibility
    eval(line.substring(1))
    console?.getHistory()?.addToHistory(line)
    line = '' // strip expression
  } else if (line != "" && line[0] == '.') {
    handleCmd(line.substring(1))
    console?.getHistory()?.addToHistory(line)
    line = '' // strip expression
  }    

  while (line.indexOf(';') != -1) {
    pos = line.indexOf(';')
    query += " " + line[0..pos-1].trim()
    if (query.trim() != "") {
      execute query.trim()
      console?.getHistory()?.addToHistory(query.trim() + ";")
    }
    query = ""
    line = line.substring(pos+1)
  }
  if (line.trim() != "")
    query += " " + line.trim()

  if (showPrompt)
    prompt()
}

// Read init file if it exists
if (bInit) {
  def initfile = new File(new File(System.getProperty("user.home")), ".runitql")
  if (initfile.exists())
    initfile.eachLine { line -> processLine(line, null, false) }
}

// Show the initial prompt
if (bPrompt)
  println 'Itql Interpreter. Run ".help" for more information.'
prompt()

// Use jline for some attempt at readline functionality
def cr = new ConsoleReader(file, writer)
cr.setDefaultPrompt("itql> ")
cr.setUseHistory(false)
try {
  histfile = new File(System.getProperty("user.home") + File.pathSeparator + ".runitql_history")
  cr.setHistory(new History(histfile))
} catch (IOException e) {
  println "Error loading history: $e"
}

//file.eachLine { line -> // Old way (without jline)
while ((line = cr.readLine()) != null) { // Loop over lines with jline
  processLine(line, cr, true)
}

print "\n"
