/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.action;

import java.io.File;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.article.util.DuplicateArticleIdException;
import org.plos.article.util.ImageProcessingException;

public class IngestArchivesAction extends BaseAdminActionSupport {

  private static final Log log = LogFactory.getLog(IngestArchivesAction.class);
  private String[] filesToIngest;

  public void setFilesToIngest(String[] files) {
    filesToIngest = files;
  }

  @Override
  public String execute() throws RemoteException, ApplicationException {
    if (filesToIngest != null) {
      String articleURI = null;
      for (String filename : filesToIngest) {
        articleURI = null;
        filename = filename.trim();
        try {
          articleURI = getDocumentManagementService().ingest(new File(
              getDocumentManagementService().getDocumentDirectory(),
              filename));
          addActionMessage("Ingested: " + filename);
        } catch (DuplicateArticleIdException de) {
          addActionError("Error ingesting: " + filename + " - " + de.toString());
          log.error("Error ingesting article: " + filename , de);
        } catch (ImageProcessingException ipe) {
          addActionError("Error ingesting: " + filename + " - " + ipe.getCause().toString());
          log.error("Error ingesting articles: " + filename, ipe);
          articleURI = ipe.getArticleURI().toString();
          if (log.isDebugEnabled()) {
            log.debug("trying to delete: " + articleURI);
          }
          try {
            getDocumentManagementService().delete(articleURI);
          } catch (Exception deleteException) {
            addActionError("Could not delete article: " + articleURI + ", " + deleteException);
            log.error("Could not delete article: " + articleURI, deleteException);
          }
        } catch (Exception e) {
          addActionError("Error ingesting: " + filename + " - " + e.toString());
          log.error("Error ingesting article: " + filename, e);
        }
      }
    }
    return base();
  }
}
