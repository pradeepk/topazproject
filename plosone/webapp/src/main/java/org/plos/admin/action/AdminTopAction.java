package org.plos.admin.action;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.admin.service.DocumentManagementService;
import org.springframework.beans.factory.xml.DocumentLoader;

public class AdminTopAction extends BaseActionSupport {
	
	private static final Log log = LogFactory.getLog(AdminTopAction.class);

	private Collection uploadableFiles;
	private Collection publishableFiles;
	
	private DocumentManagementService documentManagementService;
	
	public String execute() throws Exception {
		uploadableFiles = documentManagementService.getUploadableFiles();
		publishableFiles = documentManagementService.getPublishableFiles();
		return SUCCESS;
	}	

	public void setDocumentManagementService(DocumentManagementService documentManagementService) {
		this.documentManagementService = documentManagementService;
	}
	
	public Collection getUploadableFiles() {
		return uploadableFiles;
	}
	
	public Collection getPublishableFiles() {
		return publishableFiles;
	}	
}
