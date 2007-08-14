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

import org.testng.annotations.BeforeClass

/**
 * PLoS Integration Tests using WebTest.
 *
 * All WebTests should extend this class.
 */
class PlosWebTest {

  def ant = new AntBuilder()
  def props
  def config

  /**
   * Constructor sets up WebTest environment.
   */
  public PlosWebTest() {
    testSetup()
  }

  /**
   * Standard Ant pre-test setup.
   */
  @BeforeClass
  testSetup() {

    try {
      initProps()
      initConfig()
      prepare()
    } catch (Exception e) {
      e.printStackTrace();
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

  // prepare a config based on plos.webtest.properties
  def initConfig () {

    config = [:]
    def prefix = 'plos.webtest.'
    props.keySet().each { name ->
      if (name.startsWith(prefix)) config.put(name - prefix, props[name])
    }
  }

  // prepare the ant taskdef, classpath and filesystem for reporting
  void prepare() {

    def webtestTaskdefURL = this.getClass().getResource('/plos.webtest.taskdef')
    println 'webtestTaskdefURL: ' + webtestTaskdefURL

    ant.taskdef(file: webtestTaskdefURL.getFile())
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