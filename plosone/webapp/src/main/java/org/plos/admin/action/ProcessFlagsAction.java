package org.plos.admin.action;

import java.rmi.RemoteException;
import java.util.Iterator;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.annotation.service.AnnotationService;
import org.plos.annotation.service.AnnotationWebService;
import org.plos.annotation.service.Flag;
import org.plos.annotation.service.Reply;
import org.plos.annotation.service.ReplyWebService;
import org.topazproject.ws.annotation.NoSuchAnnotationIdException;

public class ProcessFlagsAction extends BaseAdminActionSupport {

	private static final Log log = LogFactory.getLog(ProcessFlagsAction.class);
	private String commentsToUnflag = "";
	private String commentsToDelete = "";
	private AnnotationService annotationService;	
	private ReplyWebService replyWebService;
	
	
	public void setAnnotationService(AnnotationService annotationService) {
		this.annotationService = annotationService;
	}

	public void setCommentsToUnflag(String comments) {
		commentsToUnflag = comments;
	}
	
	public void setCommentsToDelete(String comments) {
		commentsToDelete = comments;
	}

	public String execute() throws RemoteException, ApplicationException, NoSuchAnnotationIdException  {
		Iterator deletes = new ArrayIterator(commentsToDelete.split(","));
		Iterator unflags = new ArrayIterator(commentsToUnflag.split(","));
		String target;
		
		while (unflags.hasNext()) {
			target = ((String) unflags.next());
			if (target.length() > 0) { //Stupid ArrayIterator
				String segments[] = target.split("_");
				deleteFlag(segments[0], segments[1]);
			}
		}
		
		while (deletes.hasNext()) {
			target = ((String) deletes.next());
			if (target.length() > 0) {
				String segments[] = target.split("_");
				deleteTarget(segments[0], segments[1]);
			}
		}
		return base();
	}

	/**
	 * @param root
	 * @param target
	 * 
	 * Delete the target. Root is either an id (in which case 
	 * target is a reply) or else it is a "" in which case target is an annotation. 
	 * In either case:
	 * 		remove flags on this target
	 * 		get all replies to this target
	 * 			for each reply
	 * 				check to see if reply has flags
	 * 					remove each flag
	 * 		remove all replies in bulk
	 * 		remove target
	 * 		
	 * 	Note that because this may be called from a 'batch' job (i.e multiple
	 *  rows were checked off in process flags UI) it may be that things have
	 *  been deleted by previous actions. So we catch 'non-exsietent-id' exceptions
	 *  which may well get thrown and recover and continue (faster than going across
	 *  teh web service tro see if the ID is valid then going out again to get its object).		
	 * @throws ApplicationException 
	 * @throws ApplicationException 
	 * @throws NoSuchAnnotationIdException 
	 * @throws RemoteException 
	 * @throws NoSuchAnnotationIdException 
	 * @throws RemoteException 
	 */
	private void deleteTarget(String root, String target) throws ApplicationException, RemoteException, NoSuchAnnotationIdException {
		Reply[] replies;		
		Flag[] flags = annotationService.listFlags(target);
		boolean isReply = root.length() > 0;
		
		log.debug("Deleting " + target + " Root is " + root);
		log.debug(target + " has " + flags.length + " flags - deleting them");
		for (Flag flag: flags) {
			if (flag.isDeleted()) // A bug in infrastructure ?
				continue;
			deleteFlag(target, flag.getId());
		}

		if (isReply) { // it's a reply
			replies = annotationService.listReplies(root, target);
		} else {
			replies = annotationService.listReplies(target, target);
		}
		
		log.debug(target + " has " + replies.length + " replies. Removing their flags");
		for (Reply reply : replies) {
			if (reply.isDeleted())
					continue;
			flags = annotationService.listFlags(reply.getId());
			log.debug(">>>>>>>> reply " + reply.getId() + " has " + flags.length + " flags");
			for (Flag flag: flags) {
				if (flag.isDeleted())
					continue;
				deleteFlag(reply.getId(), flag.getId());
			}				
		}
		
		if (isReply) {
			replyWebService.deleteReplies(target); // Bulk delete
			annotationService.deleteReply(target);
			log.debug("Deleted reply: " + target);			
		} else {
			replyWebService.deleteReplies(target, target);
			annotationService.deletePublicAnnotation(target);
			log.debug("Deleted annotation: " + target);			
		}
	}

	private void deleteFlag(String target, String flag) throws ApplicationException {
		// Delete flag
		annotationService.deleteFlag(flag);
		// Deal with 'flagged' status
		Flag[] flags = annotationService.listFlags(target);
		log.debug(">>>>> deleting flag: " + flag + " on target: " + target);
		log.debug("Checking for flags on target: " + target + " There are " + flags.length + " flags remaining");
		if (0 == flags.length) {
			log.debug("Setting status to unflagged");
			if (isAnnotation(target)) {
				annotationService.unflagAnnotation(target);
			} else if (isReply(target)) {
				annotationService.unflagReply(target);
			} else {
				String msg = target + " cannot be unFlagged - not annotation or reply";
				log.error(msg);
				throw new ApplicationException(msg);
			}
		} else
			log.debug("Flags exist. Target will remain marked as flagged");
	}
	
	private boolean isReply(String target) {
		String segments[] = target.split("/");
		if (segments.length >= 2)
			if (segments[segments.length - 2].equalsIgnoreCase("reply"))
				return true;
		return false;
	}

	private boolean isAnnotation(String target) {
		String segments[] = target.split("/");
		if (segments.length >= 2)
			if (segments[segments.length - 2].equalsIgnoreCase("annotation"))
				return true;
		return false;
	}

	public void setReplyWebService(ReplyWebService replyWebService) {
		this.replyWebService = replyWebService;
	}
		
}
