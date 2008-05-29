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
 
 /**
  * ---------
  * dojoBuild
  * ---------
  * This script performs a dojo custom build of the dojo library and the defined ambra dojo widgets.
  * Specifically, it performs the following:
  * 1) Perfoms a custom dojo build. 
  * 2) Applies ambra specific dojo library fixes to the built files.
  * 3) Injects interned locale data into the built js files.
  * 
  * IMPT: The invoking JVM's working directory is critical: it must be the js ancestor dir (the dir containing the pom.xml).
  * IMPT: This script sadly depends on the gmaven plugin scripting context and therefore can NOT be run standalone!! 
  * 
  * @see http://dojotoolkit.org/book/dojo-book-0-9/part-4-meta-dojo/package-system-and-custom-builds
  */

 static final String NL = System.getProperty("line.separator")
 
 static final String locales =  
   "dojo.provide(\"dojo.nls.ambra_xx\");dojo.provide(\"dijit.nls.loading\");dijit.nls.loading._built=true;dojo.provide(\"dijit.nls.loading.xx\");dijit.nls.loading.xx={\"loadingState\":\"Loading...\",\"errorState\":\"Sorry, an error occurred\"};dojo.provide(\"dijit.nls.common\");dijit.nls.common._built=true;dojo.provide(\"dijit.nls.common.xx\");dijit.nls.common.xx={\"buttonOk\":\"OK\",\"buttonCancel\":\"Cancel\",\"buttonSave\":\"Save\",\"itemClose\":\"Close\"};dojo.provide(\"dojo.nls.ambra_ROOT\");dojo.provide(\"dijit.nls.loading\");dijit.nls.loading._built=true;dojo.provide(\"dijit.nls.loading.ROOT\");dijit.nls.loading.ROOT={\"loadingState\":\"Loading...\",\"errorState\":\"Sorry, an error occurred\"};dojo.provide(\"dijit.nls.common\");dijit.nls.common._built=true;dojo.provide(\"dijit.nls.common.ROOT\");dijit.nls.common.ROOT={\"buttonOk\":\"OK\",\"buttonCancel\":\"Cancel\",\"buttonSave\":\"Save\",\"itemClose\":\"Close\"};dojo.provide(\"dojo.nls.ambra_en\");dojo.provide(\"dijit.nls.loading\");dijit.nls.loading._built=true;dojo.provide(\"dijit.nls.loading.en\");dijit.nls.loading.en={\"loadingState\":\"Loading...\",\"errorState\":\"Sorry, an error occurred\"};dojo.provide(\"dijit.nls.common\");dijit.nls.common._built=true;dojo.provide(\"dijit.nls.common.en\");dijit.nls.common.en={\"buttonOk\":\"OK\",\"buttonCancel\":\"Cancel\",\"buttonSave\":\"Save\",\"itemClose\":\"Close\"};dojo.provide(\"dojo.nls.ambra_en-us\");dojo.provide(\"dijit.nls.loading\");dijit.nls.loading._built=true;dojo.provide(\"dijit.nls.loading.en_us\");dijit.nls.loading.en_us={\"loadingState\":\"Loading...\",\"errorState\":\"Sorry, an error occurred\"};dojo.provide(\"dijit.nls.common\");dijit.nls.common._built=true;dojo.provide(\"dijit.nls.common.en_us\");dijit.nls.common.en_us={\"buttonOk\":\"OK\",\"buttonCancel\":\"Cancel\",\"buttonSave\":\"Save\",\"itemClose\":\"Close\"};";
 
 // NOTE: the gmaven plugin provides AntBuilder in the scripting context
 if(!ant) ant = new AntBuilder()

 def fixDojoCore = { String fpath ->
   File f = new File(fpath)
   StringBuilder sbuf = new StringBuilder(900000)
   f.eachLine{ line -> 
     sbuf.append(line)
     sbuf.append(NL)
   }
   final String str = "setTimeout(dojo._scopeName + \".loaded();\", 0);";
   int index = sbuf.indexOf(str)
   if(index >= 0) {
     sbuf.replace(index, index + str.length(), "/*AMBRA TWEAK*/setTimeout(dojo._scopeName + \".loaded();\", (dojo.isIE && typeof articlePage != 'undefined') ? 1000 : 0);/*END AMBRA TWEAK*/")
     f.delete();
     f.write(sbuf.toString());
   }
 }
 
 def injectLocales = { String fpath ->
   File f = new File(fpath)
   StringBuilder sbuf = new StringBuilder(900000)
   f.eachLine{ line -> 
     sbuf.append(line)
     sbuf.append(NL)
   }
   int index = sbuf.lastIndexOf('dojo.i18n._preloadLocalizations')
   if(index >= 0) {
     sbuf.insert(index, locales)
     f.delete();
     f.write(sbuf.toString());
   }
 }
 
 // dojo build settings 
 // IMPT: paths in the following setting vars are relative to the 'dojo-release-xxx-src/util/buildscripts' dir
 final String profileFile = '../../../ambra.profile.js';
 final String action = 'release';
 final String releaseName = 'dojo';
 final String releaseDir = '../../../../../../target/';
 final String localeList = 'en-us';
 final String optimize = ''
 final String layerOptimize = 'shrinksafe'
 final String copyTests = 'false'
 final String version = project.version;
 
 final String rhinoJarPath = (project.basedir.toString() + '/' + project.build.scriptSourceDirectory + '/dojo/util/shrinksafe/custom_rhino.jar')
 final String rhinoWorkingDir = (project.basedir.toString() + '/' + project.build.scriptSourceDirectory + '/dojo/util/buildscripts') 
 
 // java -jar ../shrinksafe/custom_rhino.jar build.js %*
 println 'Invoking ambra dojo build (' + profileFile + ')...' 
 ant.java(jar: rhinoJarPath, fork:true, dir: rhinoWorkingDir, resultproperty:'dojoBuildResult') {
   arg(value: 'build.js')
   arg(value: 'profileFile=' + profileFile)
   arg(value: 'action=' + action)
   arg(value: 'releaseName=' + releaseName)
   arg(value: 'releaseDir=' + releaseDir)
   arg(value: 'localeList=' + localeList)
   arg(value: 'optimize=' + optimize)
   arg(value: 'layerOptimize=' + layerOptimize)
   arg(value: 'copyTests=' + copyTests)
   arg(value: 'version=' + version)
 }
 def dojoBuildResult = ant.project.properties.'dojoBuildResult';
 if(dojoBuildResult != '0') {
   println 'dojo build error (exit code: ' + dojoBuildResult+ ').  Aborting!'
   System.exit(1)
 }
 println 'dojo build complete' 

 // apply ambra specific dojo library fixes to the built files
 println 'Applying ambra specific dojo library fixes...'
 fixDojoCore(project.build.directory + '/dojo/dojo/dojo.js')
 fixDojoCore(project.build.directory + '/dojo/dojo/dojo.js.uncompressed.js')
 println 'Ambra specific dojo library fixes applied'
 
 // inject the locales to the built files
 println 'Injecting locale(s)...'
 injectLocales(project.build.directory + '/dojo/dojo/ambra.js')
 injectLocales(project.build.directory + '/dojo/dojo/ambra.js.uncompressed.js')
 println 'Locale(s) injected'
 