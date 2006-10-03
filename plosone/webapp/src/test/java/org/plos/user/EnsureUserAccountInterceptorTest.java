/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.Result;
import com.opensymphony.xwork.interceptor.PreResultListener;
import com.opensymphony.xwork.mock.MockActionInvocation;
import com.opensymphony.xwork.util.OgnlValueStack;
import org.plos.ApplicationException;
import org.plos.BasePlosoneTestCase;
import org.plos.user.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class EnsureUserAccountInterceptorTest extends BasePlosoneTestCase {

  public void testShouldForwardToCreateNewAccount() throws Exception {
    final EnsureUserAccountInterceptor interceptor = new EnsureUserAccountInterceptor();
    final MockActionInvocation actionInvocation = new MockActionInvocation();

    final Map<String, Object> map = new HashMap<String, Object>();
    map.put(Constants.PLOS_ONE_USER_KEY, "ASDASDASD12312313EDB");
    map.put("plosUser", null);
    ActionContext.getContext().setSession(map);

    interceptor.setUserService(getUserService());

    actionInvocation.setAction(null);
    final String result = interceptor.intercept(actionInvocation);
    assertEquals(Constants.ReturnCode.NEW_PROFILE, result);
  }

  public void testShouldForwardToUpdateNewAccount() throws Exception {
    final EnsureUserAccountInterceptor interceptor = new EnsureUserAccountInterceptor();
    final MockActionInvocation actionInvocation = new MockActionInvocation();

    final Map<String, Object> map = new HashMap<String, Object>();
    final String GUID = "ASDASDASD12312313EDB";
    map.put(Constants.PLOS_ONE_USER_KEY, GUID);

    ActionContext.getContext().setSession(map);

    final UserService mockUserService = new UserService() {
      public PlosOneUser getUserByAuthId(final String guid) throws ApplicationException {
        final PlosOneUser plosOneUser = new PlosOneUser(guid);
        plosOneUser.setUserId("topazId");
        plosOneUser.setEmail("viru@home.com");
        plosOneUser.setDisplayName(null); //Display name is not set
        plosOneUser.setRealName("virender");
        return plosOneUser;
      }
    };
    interceptor.setUserService(mockUserService);

    actionInvocation.setAction(null);
    final String result = interceptor.intercept(actionInvocation);
    assertEquals(Constants.ReturnCode.UPDATE_PROFILE, result);

  }

  public void testShouldForwardToOriginalAction() throws Exception {
    final EnsureUserAccountInterceptor interceptor = new EnsureUserAccountInterceptor();
    final String actionCalledStatus = "actionCalled";

    final ActionInvocation actionInvocation = new ActionInvocation() {
      public Object getAction() {return null;}
      public boolean isExecuted() {return false; }
      public ActionContext getInvocationContext() {return null;}
      public ActionProxy getProxy() {return null;}
      public Result getResult() throws Exception {return null;}
      public String getResultCode() {return null;}
      public void setResultCode(String string) {}
      public OgnlValueStack getStack() {return null;}
      public void addPreResultListener(PreResultListener preResultListener) {}
      public String invoke() throws Exception {
        return actionCalledStatus;
      }
      public String invokeActionOnly() throws Exception {return null;}
    };

    final Map<String, Object> map = new HashMap<String, Object>();
    final String GUID = "ASDASDASD12312313EDB";
    map.put(Constants.PLOS_ONE_USER_KEY, GUID);
    ActionContext.getContext().setSession(map);

    final UserService mockUserService = new UserService() {
      public PlosOneUser getUserByAuthId(final String guid) throws ApplicationException {
        final PlosOneUser plosOneUser = new PlosOneUser(guid);
        plosOneUser.setUserId("topazId");
        plosOneUser.setEmail("viru@home.com");
        plosOneUser.setDisplayName("Viru");  //Display name is already set
        plosOneUser.setRealName("virender");
        return plosOneUser;
      }
    };

    interceptor.setUserService(mockUserService);

    final String result = interceptor.intercept(actionInvocation);
    assertEquals(actionCalledStatus, result);
  }

}
