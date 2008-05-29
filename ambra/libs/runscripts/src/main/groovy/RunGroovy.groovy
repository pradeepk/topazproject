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
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

def cli = new CliBuilder(usage: 'rungroovy ...')
cli.h(longOpt:'help', 'usage: rungroovy script.groovy [args]')

// In case being called from maven, re-parse arguments
if (args.size() > 0 && args[0] == null) args = [ ]
if (args != null && args.length == 1)
  args = new StrTokenizer(args[0], StrMatcher.trimMatcher(), StrMatcher.quoteMatcher()).tokenArray

def opt = cli.parse(args)
if (!opt) return
if (opt.h) { cli.usage(); return }

// Parse args to pass to sub-script
args = opt.getArgs().toList()

if (args.size() > 0) {
  def prog = args[0]
  args.remove(0)
  new GroovyShell().run(new File(prog), args)
} else
  new GroovyShell().run(System.in, 'stdin', null)