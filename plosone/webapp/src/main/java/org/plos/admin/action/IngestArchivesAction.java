package org.plos.admin.action;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.action.BaseActionSupport;
import org.plos.admin.service.DocumentManagementService;
import org.plos.admin.service.ImageResizeException;

import com.opensymphony.webwork.interceptor.ParameterAware;

public class IngestArchivesAction extends BaseActionSupport {

	private static final Log log = LogFactory.getLog(IngestArchivesAction.class);
	private DocumentManagementService documentManagementService;
	private Collection uploadableFiles;
	private Collection publishableFiles;
	private String[] filesToIngest;
	
	public void setDocumentManagementService(DocumentManagementService documentManagementService) {
		this.documentManagementService = documentManagementService;
	}
	
	public void setFilesToIngest(String[] files) {
		filesToIngest = files;
	}
	
	public String execute() throws RemoteException, ApplicationException  {
		if (filesToIngest != null) {
      String articleURI = null;
      for (String filename: filesToIngest) {
		    articleURI = null;
		    filename = filename.trim();
        try {
		      articleURI = documentManagementService.ingest(
							  new File(documentManagementService.getDocumentDirectory(), filename));
		      addActionMessage("Ingested: " + filename);
		    } catch (ImageResizeException ire) {
          addActionMessage("Error ingesting: " + filename + " - " + ire.getCause().toString());
          log.error("Error ingesting articles: " + filename, ire);
          articleURI = ire.getArticleURI();
          log.debug("trying to delete: " + articleURI);
          try {
            documentManagementService.delete(articleURI);
          } catch (Exception deleteException){
            log.error ("Could not delete article: " + articleURI, deleteException);
          }
        } catch (Exception e) {
          addActionMessage("Error ingesting: " + filename + " - " + e.toString());
          log.error("Error ingesting article: " + filename, e);
		    }
		  }
		  uploadableFiles = documentManagementService.getUploadableFiles();
		  publishableFiles = documentManagementService.getPublishableFiles();
    }
		return SUCCESS;
	}
	
	public Collection getUploadableFiles() {
		return uploadableFiles;
	}	
	
	public Collection getPublishableFiles() {
		return publishableFiles;
	}	
}
