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

/**
 * An integration test environment for PlosOne. The environment runs
 * its own copy of mulgara and fedora. The DummySSO Filter is enabled
 * instead of using CAS. The mulgara and fedora stores can be pre-populated
 * with canned data. Same with lucene and ingestion-queue etc. In addition
 * tests can reset the data used to the original state any time by calling 
 * the restore() function.
 * <p>
 * Multiple environments can be installed - however only one environment can 
 * be running at a time. So running tests in parallel is not an option.
 * <p>
 * The commands here launches mvn to run the ant-tasks-plugin for tasks like 
 * fedora-install, mulgara-install etc. Make sure this plugin is installed
 * in the local maven repository and the 'mvn' executable is in your PATH.
 * 
 * @author Pradeep Krishnan
 */
public class Env {
  private AntBuilder ant = new AntBuilder();
  private String install;
  private String data;
  private String opts;
  private static Env active = null;
  private String fedoraHome;

  public static boolean stopOnExit = true;
  private static boolean stopOnStart = true;
  private static String ext; 

  static {
    ext = (System.properties.'os.name'.toLowerCase().indexOf('windows') > -1) ? '.bat' : '';
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
    fedoraHome = path(install, "fedora-2.1.1")

    String url = '/net/sf/antcontrib/antlib.xml'
    url = this.getClass().getResource(url)
    Antlib.createAntlib(ant.antProject, url.toURL(), null).execute()
  }

  /**
   * Stop all services and clean the install directory.
   */
  public void clean() {
    ant.echo 'Cleaning ...' 
    stop();
    ant.delete(dir:install)
    ant.echo 'Finished cleaning'
  }

  /**
   * Stop all services.
   */ 
  public void stop() {
    if ((active == null) && (stopOnStart == false))
      return
    Env env = (active == null) ? this : active

    ant.echo 'Stopping all services on ...'
    for (task in ['plosone-stop', 'fedora-stop', 'mulgara-stop'])
      try { env.antTask(task) } catch (Throwable t) { } 
    active = null
    stopOnStart = false
    ant.echo 'Stopped all services'
  }

  /**
   * Install all services. A marker file is placed after the install
   * so that subsequent calls to install can bypass the actual install.
   */
  public void install() {
    File f = new File(path(install, '/installed'));
    if (f.exists())
      return;

    ant.echo 'Installing ...'

    clean()  // clean any previous partial installs

    for (task in ['ecqs-install', 'fedora-install', 'mulgara-install', 'plosone-install'])
      antTask(task)

    load()  // load data

    // install the log4j files
    resource('/plosoneLog4j.xml')
    resource('/mulgaraLog4j.xml')

    // finally create the marker file
    ant.touch(file: path(install, '/installed'))
    ant.echo 'Finished installation'
  }

  /**
   * Restore the data to the same state as a fresh install.
   */
  public void restore() {
    ant.echo 'Restoring data ...'
    stop()
    ant.delete(dir: path(install, "/data"))
    load()
    ant.echo 'Finished restoring data'
  }

 /**
  * Start all services. If another environment is currently running, that is stopped first.
  */
  public void start() {
    ant.echo 'Starting services for ...'
    if (active != null) {
      if (active.install.equals(install))
         return
      active.stop()
    }  
    active = this
    mulgara()
    fedora()
    waitFor('http://localhost:9091/mulgara-service/')
    ant.echo 'Mulgara started'
    plosone()
    // FIXME: mvn seems to have registered an auth-handler that is sending
    // out the repository password for topaz instead of the ones from here 
    //waitFor('http://fedoraAdmin:fedoraAdmin@localhost:9090/fedora/')
    waitFor('http://localhost:9090/')
    ant.echo 'Fedora started'
    waitFor('http://localhost:8080/plosone-webapp/')
    ant.echo 'Plosone started'
    ant.echo 'All services are up and running'
  }
  
