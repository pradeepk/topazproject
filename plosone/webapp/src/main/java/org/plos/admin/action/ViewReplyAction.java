package org.plos.admin.action;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.annotation.service.ReplyWebService;
import org.topazproject.ws.annotation.ReplyInfo;

public class ViewReplyAction extends BaseAdminActionSupport {
	
	private String replyId;
	private ReplyInfo replyInfo;
	private ReplyWebService replyWebService;
	
	private static final Log log = LogFactory.getLog(ViewReplyAction.class);

	
	public String execute() throws Exception {
		replyInfo = replyWebService.getReplyInfo(replyId);
		return SUCCESS;
	}

	public ReplyInfo getReplyInfo() {
		return replyInfo;
	}


	public void setReplyId(String annotationId) {
		this.replyId = annotationId;
	}

	public void setReplyWebService(ReplyWebService replyWebService) {
		this.replyWebService = replyWebService;
	}
}
