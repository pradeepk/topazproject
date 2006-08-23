<html>
    <head>
        <title>Welcome to Plosone</title>
    </head>
    <body>
        <h1>Welcome to the Plosone webapp</h1>
        <legend>Messages</legend>

          <fieldset>
            <p>
              <#list actionMessages as message>
              ${message} <br/>
            </#list>
            </p>
          </fieldset>


            <fieldset>
                <legend>A few things for you to do</legend>
                <p>
                    <@ww.url id="articleListURL"  namespace="/article" action="articleList" />
                    <@ww.a href="%{articleListURL}">View Articles</@ww.a>
                </p>

                <p>
                    <@ww.url id="createAnnotationURL" namespace="/annotation" action="createAnnotation" />
                    <@ww.a href="%{createAnnotationURL}">Create Annotation</@ww.a>
                </p>

                <p>
                    <@ww.url id="listAnnotationURL" namespace="/annotation" action="listAnnotation">
                      <@ww.param name="target" value="%{'http://here.is/viru'}"/>
                    </@ww.url>
                    <@ww.a href="%{listAnnotationURL}">List Annotations</@ww.a>
                </p>

            </fieldset>
    </body>
</html>
