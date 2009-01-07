/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.ambra.struts2;

import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Action;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Required;

import java.io.Serializable;

/**
 * Struts interceptor will wrap read-only transaction around actions that implement
 * OtmTransactionAware interface.
 *
 * Use when you want transaction to span beyond your action method into result.
 *
 * @author Dragisa Krsmanovic
 */
public class TransactionInterceptor extends AbstractInterceptor {

  private TransactionalActionInvoker txActionInvoker;

  public String intercept(ActionInvocation actionInvocation) throws Exception {
    Action action = (Action) actionInvocation.getAction();

    if (action instanceof TransactionAware) {
      return txActionInvoker.invoke(actionInvocation);
    } else {
      return actionInvocation.invoke();
    }
  }

  /**
   * Spring setter method. Sets external class for invoking Transactional method.
   *
   * @param txActionInvoker Class that has transactional method for invoking Struts action.
   */
  @Required
  public void setTxActionInvoker(TransactionalActionInvoker txActionInvoker) {
    this.txActionInvoker = txActionInvoker;
  }

  /**
   * Spring managed bean that allows actionInvocation.invoke() to be invoked withing transaction.
   *
   * @author Dragisa Krsmanovic
   */
  public static class TransactionalActionInvoker implements Serializable {

    /**
     * Invoke action inside a transaction.
     * @param actionInvocation Struts Interceptor action invocation object.
     * @return Struts result.
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public String invoke(ActionInvocation actionInvocation) throws Exception {
      return actionInvocation.invoke();
    }
  }
}
