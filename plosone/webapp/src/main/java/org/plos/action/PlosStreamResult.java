/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.action;

import com.opensymphony.webwork.dispatcher.StreamResult;
import com.opensymphony.xwork.ActionInvocation;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class PlosStreamResult extends StreamResult {

  protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
    InputStream oInput = null;
    OutputStream oOutput = null;

    try {
      // Find the inputstream from the invocation variable stack
        oInput = (InputStream) invocation.getStack().findValue(conditionalParse(this.inputName, invocation));

        if (oInput == null) {
            String msg = ("Can not find a java.io.InputStream with the name [" + this.inputName + "] in the invocation stack. " +
                "Check the <param name=\"inputName\"> tag specified for this action.");
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Find the Response in context
        HttpServletResponse oResponse = (HttpServletResponse) invocation.getInvocationContext().get(HTTP_RESPONSE);

        // Set the content type
//        oResponse.setContentType(getProperty("contentType", this.contentType, invocation));

        // Set the content length
        if (this.contentLength != null) {
          String _contentLength = conditionalParse(this.contentLength, invocation);
          int _contentLengthAsInt = -1;
          try {
            _contentLengthAsInt = Integer.parseInt(_contentLength);
            if (_contentLengthAsInt >= 0) {
                oResponse.setContentLength(_contentLengthAsInt);
              }
          }
          catch(NumberFormatException e) {
            log.warn("failed to recongnize "+_contentLength+" as a number, contentLength header will not be set", e);
          }
        }

        // Set the content-disposition
        if (this.contentDisposition != null) {
//            oResponse.addHeader("Content-disposition", getProperty("contentDisposition", this.contentDisposition, invocation));
        }

        // Get the outputstream
        oOutput = oResponse.getOutputStream();

        if (log.isDebugEnabled()) {
            log.debug("Streaming result [" + this.inputName + "] type=[" + this.contentType + "] length=[" + this.contentLength +
                "] content-disposition=[" + this.contentDisposition + "]");
        }

        // Copy input to output
        log.debug("Streaming to output buffer +++ START +++");
        byte[] oBuff = new byte[this.bufferSize];
        int iSize;
        while (-1 != (iSize = oInput.read(oBuff))) {
            oOutput.write(oBuff, 0, iSize);
        }
        log.debug("Streaming to output buffer +++ END +++");

        // Flush
        oOutput.flush();
    }
    finally {
        if (oInput != null) oInput.close();
        if (oOutput != null) oOutput.close();
    }
  }

  private String getProperty(final String propertyName, final String param, final ActionInvocation invocation) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final Object action = invocation.getAction();
    final String methodName = "get" + propertyName.substring(0,1).toUpperCase() + propertyName.substring(1);
    final Method method = action.getClass().getMethod(methodName);
    final Object o = method.invoke(action);
    final String propertyValue = o.toString();
    if (null == propertyValue) {
      return conditionalParse(param, invocation);
    }

    return propertyValue;
  }
}
