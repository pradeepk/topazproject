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

package org.plos.article.util

import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

import org.plos.util.ToolHelper

/**
 * Create scaled down versions of all images and add them as additional representations
 * to the SIP.
 *
 * @author stevec
 * @author Ronald Tschalär
 */
public class ProcessImages {
  boolean verbose
  String  imConvert = 'convert'
  String  imIdentify = 'identify'

  /**
   * Create the scaled images and add them to the sip.
   *
   * @param articleFile the sip
   * @param newName     the new sip file's name, or null to overwrite
   */
  public void processImages(String articleFile, String newName) {
    if (verbose) {
      println('Processing file: ' + articleFile)
    }

    SipUtil.updateZip(articleFile, newName) { articleZip, newZip ->
      // get manifest
      ZipEntry me = articleZip.getEntry(SipUtil.MANIFEST)
      if (me == null)
        throw new IOException(
            "No manifest found - expecting one entry called '${SipUtil.MANIFEST}' in zip file")

      def manif = SipUtil.getManifestParser().parse(articleZip.getInputStream(me))

      // copy and scale
      Map<String, List<File>> imgNames = [:]

      for (entry in articleZip.entries()) {
        if (entry.name == SipUtil.MANIFEST)
          continue

        newZip.copyFrom(articleZip, [entry.name])

        if (entry.name.toLowerCase().endsWith('.tif')) {
          File f = File.createTempFile('tmp_', entry.name)
          if (verbose)
            println 'Created temp file: ' + f.getCanonicalPath()
          f.withOutputStream{ it << articleZip.getInputStream(entry) }

          imgNames[entry.name] = []
          resizeImage(entry.name, imgNames[entry.name], f)
          f.delete()
        }
      }

      if (verbose)
        println 'Number of resized images: ' + imgNames.inject(0){ cnt, e -> cnt += e.value.size() }

      // write out the new images
      for (newImg in imgNames.inject([]){ res, e -> res + e.value }) {
        if (verbose)
          println 'Adding to zip file: ' + newImg.name

        newZip.putNextEntry(new ZipEntry(newImg.name))
        newImg.withInputStream{ newZip << it }
        newZip.closeEntry()

        newImg.delete()
      }

      // write out the new manifest
      newZip.putNextEntry(new ZipEntry(SipUtil.MANIFEST))

      newZip << '<?xml version="1.1"?>\n'
      newZip << '<!DOCTYPE manifest SYSTEM "manifest.dtd">\n'

      def newManif = new groovy.xml.MarkupBuilder(new OutputStreamWriter(newZip, 'UTF-8'))
      newManif.doubleQuotes = true

      newManif.'manifest' {
        manif.articleBundle.each{ ab ->
          articleBundle {
            article(uri:ab.article.@uri, 'main-entry':ab.article.'@main-entry') {
              for (r in ab.article.representation) {
                representation(name:r.@name, entry:r.@entry)
                for (img in imgNames[r.@entry.text()])
                  representation(name:SipUtil.getRepName(img.name), entry:img.name)
              }
            }

            for (obj in ab.object) {
              object(uri:obj.@uri) {
                for (r in obj.representation) {
                  representation(name:r.@name, entry:r.@entry)
                  for (img in imgNames[r.@entry.text()])
                    representation(name:SipUtil.getRepName(img.name), entry:img.name)
                }
              }
            }
          }
        }
      }

      newZip.closeEntry()
    }
  }

  private void resizeImage(name, imgNames, file) {
    def baseName = name.substring(0, name.lastIndexOf('.')) + '.png'

    doResize(file, baseName + '_S', "-resize \"70x>\"", imgNames)
    doResize(file, baseName + '_L', "", imgNames)

    if (verbose) {
      println "Sizing " + name
    }
    def props = antExec(imIdentify, '-quiet -format "%w %h" "' + file.getCanonicalPath() + '"')

    def dim = props.cmdOut.split(" ")
    def height = dim[1]
    def width = dim[0]

    if (verbose) {
      println 'height = ' + height;
      println 'width= ' + width;
    }

    def arg = (height > width) ? "x600>" : "600x>"

    doResize(file, baseName + '_M', "-resize \"${arg}\"", imgNames)
  }

  private void doResize(file, outName, args, imgNames) {
    def newFile = new File(new File(System.getProperty('java.io.tmpdir')), outName)

    if (verbose) {
      println "Creating " + newFile
    }

    def props = antExec(imConvert, "\"${file.canonicalPath}\" ${args} \"png:${newFile}\"")

    if (props.cmdExit == '0') {
      imgNames.add(newFile)
    }
  }

  private Properties antExec(exe, args) {
    def ant = new AntBuilder()   // create an antbuilder
    ant.exec(outputproperty:"cmdOut",
             errorproperty: "cmdErr",
             resultproperty:"cmdExit",
             failonerror: "true",
             executable: exe) {
               arg(line: args)
             }

    if (verbose) {
      println "return code:  ${ant.project.properties.cmdExit}"
      println "stderr:       ${ant.project.properties.cmdErr}"
      println "stdout:       ${ant.project.properties.cmdOut}"
    }

    return ant.project.properties
  }

  public static void main(String[] args) {
    args = ToolHelper.fixArgs(args)

    def cli = new CliBuilder(usage: 'processimages [-v] [-c <config-overrides.xml>] [-o <output.zip>] <article.zip>' , writer : new PrintWriter(System.out))
    cli.h(longOpt:'help', "help (this message)")
    cli.o(args:1, 'output.zip - new zip file containing resized images')
    cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz/ambra.xml')
    cli.v(args:0, 'verbose')


    // Display help if requested
    def opt = cli.parse(args); if (opt.h) { cli.usage(); return }
    String[] otherArgs = opt.arguments()
    if (otherArgs.size() != 1) {
      cli.usage()
      return
    }

    ProcessImages pi = new ProcessImages(verbose: opt.v)

    def CONF = ToolHelper.loadConfiguration(opt.c)
    pi.imConvert  = CONF.getString("ambra.services.documentManagement.imageMagick.executablePath", 'convert')
    pi.imIdentify = CONF.getString("ambra.services.documentManagement.imageMagick.identifyPath", 'identify')

    pi.processImages(otherArgs[0], opt.o ?: null)
  }
}
