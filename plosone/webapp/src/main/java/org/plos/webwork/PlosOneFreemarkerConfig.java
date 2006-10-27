/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.webwork;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;

/**
 * Class to configure the FreeMarker templates with css and javascript files and the title of page.
 * Used so that we can have just one or two main templates and swap out the body section with
 * a Webwork result.
 * 
 * 
 * @author Stephen Cheng
 *
 */
public class PlosOneFreemarkerConfig {
  private static final Log log = LogFactory.getLog(PlosOneFreemarkerConfig.class);
  
  private HashMap<String, String[]> cssFiles;
  private HashMap<String, String[]> javaScriptFiles;
  private HashMap<String, String> titles;
  
  private String[] defaultCss;
  private String[] defaultJavaScript;
  private String defaultTitle;

  private static final String[] DEFAULT_CSS_FILES = {"/css/pone_iepc.css", "/css/pone_screen.css"};
  private static final String[] DEFAULT_JS_FILES = {"/javascript/all.js"};
  private static final String DEFAULT_TITLE = "PLoS ONE";
  
  /**
   * Constructor that loads the list of css and javascript files and page titles for pages which
   * follow the standard templates.  
   * 
   */
  public PlosOneFreemarkerConfig() {
    ConfigurationStore myConfigStore = ConfigurationStore.getInstance();
    Configuration myConfig = myConfigStore.getConfiguration();
    String dirPrefix = myConfig.getString("webapp-dir");
    String subdirPrefix = myConfig.getString("webapp-subdir");
    
    String title = myConfig.getString("default.title");
    if (title != null) {
      defaultTitle = title;
    } else {
      defaultTitle = DEFAULT_TITLE;
    }
    
    List fileList = myConfig.getList("default.css.file");
    if (fileList.size() > 0) {
      defaultCss = new String[fileList.size()];
      Iterator iter = fileList.iterator();
      for (int i = 0; i < fileList.size(); i++) {
        defaultCss[i] = (String)iter.next();
      }
    } else {
      defaultCss = DEFAULT_CSS_FILES;
    }
    
    fileList = myConfig.getList("default.javascript.file");
    if (fileList.size() > 0) {
      defaultJavaScript = new String[fileList.size()];
      Iterator iter = fileList.iterator();
      for (int i = 0; i < fileList.size(); i++) {
        defaultJavaScript[i] = (String)iter.next();
      }
    } else {
      defaultJavaScript = DEFAULT_JS_FILES;
    }
    
    int numPages = myConfig.getList("page.name").size();
    int numCss, numJavaScript, j;
    String pageName, page;
    titles = new HashMap<String, String>();
    cssFiles = new HashMap<String, String[]>();
    javaScriptFiles = new HashMap<String, String[]>();
    

    String[] cssArray = null;
    String[] javaScriptArray = null;
    
    for (int i = 0; i < numPages; i++) {
      page = "page(" + i + ")";
      pageName = myConfig.getString(page + ".name");
      if (log.isDebugEnabled()){
        log.debug("Reading config for page name: " + pageName);
      }
      titles.put(pageName, myConfig.getString(page + ".title"));
      numCss = myConfig.getList(page + ".css.file").size();
      numJavaScript = myConfig.getList(page + ".javascript.file").size();
      cssArray = new String[numCss];
      javaScriptArray = new String[numJavaScript];
      for (j = 0; j < numCss; j++) {
        cssArray[j] =  dirPrefix + subdirPrefix + myConfig.getString(page + ".css.file(" + j + ")");
      }
      cssFiles.put(pageName, cssArray);
      for (j = 0; j < numJavaScript; j++) {
        javaScriptArray[j] =  dirPrefix + subdirPrefix + myConfig.getString(page + ".javascript.file(" + j + ")");
      }
      javaScriptFiles.put(pageName, javaScriptArray);
    }
  }
  
  /**
   * Gets the title for the given template name. Return the default PLoS ONE if not defined
   * 
   * @param templateName 
   * @return Returns the title given a template name.
   */
  public String getTitle(String templateName) {
    String retVal = titles.get(templateName);
    if (retVal == null) {
      return defaultTitle;
    } else {
      return retVal;
    }
  }


  /**
   * Gets the array of CSS files associated with templateName or returns the default values
   * if not available.
   * 
   * @param templateName
   * @return Returns list of css files given a template name.
   */  
  public String[] getCss(String templateName) {
    String[] retVal = cssFiles.get(templateName);
    if (retVal == null) {
      return defaultCss;
    } else {
      return retVal;
    }
  }

  /**
   * Gets the array of JavaScript files associated with templateName or returns the default values
   * if not available.
   * 
   * @param templateName 
   * @return Returns the list of JavaScript files given a template name.
   */
  public String[] getJavaScript(String templateName) {
    String[] retVal = javaScriptFiles.get(templateName);
    if (retVal == null) {
      return defaultJavaScript;
    } else {
      return retVal;
    }
  }
  
  
  /**
   * @return Returns the cssFiles.
   */
  public HashMap<String, String[]> getCssFiles() {
    return cssFiles;
  }

  /**
   * @param cssFiles The cssFiles to set.
   */
  public void setCssFiles(HashMap<String, String[]> cssFiles) {
    this.cssFiles = cssFiles;
  }

  /**
   * @return Returns the javaScriptFiles.
   */
  public HashMap<String, String[]> getJavaScriptFiles() {
    return javaScriptFiles;
  }

  /**
   * @param javaScriptFiles The javaScriptFiles to set.
   */
  public void setJavaScriptFiles(HashMap<String, String[]> javaScriptFiles) {
    this.javaScriptFiles = javaScriptFiles;
  }

  /**
   * @return Returns the titles.
   */
  public HashMap<String, String> getTitles() {
    return titles;
  }

  /**
   * @param titles The titles to set.
   */
  public void setTitles(HashMap<String, String> titles) {
    this.titles = titles;
  }
  
}