  /**
   * Execute an ant-tasks plugin task. 
   */
  private void antTask(task) {
    ant.echo 'Executing ant-tasks:' + task + ' ...'
    ant.exec(executable: 'mvn' + ext, failonerror:true) {
      arg(line: 'ant-tasks:' + task + opts)
    }
    ant.echo 'Finished execution of ant-tasks:' + task
  }

  /**
   * Start up mulgara.
   */
  private void mulgara() {
    ant.echo 'Starting mulgara ...'
    ant.forget {
      exec(executable: 'mvn' + ext, failonerror:true) {
        arg(line: '-f ' + pom() + ' ant-tasks:mulgara-start -DDEST_DIR=' + install
         + ' -Dtopaz.mulgara.databaseDir=' + path(install, '/data/mulgara') 
         + ' -Dlog4j.configuration='+ pathUrl(install, '/mulgaraLog4j.xml'))
      }
      echo 'Mulgara stopped'
    }
  }

  /**
   * Start up fedora. mvn ant-tasks:fedora-start hangs in windows. So inline it here.
   */
  private void fedora() {
    ant.echo("Starting mckoi ...")
    ant.forget {
       exec(dir: fedoraHome, 
            executable:path(fedoraHome, "/server/bin/mckoi-start") + ext,
            failonerror:true) {
         env(key:"FEDORA_HOME", file:fedoraHome)
       }
    }
    ant.sleep(seconds:"10")
    ant.echo("Starting fedora ...")
    ant.delete(file: path(fedoraHome, '/server/status'))
    ant.forget {
       exec(dir: fedoraHome,
            executable:path(fedoraHome, "/server/bin/fedora-start") + ext,
            failonerror:true) {
         arg(line: "mckoi")
         env(key:"FEDORA_HOME", file:fedoraHome)
       }
    }
  }

  /**
   * Start up plosone.
   */
  private void plosone() {
    ant.echo 'Starting plosone ...'
    ant.forget {
      exec(executable: 'mvn' + ext, failonerror:true) {
        arg(line: '-f ' + pom() + ' ant-tasks:plosone-start -DDEST_DIR=' + install
         + ' -Dorg.plos.configuration.overrides=defaults-dev.xml -Dlog4j.configuration=' 
         + pathUrl(install, '/plosoneLog4j.xml') 
         + ' -Dpub.spring.ingest.source=' + path(install, '/data/ingestion-queue')
         + ' -Dpub.spring.ingest.destination=' + path(install, '/data/ingested')
         + ' -Dtopaz.search.indexpath=' + path(install, '/data/lucene')
         + ' -Dtopaz.search.defaultfields=description,title,body,creator'      
       )
      }
      echo 'Plosone Stopped'
    }
  }

  /**
   * Wait for the service to start up. Tries for over 2 minutes before giving up.
   */
  private String waitFor(String uri) {
    Throwable saved = null;
    for (i in 1..120) {
      try {return uri.toURL().text} catch (Throwable e)  {saved = e}
      ant.echo i + ' Waiting for ' + uri + ' ...'
      sleep 1000
    }
    ant.echo 'Failed to start the service at ' + uri
    throw saved
  }
  
  /**
   * Locate a pom.xml file to pass to mvn.  Only for the tasks that require a pom.
   * Looks up the pom in the current working directory or its parents. If a
   * pom is not found there, then the install directory and its parents are looked up.
   *
   * Note that the expectation here is to find a pom that is the head/pom.xml or
   * a child of the head/pom.xml where head represents the head of the plosone 
   * project source tree.
   */
  private String pom () {
    String p = pom(new File(System.properties.'user.dir'))
    if (p == null)
      p = pom(new File(install))
    if (p == null)
      throw new FileNotFoundException('a pom.xml was not found')
    return p
  }

  /**
   * Look up a pom in this directory or recursively its parents.
   * 
   * @param dir the directory to look for pom.xml
   *
   * @return the full path of the pom.xml file found or null
   */
  private String pom(File dir) {
    if (dir == null)
      return null;
    File f = new File(dir, 'pom.xml');
    if (f.exists())
      return f.absoluteFile.canonicalPath;
    return pom(dir.parentFile)
  }

