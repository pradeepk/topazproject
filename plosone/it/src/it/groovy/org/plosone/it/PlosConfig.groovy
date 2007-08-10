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
 * PLoS Configuration for WebTest.
 *
 * All WebTests should extend PlosConfig.
 */
class PlosConfig extends TestCase {

  def props
  def ant = new AntBuilder()

  public webtestHome
  public Map configMap

  PlosConfig() {

    initProps()
    initConfigMap()
    prepare()
  }

  void initProps () {
    ant.property(resource: 'plos.webtest.properties')
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

      println 'ant.taskdef OK'
    } catch (Exception e) {
      System.err.println 'Exception: ' + e
    }
  }
}