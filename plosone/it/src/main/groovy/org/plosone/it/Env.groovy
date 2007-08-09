/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/article-util/src/main/groovy/#$
 * $Id: Delete.groovy 2686 2007-05-15 08:22:38Z ebrown $
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plosone.it;
import org.apache.tools.ant.taskdefs.Antlib;

public class Env {
  private AntBuilder ant = new AntBuilder();
  private String install;
  private String data;
  private String opts;
  private static Env active = null;

  public static boolean stopOnExit = true;

  static {
    def shutdownHook = new Thread( {
        if ((active != null) && stopOnExit)
          active.stop();
    }, "Shutdown services")
    Runtime.runtime.addShutdownHook(shutdownHook)
  }


 /**
  *  Construct a test environment.
  *
  *  @param install the installation directory
  *  @param data the mvn dependency spec for data (eg. 'org.plosone:plosone-it-data:0.7')
  */
  public Env(String install, String data) {
    File f = new File(install);
    this.install = install = f.absoluteFile.canonicalPath;
    this.data = data;
    opts  = ' -DECQS_INSTALL_DIR=' + install + 
            ' -DFEDORA_INSTALL_DIR=' + install + 
            ' -DDEST_DIR=' + install

    String url = '/net/sf/antcontrib/antlib.xml'
    url = this.getClass().getResource(url)
    Antlib.createAntlib(ant.antProject, url.toURL(), null).execute()
  }

  /**
   * Stop all services and clean the install directory
   */
  public void clean() {
    stop();
    ant.delete(dir:install)
  }

  /**
   * Stop all services
   */ 
  public void stop() {
    for (task in ['plosone-stop', 'mulgara-stop', 'fedora-stop'])
      try { antTask(task) } catch (Throwable t) { } 
    active = null
  }

  /**
   * Install all services. A marker file is placed after the install
   * so that subsequent calls to install can bypass the actual install.
   * 
   */
  public void install() {
    File f = new File(install + '/installed');
    if (f.exists())
      return;

    clean()

    for (task in ['ecqs-install', 'fedora-install', 'mulgara-install', 'plosone-install'])
      antTask(task)
   
    if (data != null)
      load()

    ant.touch(file: install + '/installed')
  }

  /**
   * Restore the data to the same state as a fresh install.
   *
   */
  public void restore() {
    stop()
    // mulgara works off of the data dir. So delete that
    ant.delete(dir: install + "/data/mulgara")
    load()
  }

 /**
  * Start all services. If another environment is currently running, that is stopped first.
  */
  public void start() {
    if (active != null) {
      if (active.install.equals(install))
         return
      active.stop()
    }  
    active = this
    mulgara()
    fedora()
    plosone()
    waitFor('http://localhost:8080/plosone-webapp/')
  }
  
  private void antTask(task) {
    ant.exec(executable: 'mvn') {
      arg(line: 'ant-tasks:' + task + opts)
    }
  }

  private void mulgara() {
    ant.echo 'Starting mulgara'
    ant.forget {
      ant.exec(executable: 'mvn') {
        arg(line: '-f ' + pom() + ' ant-tasks:mulgara-start -DDEST_DIR=' + install
         + ' -Dtopaz.mulgara.databaseDir=' + install + '/data/mulgara -Dlog4j.configuration='
         + mulgaraLog4j())
      }
      ant.echo 'Mulgara stopped'
    }
  }

  private void fedora() {
    ant.exec(executable: 'mvn') {
      arg(line: 'ant-tasks:fedora-start -DSPAWN=true' + opts)
    }
  }

  private void plosone() {
    ant.echo 'Starting plosone'
    ant.forget {
      ant.exec(executable: 'mvn') {
        arg(line: '-f ' + pom() + ' ant-tasks:plosone-start -DDEST_DIR=' + install
         + ' -Dorg.plos.configuration.overrides=defaults-dev.xml -Dlog4j.configuration=' 
         + plosoneLog4j())
      }
      ant.echo 'PlosOne Stopped'
    }
  }

  private String waitFor(String uri) {
    Throwable saved = null;
    for (i in 1..120) {
      try {return uri.toURL().getText()} catch (Throwable e)  {saved = e}
      ant.echo i + ' Waiting for ' + uri + ' ...'
      sleep 1000
    }
    throw saved
  }
  
  private String pom () {
    String p = pom(new File(System.properties.'user.dir'))
    if (p == null)
      p = pom(new File(install))
    if (p == null)
      throw new FileNotFoundException('a pom.xml was not found')
    return p
  }

  private String pom(File dir) {
    if (dir == null)
      return null;
    File f = new File(dir, 'pom.xml');
    if (f.exists())
      return f.absoluteFile.canonicalPath;
    return pom(dir.parentFile)
  }


  private void load() {
    ant.exec(executable: 'mvn') {
      arg(line: '-f ' + pom() + ' ant-tasks:tgz-explode -Dlocation=' + install 
        + ' -Dtype=tgz -Ddependencies=' + data)
    }
    File d = new File(install);
    d = new File(install, 'data')
    if (!d.exists())
      throw new FileNotFoundException("exploded data file '" + data 
             + "' does not contain the top level directory 'data'")
    File m = new File(d, 'mulgara')
    if (!m.exists())
      throw new FileNotFoundException("exploded data file '" + data 
             + "' does not contain the top level directory 'data/mulgara'")
    File f = new File(d, 'fedora')
    if (!f.exists())
      throw new FileNotFoundException("exploded data file '" + data 
             + "' does not contain the top level directory 'data/fedora'")

    ant.exec(executable: 'mvn') {
      arg(line: 'ant-tasks:fedora-rebuild ' + opts
       + ' -DFEDORA_REBUILD_STDIN=' + rebuildInput()
       + ' -DFEDORA_REBUILD_FROM=' + install + '/data/fedora')
    }
  }

  private String rebuildInput() {
     return resource('/fedora-rebuild-input')
  }
  
  private String plosoneLog4j() {
     return 'file://' + resource('/plosoneLog4j.xml')
  }
  
  private String mulgaraLog4j() {
     return 'file://' + resource('/mulgaraLog4j.xml')
  }

  private String resource(String name) {
     String input = install + name
     def out = new BufferedOutputStream(new FileOutputStream(input))
     out << getClass().getResourceAsStream(name)
     out.close()
     return input
  }

}
