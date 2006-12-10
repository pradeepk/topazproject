package org.plos.admin.action;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.admin.service.DocumentManagementService;
import org.springframework.beans.factory.xml.DocumentLoader;

public class AdminTopAction extends BaseAdminActionSupport {
	
	private static final Log log = LogFactory.getLog(AdminTopAction.class);

	public String execute() throws Exception {
		return base();
	}	

}
