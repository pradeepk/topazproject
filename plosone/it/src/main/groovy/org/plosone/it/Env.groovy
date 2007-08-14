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
   
    load()

    ant.touch(file: install + '/installed')
  }

  /**
   * Restore the data to the same state as a fresh install.
   *
   */
  public void restore() {
    stop()
    ant.delete(dir: install + "/data")
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
    publishingApp()
    waitFor('http://localhost:8080/plosone-webapp/')
    ant.echo 'Publishing app started'
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
    ant.delete(file: install + '/fedora-2.1.1/server/status')
    ant.exec(executable: 'mvn') {
      arg(line: 'ant-tasks:fedora-start -DSPAWN=true' + opts)
    }
  }

  private void publishingApp() {
    ant.echo 'Starting publishing app'
    ant.forget {
      ant.exec(executable: 'mvn') {
        arg(line: '-f ' + pom() + ' ant-tasks:plosone-start -DDEST_DIR=' + install
         + ' -Dorg.plos.configuration.overrides=defaults-dev.xml -Dlog4j.configuration=' 
         + publishingAppLog4j() 
         + ' -Dpub.spring.ingest.source=' + install + '/data/ingestion-queue'
         + ' -Dpub.spring.ingest.destination=' + install + '/data/ingested'
         + ' -Dtopaz.search.indexpath=' + install + '/data/lucene'
         + ' -Dtopaz.search.defaultfields=description,title,body,creator'      
       )
      }
      ant.echo 'Publishing app Stopped'
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
    if (data != null) {
      ant.exec(executable: 'mvn', failonerror:true) {
        arg(line: '-f ' + pom() + ' ant-tasks:tgz-explode -Dlocation=' + install 
          + ' -Dtype=tgz -Ddependencies=' + data)
      }
    }

    File d = new File(install);
    d = new File(install, 'data')
    if (!d.exists())
       d.mkdir()
    File m = new File(d, 'mulgara')
    if (!m.exists())
      m.mkdir()

    File f = new File(d, 'fedora')
    if (!f.exists())
      f.mkdir()
    
    File iq = new File(d, 'ingestion-queue')
    if (!iq.exists())
      iq.mkdir()

    File cq = new File(d, 'ingested')
    if (!cq.exists())
      cq.mkdir()

    File l = new File(d, 'lucene')
    if (!l.exists())
      l.mkdir()

    ant.exec(executable: 'mvn') {
      arg(line: 'ant-tasks:fedora-rebuild ' + opts
       + ' -DFEDORA_REBUILD_STDIN=' + rebuildInput()
       + ' -DFEDORA_REBUILD_FROM=' + install + '/data/fedora')
    }
  }

  private String rebuildInput() {
     return resource('/fedora-rebuild-input')
  }
  
  private String publishingAppLog4j() {
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
