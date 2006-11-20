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

public class PublishArchivesAction extends BaseActionSupport {

	private static final Log log = LogFactory.getLog(PublishArchivesAction.class);
	private DocumentManagementService documentManagementService;
	private Collection uploadableFiles;
	private Collection publishableFiles;
	private String articlesToPublish;
	
	public void setDocumentManagementService(DocumentManagementService documentManagementService) {
		this.documentManagementService = documentManagementService;
	}
	
	public void setArticlesToPublish(String articles) {
		articlesToPublish = articles;
	}
	
	public String execute() throws RemoteException, ApplicationException  {
		Iterator articles = new ArrayIterator(articlesToPublish.split(","));
		while (articles.hasNext()) {
			String article = ((String) articles.next()).trim();
			try {
				documentManagementService.publish(article);
				addActionMessage("Published: " + article);
			} catch (Exception e) {
				addActionMessage("Error publishing: " + article + " - " + e.toString());
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
