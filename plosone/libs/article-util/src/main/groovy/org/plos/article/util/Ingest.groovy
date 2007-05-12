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

import org.apache.commons.lang.text.StrMatcher
import org.apache.commons.lang.text.StrTokenizer
import org.apache.commons.configuration.CompositeConfiguration
import org.apache.commons.configuration.XMLConfiguration

import org.plos.article.util.Zip // Groovy needs this for some inexplicable reason

import org.topazproject.configuration.ConfigurationStore
import org.topazproject.authentication.UnProtectedService
import org.topazproject.authentication.PasswordProtectedService
import org.topazproject.mulgara.itql.ItqlHelper
import org.topazproject.fedora.client.APIMStubFactory
import org.topazproject.fedora.client.Uploader
import org.topazproject.fedoragsearch.service.FgsOperations
import org.topazproject.fedoragsearch.service.FgsOperationsServiceLocator

def cli = new CliBuilder(usage: 'Ingest [-M mulgara:port] [-F fedora:port] [-S search:port] zip')
cli.h(longOpt:'help', "help (this message)")
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')

// Fix maven java:exec command line parsing
if (args[0] == null) args = [ ]
if (args != null && args.length == 1)
  args = new StrTokenizer(args[0], StrMatcher.trimMatcher(), StrMatcher.quoteMatcher()).tokenArray

// Display help if requested
def opt = cli.parse(args); if (opt.h) { cli.usage(); return }

// Must initialize configuration before anybody tries to use it (at class load time)
ConfigurationStore.instance.loadConfiguration(getClass().getResource("/config.xml"))
CONF = new CompositeConfiguration()
if (opt.c)
  CONF.addConfiguration(new XMLConfiguration(opt.c))
CONF.addConfiguration(ConfigurationStore.instance.configuration)

def itql     = new ItqlHelper(URI.create(CONF.getString("topaz.services.itql.uri")))
def apim     = APIMStubFactory.create(new PasswordProtectedService(
                                  CONF.getString("topaz.services.fedora.uri"),
                                  CONF.getString("topaz.services.fedora.userName"),
                                  CONF.getString("topaz.services.fedora.password")))
def uploader = new Uploader(new PasswordProtectedService(
                                  CONF.getString("topaz.services.fedoraUploader.uri"),
                                  CONF.getString("topaz.services.fedoraUploader.userName"),
                                  CONF.getString("topaz.services.fedoraUploader.password")))

// Build list of lucene search servers
def ops      = CONF.getList("topaz.fedoragsearch.urls.url")
opsSet       = new FgsOperations[ops.size()] // typed arrays in groovy cannot be initialized
def i        = 0
ops.each() { opsSet[i] = new FgsOperationsServiceLocator().getOperations(new URL(it)); i++ }

// Ingest files
def ingester = new Ingester(itql, apim, uploader, opsSet)

opt.arguments().each() {
  def zip = new Zip.FileZip(it)
  ingester.ingest(zip)
}
