/* $$HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/webapp/src/main/java/org/#$$
 * $$Id: ManageVirtualJournalsAction.java 3395 2007-08-10 05:40:14Z jsuttor $$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plosone.it

import junit.framework.TestCase;

/**
 * PLoS Integration Tests using WebTest.
 *
 * Main entry point for testing.
 */
class PlosIT extends TestCase {

  def ant = new AntBuilder()
  def props
  def configMap

  public PlosIT() {

    initProps()
    initConfigMap()
    prepare()
  }

  public void testSuite() throws Exception {
    try {
      // TODO: scan classpath for *IT test classes, for now, they must be put in manually
      //
      //def scanner = ant.fileScanner {
      //  fileset(includes: '**/*IT.groovy')
      //}

      def testsIT = ['org.plosone.it.HomeActionIT']
      testsIT.each {testIT ->
        println 'testIT: ' + testIT
        println '  config: ' + configMap
        def test = this.getClass().getClassLoader().loadClass(testIT).newInstance()
        test.ant = ant
        test.props = props
        test.config = configMap
        test.testSuite()
        }

      style()
    } catch (Exception e) {
      e.printStackTrace()
      throw(e)
    }
  }

  void initProps () {

    def webtestPropertiesURL = this.getClass().getResource('/plos.webtest.properties')
    println 'webtestPropertiesURL: ' + webtestPropertiesURL

    ant.property(file: webtestPropertiesURL.getFile())

    ant.property(environment: 'env')
    props = ant.antProject.properties
  }

  // prepare a configmap based on plos.webtest.properties
  def initConfigMap () {

    configMap = [:]
    def prefix = 'plos.webtest.'
    props.keySet().each { name ->
      if (name.startsWith(prefix)) configMap.put(name - prefix, props[name])
    }
  }

  // prepare the ant taskdef, classpath and filesystem for reporting
  void prepare() {

    try {
      def webtestTaskdefURL = this.getClass().getResource('/plos.webtest.taskdef')
      println 'webtestTaskdefURL: ' + webtestTaskdefURL

      ant.taskdef(file: webtestTaskdefURL.getFile())
    } catch (Exception e) {
      System.err.println 'Exception: ' + e
    }
  }

  def style() {

    def webtestStyleURL = this.getClass().getResource('/WebTestReport.xsl')
    println 'webtestStyleURL: ' + webtestStyleURL

    ant.xslt(
      basedir:    props.'plos.webtest.resultpath',
      destdir:    props.'plos.webtest.resultpath',
      includes:   props.'plos.webtest.resultfile',
      extension:  '.html',
      style:      webtestStyleURL.getFile()) {
        param(name:'reporttime',     expression: new Date().toString())
        param(name:'title',          expression: "The ${props.projectName} Project")
        param(name:'resources.dir',  expression: new File(props.'plos.webtest.resultpath' + '/resources').toURI())
    }

    def reportHtml = props.'plos.webtest.resultpath' + '/' + props.'plos.webtest.resultfile' - '.xml' + '.html'
    def filename = new File(reportHtml).canonicalPath
    println "Report generated in: $filename"
  }
}