/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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

import org.apache.commons.lang.text.StrMatcher
import org.apache.commons.lang.text.StrTokenizer

import org.topazproject.ambra.configuration.ConfigurationStore;
import org.apache.commons.configuration.ConfigurationUtils

DEFAULT_CONFIG_URL = "file:///etc/topaz/ambra.xml"

// Parse command line
def cli = new CliBuilder(usage: 'ambraConfiguration [-f <configURL>]')
cli.h(longOpt:'help', 'usage information')
cli.f(args:1, 'Config URL [file:///etc/topaz/ambra.xml]')

if (args.size() > 0 && args[0] == null) args = [ ]
if (args != null && args.length == 1)
  args = new StrTokenizer(args[0], StrMatcher.trimMatcher(), StrMatcher.quoteMatcher()).tokenArray

def opt = cli.parse(args)
if (!opt) return
if (opt.h) { cli.usage(); return }
def configURL = (opt.f) ? opt.f : DEFAULT_CONFIG_URL

// Create config instance
def config = ConfigurationStore.getInstance()
config.loadConfiguration(configURL.toURL())

// dump it
println ConfigurationUtils.toString(config.getConfiguration())
