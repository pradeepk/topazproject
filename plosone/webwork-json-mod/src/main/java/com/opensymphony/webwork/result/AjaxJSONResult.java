package com.opensymphony.webwork.result;


import com.metaparadigm.jsonrpc.JSONSerializer;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.Result;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Result to generate a JSON response from an Action.
 *
 * @author <a href="mailto:fzammetti@omnytex.com">Frank W. Zammetti</a>
 */
public class AjaxJSONResult implements Result {


  /**
   * Serializable ID.
   */
  public static final long serialVersionUID = 1;


  /**
   * Log instance.
   */
  private static Log log = LogFactory.getLog(AjaxJSONResult.class);


  /**
   * Generate a JSON response from the Action associated with this request
   * using the fields of the Action to populate the message.
   *
   * @param inInvocation The execution state of the action.
   */
  public void execute(final ActionInvocation inInvocation)  {

    ActionContext actionContext = inInvocation.getInvocationContext();
    HttpServletResponse response =
      (HttpServletResponse)actionContext.get(
        ServletActionContext.HTTP_RESPONSE);

    // Generate JSON.
    JSONSerializer jsonSerializer = new JSONSerializer();
    jsonSerializer.setDebug(false);
    jsonSerializer.setMarshallClassHints(false);
    jsonSerializer.setMarshallNullAttributes(true);
    String json = null;
    try {
      jsonSerializer.registerDefaultSerializers();
      json = jsonSerializer.toJSON(inInvocation.getAction());
      if (log.isDebugEnabled()) {
        log.debug("\n\nJSON!! = " + json);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Write JSON to response.
    try {
      response.setContentLength(json.length());
//      response.setContentType("text/xml");
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(json);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  } // End execute().

  
} // End class.
