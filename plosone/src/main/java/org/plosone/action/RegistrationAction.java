package org.plosone.action;

import java.util.Map;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionSupport;

import edu.yale.its.tp.cas.client.CASReceipt;
import edu.yale.its.tp.cas.client.filter.CASFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plosone.PlosOneUser;

/**
 * Action class to test out single sign on and token passing
 * 
 * @author Stephen Cheng
 * 
 */
public class RegistrationAction extends ActionSupport {
  private static final Log log = LogFactory.getLog(RegistrationAction.class);

  private String signOnId;

  private String ticket;

  /**
   * A default implementation that does nothing and returns "success" with the user's CAS identity.
   * 
   * @return {@link #SUCCESS}
   */
  public String execute() throws Exception {
    Map sessionMap = ActionContext.getContext().getSession();
    this.signOnId = (String) sessionMap.get(CASFilter.CAS_FILTER_USER);
    CASReceipt receipt = (CASReceipt) sessionMap.get(CASFilter.CAS_FILTER_RECEIPT);
    this.ticket = receipt.getPgtIou();
    PlosOneUser newUser = new PlosOneUser();
    newUser.setEmail("testuser@topazproject.org");
    newUser.setDisplayName("testuser");
    newUser.create(this.signOnId, this.ticket);
    return SUCCESS;
  }

  /**
   * @return Returns the signOnId.
   */
  public String getSignOnId() {
    return this.signOnId;
  }

  /**
   * @param inSignOnId
   *          The signOnId to set.
   */
  public void setUser(String inSignOnId) {
    this.signOnId = inSignOnId;
  }

  /**
   * @return Returns the ticket.
   */
  public String getTicket() {
    return ticket;
  }

  /**
   * @param ticket
   *          The ticket to set.
   */
  public void setTicket(String ticket) {
    this.ticket = ticket;
  }

}
