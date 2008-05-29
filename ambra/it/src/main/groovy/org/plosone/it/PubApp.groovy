/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/article-util/src/main/groovy/#$
 * $Id: Delete.groovy 2686 2007-05-15 08:22:38Z ebrown $
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

package org.plosone.it

/**
 * PubApp service manager.
 *
 * @author Pradeep Krishnan
 */
public class PubApp extends Tomcat5x {

  private Map wars = [:]

  /**
   * Create a PubApp service instance.
   *
   * @param installDir root directory for this service
   */
  public PubApp(String installDir) {
    this(Tomcat5x.TOMCAT_URL, installDir)
  }

  /**
   * Create a PubApp service instance.
   *
   * @param tomcatUrl the url to download the tomcat service zip file from
   * @param installDir root directory for this service
   */
  public PubApp(String tomcatUrl, String installDir) {
    this(tomcatUrl, installDir, '8080', '8005', '-server -Xmx200m')
  }

  /**
   * Create a PubApp service instance.
   *
   * @param tomcatUrl the url to download the tomcat service zip file from
   * @param installDir root directory for this service
   * @param port the http port 
   * @param shutdownPort the port to send shutdown command to
   * @param jvmargs jvm args for tomcat
   */
  public PubApp(String tomcatUrl, String installDir, String port, 
                 String shutdownPort, String jvmargs) {
    super(tomcatUrl, installDir, port, shutdownPort, 
          '-server -Xmx200m -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8085')
    String version = Env.pomVersion()
    wars['/plosone-webapp'] = Env.dependencyPath('org.plosone', 'plosone-webapp', version, 'war')
    wars['/plos-registration'] = Env.dependencyPath('org.plos', 'plos-registration-webapp', 
                                                     version, 'war')
    wars['/ws-search-webapp'] = Env.dependencyPath('org.topazproject.ws', 'ws-search-webapp',
                                                   version, 'war')
    setExtraClasspath([Env.dependencyPath('log4j', 'log4j', '1.2.14', 'jar')])
  }

  /*
   * inherited javadoc
   */
  public void start() {
    super.start(wars)
  }

  /*
   * inherited javadoc
   */
  public void waitFor() {
    waitFor('http://localhost:8080/plosone-webapp/')
  }

  /*
   * inherited javadoc
   */
  public void rebuild() {
   // nothing to do
  }

}