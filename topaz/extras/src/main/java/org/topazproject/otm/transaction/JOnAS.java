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

import org.topazproject.otm.OtmException;

/**
 * TransactionManager lookup strategy for JOnAS
 * 
 * @author kimchy
 */
public class JOnAS implements TransactionManagerLookup {
  public TransactionManager getTransactionManager() throws OtmException {
    try {
      Class clazz = TransactionManagerLookupFactory.forName("org.objectweb.jonas_tm.Current");
      return (TransactionManager) clazz.getMethod("getTransactionManager", null).invoke(null, null);
    } catch (Exception e) {
      throw new OtmException("Could not obtain JOnAS transaction manager instance", e);
    }
  }

  public String getUserTransactionName() {
    return "java:comp/UserTransaction";
  }
}
