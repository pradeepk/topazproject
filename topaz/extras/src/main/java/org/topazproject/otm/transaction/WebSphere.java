/*
 * Copyright 2004-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.otm.transaction;

import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.OtmException;

/**
 * TransactionManager lookup strategy for WebSphere (versions 4, 5.0 and 5.1)
 * 
 * @author kimchy
 */
public class WebSphere implements TransactionManagerLookup {
  private static final Log log = LogFactory.getLog(WebSphere.class);

  private int version;

  public TransactionManager getTransactionManager() throws OtmException {
    try {
      Class clazz;
      try {
        clazz = TransactionManagerLookupFactory.forName(
                                              "com.ibm.ws.Transaction.TransactionManagerFactory");
        version = 5;
        log.debug("Found WebSphere 5.1+");
      } catch (Exception e) {
        try {
          clazz = TransactionManagerLookupFactory.forName(
                                              "com.ibm.ejs.jts.jta.TransactionManagerFactory");
          version = 5;
          log.debug("Found WebSphere 5.0");
        } catch (Exception e2) {
          clazz = TransactionManagerLookupFactory.forName("com.ibm.ejs.jts.jta.JTSXA");
          version = 4;
          log.debug("Found WebSphere 4");
        }
      }

      return (TransactionManager) clazz.getMethod("getTransactionManager", null).invoke(null, null);
    } catch (Exception e) {
      throw new OtmException("Could not obtain WebSphere JTSXA instance", e);
    }
  }

  public String getUserTransactionName() {
    return version == 5 ? "java:comp/UserTransaction" : "jta/usertransaction";
  }
}
