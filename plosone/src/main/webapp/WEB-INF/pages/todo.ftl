<html>
    <head>
        <title>Welcome</title>
    </head>
    <body>
        <h1>Welcome to the Plosone webapp</h1>

        <p>
            <fieldset>
                <legend>A few things for you to do</legend>
                <p>
                    <@ww.url id="articleListURL" action="articleList" />
                    <@ww.a href="%{articleListURL}">View Articles</@ww.a>
                </p>

                <p>
                    <@ww.url id="createAnnotationURL" action="createAnnotation" />
                    <@ww.a href="%{createAnnotationURL}">Create Annotation</@ww.a>
                </p>

                <p>
                    <@ww.url id="createAnnotationURL" action="createAnnotation" />
                    <@ww.a href="%{createAnnotationURL}">Create Annotation</@ww.a>
                </p>

            </fieldset>
        </p>
    </body>
</html>
