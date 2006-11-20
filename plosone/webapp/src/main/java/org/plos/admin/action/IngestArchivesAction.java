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

import com.opensymphony.webwork.interceptor.ParameterAware;

public class IngestArchivesAction extends BaseActionSupport {

	private static final Log log = LogFactory.getLog(IngestArchivesAction.class);
	private DocumentManagementService documentManagementService;
	private Collection uploadableFiles;
	private Collection publishableFiles;
	private String filesToIngest;
	
	public void setDocumentManagementService(DocumentManagementService documentManagementService) {
		this.documentManagementService = documentManagementService;
	}
	
	public void setFilesToIngest(String files) {
		filesToIngest = files;
	}
	
	public String execute() throws RemoteException, ApplicationException  {
		Iterator filenames = new ArrayIterator(filesToIngest.split(","));
		while (filenames.hasNext()) {
			String filename = ((String) filenames.next()).trim();
			try {
				documentManagementService.ingest(
							new File(documentManagementService.getDocumentDirectory(), filename));
				addActionMessage("Ingested: " + filename);
			} catch (Exception e) {
				addActionMessage("Error ingesting: " + filename + " - " + e.toString());
				e.printStackTrace();
			}
		}
		uploadableFiles = documentManagementService.getUploadableFiles();
		publishableFiles = documentManagementService.getPublishableFiles();
		
		return SUCCESS;
	}
	
	public Collection getUploadableFiles() {
		return uploadableFiles;
	}	
	
	public Collection getPublishableFiles() {
		return publishableFiles;
	}	
}
