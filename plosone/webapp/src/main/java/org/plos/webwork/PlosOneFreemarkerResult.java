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

import java.io.IOException;

import com.opensymphony.webwork.views.freemarker.FreemarkerResult;

import freemarker.template.SimpleHash;

/**
 * Custom Freemarker Result class so that we can pass the templateFile name into the template
 * in order to have a limited number of templates for the system.
 * 
 * @author Stephen Cheng
 *
 */
public class PlosOneFreemarkerResult extends FreemarkerResult {
  private String templateFile;
  
  /**
   * @return Returns the templateFile.
   */
  public String getTemplateFile() {
    return templateFile;
  }

  /**
   * @param templateFile The templateFile to set.
   */
  public void setTemplateFile(String templateFile) {
    this.templateFile = templateFile;
  }

  protected boolean preTemplateProcess(freemarker.template.Template template,
                                       freemarker.template.TemplateModel model) throws IOException{
    ((SimpleHash)model).put("templateFile", this.templateFile);
    return super.preTemplateProcess(template, model);
      
  }
  
  
}
