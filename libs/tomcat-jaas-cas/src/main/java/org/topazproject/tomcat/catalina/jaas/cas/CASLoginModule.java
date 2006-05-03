
package org.topazproject.tomcat.catalina.jaas.cas;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.yale.its.tp.cas.client.ProxyTicketValidator;

/**
 * This class implements a JAAS <code>LoginModule</code> that is suitable for use in 
 * tomcat's JAAS Realm, and that defers authentication to CAS. See 
 * <code>org.apache.catalina.realm.JAASCallbackHandler</code> for the values available 
 * via the <code>CallbackHandler</code>. Also see
 * <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/jaas/JAASRefGuide.html">JAAS documentation</a> for details on LoginModules.
 * <p>
 * This class assumes that the CAS <code>ticket</code> value is available via the 
 * <code>NameCallback</code>.
 * <p>
 * The CAS <code>service</code> MAY be hard-coded into the configuration; if it is not,
 * the <code>PasswordCallbackHandler</code> MAY return the <code>service</code> URL.
 * <p>
 * The <code>cas_validate_url</code> MUST be hard-coded in the configuration 
 * <p>
 * Sample configuration:
 * 
 * <pre>
 * Application
 * {
 *      org.topazproject.tomcat.catalina.jaas.cas.CASLoginModule sufficient    
 *          cas_validate_url="https://my.org/cas/serviceValidate"
 * };
* 
* </pre>
  *
* @author Pradeep Krishnan (pradeep@topazproject.org)
  */
  public class CASLoginModule implements LoginModule{
    protected Subject subject;
    protected CallbackHandler callbackHandler;
    protected String casValidateUrl;
    protected String configuredService;
    protected Principal principal;

    /**
     * Initialize the CASLoginModule.
     * @param subject - the <code>Subject</code> to be authenticated
     * @param callbackHandler - a <code>CallbackHandler</code> for communicating with 
     *      Tomcat. See <code>org.apache.catalina.realm.JAASCallbackHandler</code>
     * @param sharedState - ignored
     * @param options can contain <ul>
     *      <li><strong>cas_validate_url</strong> (required)</li>
     *      <li><strong>service</strong> (optional)</li>
     * </ul>
     * 
     */ 
    public void initialize(Subject subject, CallbackHandler callbackHandler, 
        Map sharedState, Map options){
      this.subject=subject;
      this.callbackHandler=callbackHandler;
      this.casValidateUrl=(String)options.get("cas_validate_url");
      this.configuredService = (String)options.get("service");
      if ((configuredService != null) && "".equals(configuredService.trim()))
        configuredService = null;
    }
    /**
     * Method to authenticat a Subject (phase 1).
     * <p>
     * The implementation of this method authenticates a Subject. It assumes
     * that the <code>CallbackHandler</code> can return the <code>ticket</code>
     * and optionaly <code>service</code> for validation against the CAS service URL.
     * 
     * @return true if authentication succeeds; false if this module is to be ignored
     * @throws LoginException if authentication fails.
     */
    public boolean login() throws LoginException{
      Callback[] callbacks;
      NameCallback ticketCallback = new NameCallback("ticket");
      PasswordCallback serviceCallback = null;
      String service = configuredService;
      if(service==null){
        //the service has not been hardcoded, so let us get it
        //from the password callback
        serviceCallback = new PasswordCallback("service url: ", true);
        callbacks = new Callback[] {ticketCallback,serviceCallback};
      }
      else{
        callbacks = new Callback[] {ticketCallback};
      }

      try{
        callbackHandler.handle(callbacks);
      }
      catch(IOException e){
        throw new LoginException(e.getMessage());
      }
      catch(UnsupportedCallbackException e){
        throw new LoginException(e.getMessage());
      }
      String ticket = ticketCallback.getName();

      if (ticket!=null){
        ticket = ticket.trim();
        if (!ticket.startsWith("ST") && !ticket.startsWith("PT"))
          ticket = null;
      }

      if (ticket == null)
        throw new FailedLoginException("No CAS ticket");

      if(serviceCallback!=null){
        char [] p = serviceCallback.getPassword();
        if (p != null)
          service = new String(p);

      }
      ProxyTicketValidator pv = new ProxyTicketValidator();
      pv.setCasValidateUrl(casValidateUrl);
      if(service!=null &&  !("".equals(service.trim()))){
        try{
          pv.setService(URLEncoder.encode(service,"UTF-8"));
        }
        catch (UnsupportedEncodingException e){
          throw new LoginException(e.getMessage());
        }
      }
      pv.setServiceTicket(ticket);
      try{
        pv.validate();
      }
      catch(IOException e){
        throw new LoginException(e.getMessage());
      }
      catch(SAXException e){
        throw new LoginException(e.getMessage());
      }
      catch(ParserConfigurationException e){
        throw new LoginException(e.getMessage());
      }
      if(pv.isAuthenticationSuccesful()){
        final String name = pv.getUser();
        principal = new Principal(){
          public String getName(){
            return name;
          }
        };
        //authentication successful
        return true;
      }

      //authentication failed
      throw new FailedLoginException("Login failed.");
    }

    /**
     * Commit the authentication process (phase 2).
     *
     * @return true if commit succeeds.
     * @throws LoginException if abort fails
     */
    public boolean commit() throws LoginException{
      if(principal!=null){
        subject.getPrincipals().add(principal);
        return true;
      }
      return false;
    }

    /**
     * Abort the authentication process (phase 2).
     * @return true if abort succeeds
     * @throws LoginException if abort fails
     */
    public boolean abort() throws LoginException{
      if(principal!=null){
        principal = null;
        return true;
      }
      return false;
    }

    /**
     * Logout a Subject.
     * 
     * @return true if abort succeeds
     * @throws LoginException if abort fails
     */
    public boolean logout() throws LoginException{
      if(principal!=null){
        subject.getPrincipals().remove(principal);
        return true;
      }
      return false;
    }

  }