  /**
   * Load canned data into the environment. The canned data is expected to be
   * a mvn artifact and is expected to be a tar.gz of the expected data directory
   * lay out. The directory layout is as follows:
   * <code>
   *       data
   *       `-- README
   *       `-- lucene
   *       `-- ingestion-queue
   *       `-- ingested
   *       `-- fedora
   *       |   |-- objects
   *       |   |-- datastreams
   *       `-- mulgara (mulgara data dir)
   * </code>
   */
  private void load() {
    ant.echo 'Loading/Creating data for ...'

    if (data != null) {
      ant.exec(executable: 'mvn' + ext, failonerror:true) {
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

    fedoraRebuild();

    ant.echo 'Finished loading data'
  }

  /**
   * Rebuild the fedora database. mvn ant-tasks:fedora-rebuild hangs in windows. So inline-it here.
   */
  private void fedoraRebuild() {
    ant.echo("Rebuilding fedora data ...")
    ant.echo("Starting mckoi ...")
    ant.forget {
       exec(dir: fedoraHome, 
            executable:path(fedoraHome, "/server/bin/mckoi-start") + ext,
            failonerror:true) {
         env(key:"FEDORA_HOME", file:fedoraHome)
       }
    }
    ant.sleep(seconds:"10")
    ant.echo("Replacing fedora objects and datastreams ...")
    ant.delete(dir: path(fedoraHome, "/data/datastreams"))
    ant.delete(dir: path(fedoraHome, "/data/objects"))
    ant.copy(todir:path(fedoraHome, "/data")) {
      fileset(dir:path(install, "/data/fedora"))
    }
    ant.echo("Invoking fedora-rebuild ...")
    ant.exec(dir: fedoraHome, 
             executable:path(fedoraHome, "/server/bin/fedora-rebuild") + ext,
             failonerror:true,
             input: resource('/fedora-rebuild-input')) {
      arg(line:"mckoi")
      env(key:"FEDORA_HOME", file:fedoraHome)
    }
    ant.echo("Stopping mckoi ...")
    ant.exec(dir: fedoraHome,
            executable:path(fedoraHome, "/server/bin/mckoi-stop") + ext,
            failonerror:true) {
      arg(line:"fedoraAdmin fedoraAdmin")
      env(key:"FEDORA_HOME", file:fedoraHome)
    }
    ant.sleep(seconds:"5")
    ant.echo("Fedora rebuild completed.")
  }

  /**
   * Copy a resource from class-path and return the file-name.
   *
   * @param name the resource to find in class-path
   * 
   * @return the filename where the resource was copied into.
   */
  public String resource(String name) {
     String input = path(install, name)
     def out = new BufferedOutputStream(new FileOutputStream(input))
     out << getClass().getResourceAsStream(name)
     out.close()
     return input
  }

  /**
   * Execute a groovy shell script with arguments.
   *
   * @param args command line where first argument is the script-file to execute
   */
  public void script(String[] args) {
    String res = args[0]
    String[] sargs = new String[args.length - 1]

    for (x in 1 .. args.length - 1)
        sargs[x-1] = args[x]

    ant.echo 'Executing script ' + res + ' with args ' + sargs
    GroovyShell shell = new GroovyShell();
    shell.run(new File(res), sargs)
  }

  /**
   * Convert to a platform specific file path.
   *
   * @param dir a directory or root path
   * @param file a filename or a relative path
   *
   * @return platform specific file path
   */
  private String path(String dir, String file) {
    String sep = System.properties.'file.separator'
        
    if (!file.startsWith('/'))
      file = '/' + file

    file =  dir + file
    if (!sep.equals('/')) {
      int pos;
      while ((pos = file.indexOf('/')) > -1)
         file = file.substring(0, pos) + sep + file.substring(pos+1)     
    }
    return file
  }

  /**
   * Convert a path to a file: URL.
   *
   * @param dir a directory or root path
   * @param file a filename or a relative path
   *
   * @return the file: URL for the path
   */
  private String pathUrl(String dir, String file) {
    File f = new File(path(dir, file))
    return f.toURL().toString()
  }
}
