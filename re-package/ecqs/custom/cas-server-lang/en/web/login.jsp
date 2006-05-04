<%@ include file="header.jsp" %>

    <script language="JavaScript" type="text/javascript">
      function myFocus(){
        document.login_form.username.focus();
      }
    </script>
    
    <font face="${cas-server.html.font}">
      <p>
        <% if (request.getAttribute("edu.yale.its.tp.cas.badUsernameOrPassword") != null) { %>
          <font color="${cas-server.html.error.color}">
            Sorry, you entered an invalid NetID or password. <br />
            Please try again. 
          </font>
        <% } else if (request.getAttribute("edu.yale.its.tp.cas.service") == null) { %>
            You may authenticate now in order to access protected
            services later.
        <% } else if (request.getAttribute("edu.yale.its.tp.cas.badLoginTicket") != null) { %>
          <%-- BAD LOGIN TICKET --%>
        <% } else { %>
          You have requested access to a site that requires authentication.
        <% } %>
      </p>
    </font>
    
    <font face="${cas-server.html.font}">
      <p>
        Enter your NetID and password below; then click on the <b>Login</b>
        button to continue.
      </p>
    </font>
    
    <table bgcolor="${cas-server.html.credentials.color}" align="center">
      <tr>
        <td>
          <form method="post" name="login_form">
            <table border="0" cellpadding="0" cellspacing="5">
              <tr>
                <td nowrap>
                  <font face="${cas-server.html.font}">
                    <b>NetID:</b>
                  </font>
                </td>
                <td>
                  <input type="text" name="username" maxlength="80" autocomplete="off">
                </td>
              </tr>
              <tr>
                <td nowrap>
                  <font face="${cas-server.html.font}">
                    <b>Password:</b>
                  </font>
                </td>
                <td><input type="password" name="password" autocomplete="off"></td>
              </tr>
              <tr>
                <td colspan="2" align="left">
                  <input type="checkbox" name="warn" value="true" />
                  <small>
                    <small>Warn me before logging me in to other sites.</small>
                  </small>
                </td>
              </tr>
              <tr>
                <td colspan="2" align="right">
                  <input type="hidden" name="lt" value="<%= request.getAttribute("edu.yale.its.tp.cas.lt") %>" />
                  <input type="submit" value="Login">
                </td>
              </tr>
            </table>
          </form>
        </td>
      </tr>
    </table>
  </td>
</tr>
<tr>
  <td colspan="2">
    <center>
      <font color="${cas-server.html.error.color}" face="${cas-server.html.font}">
        <i>
          <b>
            For security reasons, quit your web browser when you are done
accessing services that require authentication!
          </b>
        </i>
      </font>
    </center>
  </td>
</tr>
<tr>
  <td colspan="2">
    <p>
      <font face="${cas-server.html.font}" size="1">
        Be wary of any program or web page that asks you for your NetID and
        password. ${cas-server.html.institution.web-pages} that ask you for your NetID and password
        will generally have URLs that begin with ${cas-server.html.institution.url-pattern}.
        In addition, your browser should visually
        indicate that you are accessing a secure page.
      </font>
    </p>
    <p>
      <a target="new" href="${cas-server.html.institution.url}">${cas-server.html.institution.name}</a>
    </p>
    <script language="JavaScript" type="text/javascript">
      myFocus();
    </script>

<%@ include file="footer.jsp" %>
