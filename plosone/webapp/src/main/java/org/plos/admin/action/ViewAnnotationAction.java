package org.plos.admin.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.annotation.service.Annotation;
import org.plos.annotation.service.AnnotationService;

public class ViewAnnotationAction extends BaseActionSupport {
	
	private String annotationId;
	private Annotation annotation;
	private AnnotationService annotationService;
	
	
	private static final Log log = LogFactory.getLog(ViewAnnotationAction.class);

	
	public String execute() throws Exception {
		annotation = getAnnotationService().getAnnotation(annotationId);
		return SUCCESS;
	}


	public AnnotationService getAnnotationService() {
		return annotationService;
	}


	public void setAnnotationService(AnnotationService annotationService) {
		this.annotationService = annotationService;
	}


	public Annotation getAnnotation() {
		return annotation;
	}


	public void setAnnotationId(String annotationId) {
		this.annotationId = annotationId;
	}	
}
