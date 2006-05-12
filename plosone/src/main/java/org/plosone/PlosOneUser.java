package org.plosone;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.User;
import org.plos.exception.UserException;

/**
 * Loose wrapper for the <code>org.plos.User</code> object so that we have application specific
 * variables and fields that we can deal with easily.
 * 
 * @author Stephen Cheng
 * 
 */
public class PlosOneUser {
  private static final Log log = LogFactory.getLog(PlosOneUser.class);

  private static final String EMAIL = "EMAIL";

  private static final String DISPLAY_NAME = "DISPLAY";

  private User topazUser;

  private String email;

  private String displayName;

  /**
   * Default constructor that doesn't do anything.
   */
  public PlosOneUser() {

  }

  /**
   * Constructor that takes in an <code>org.plos.User</code> and sets the appropriate app level
   * properties
   * 
   * @param inTopazUser
   */
  public PlosOneUser(User inTopazUser) {
    this.topazUser = inTopazUser;
    this.readProperties();
  }

  /**
   * Writes current user to store as a new user. Initialize variables through getters/setters first
   * 
   * @param signOnId
   *          caller CAS id
   * @param authToken
   *          caller CAS token
   * @throws UserException
   */
  public void create(String signOnId, String authToken) throws UserException {
    this.topazUser = new User();
    topazUser.setUserProperties(this.storeProperties());
    topazUser.setSignOnId(signOnId);
    topazUser.createUser(signOnId, authToken);
    if (log.isDebugEnabled()) {
      log.debug("User created: " + signOnId);
    }
  }

  /**
   * Deletes user from the store.
   * 
   * @param signOnId
   * @param authToken
   * @throws UserException
   * 
   */
  public void delete(String signOnId, String authToken) throws UserException {
    topazUser.deleteUser(signOnId, authToken);
  }

  /**
   * Updates current user in store with new properties
   * 
   * @param signOnId
   * @param authToken
   * @throws UserException
   */
  public void update(String signOnId, String authToken) throws UserException {
    topazUser.setUserProperties(this.storeProperties());
    // TODO: Probably need to update email addresses on alerts, and maybe other places...
    topazUser.updateUser(signOnId, authToken);
    if (log.isDebugEnabled()) {
      log.debug("User updated: " + this.getTopazUser().getTopazUserId());
    }
  }

  /**
   * Gets a user from the store
   * 
   * @param userToGet
   * @param signOnId
   * @param authToken
   * @return <code>PlosOneUser</code>
   */
  public static PlosOneUser getUser(String userToGet, String signOnId, String authToken) {
    PlosOneUser newUser = new PlosOneUser();
    newUser.setTopazUser(User.getUser(userToGet, signOnId, authToken));
    newUser.readProperties();
    return newUser;
  }

  private Hashtable<String, String> storeProperties() {
    Hashtable<String, String> userProperties = new Hashtable<String, String>();
    if (this.getEmail() != null) {
      userProperties.put(EMAIL, this.getEmail());
    }
    if (this.getDisplayName() != null) {
      userProperties.put(DISPLAY_NAME, this.getDisplayName());
    }
    return userProperties;
  }

  private void readProperties() {
    if (topazUser != null) {
      Hashtable<String, String> userProperties = topazUser.getUserProperties();
      if (userProperties != null) {
        this.setDisplayName(userProperties.get(DISPLAY_NAME));
        this.setEmail(userProperties.get(EMAIL));
      }
    }
  }

  /**
   * @return Returns the displayName.
   */
  public String getDisplayName() {
    if (displayName == null) {
      setDisplayName(topazUser.getProperty(DISPLAY_NAME));
    }
    return displayName;
  }

  /**
   * @param displayName
   *          The displayName to set.
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * @return Returns the email.
   */
  public String getEmail() {
    return email;
  }

  /**
   * @param email
   *          The email to set.
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return Returns the topazUser.
   */
  public User getTopazUser() {
    return topazUser;
  }

  /**
   * @param topazUser
   *          The topazUser to set.
   */
  public void setTopazUser(User topazUser) {
    this.topazUser = topazUser;
  }

}
