/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

package org.plos.admin.action;

import java.io.File;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.admin.service.ImageResizeException;

import org.plos.article.util.DuplicateArticleIdException;

public class IngestArchivesAction extends BaseAdminActionSupport {

  private static final Log log = LogFactory.getLog(IngestArchivesAction.class);
  private String[] filesToIngest;

  public void setFilesToIngest(String[] files) {
    filesToIngest = files;
  }

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
        } catch (ImageResizeException ire) {
          addActionError("Error ingesting: " + filename + " - " + ire.getCause().toString());
          log.error("Error ingesting articles: " + filename, ire);
          articleURI = ire.getArticleURI().toString();
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
