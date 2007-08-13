<?xml version="1.0"?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY space "&#32;">
        <!ENTITY nbsp "&#160;">
        <!-- either '&#8470;' for No, or '#' -->
        <!ENTITY no "#">
        ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:file="java.io.File"
                xmlns:redirect="http://xml.apache.org/xalan/redirect"
                extension-element-prefixes="file redirect"
        >
    <xsl:output method="html" encoding="us-ascii" indent="yes"
                doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
                doctype-system="http://www.w3.org/TR/html4/loose.dtd"
            />

    <!-- Parameter passed from ant with the creation time -->
    <xsl:param name="reporttime"/>
    <xsl:param name="title" select="'WebTest'"/>

    <!-- Customization hook for site-specific differences -->
    <!-- path to the deployed lastResponse_XXX.html; absolute, or relative to the WebTestReport.html -->
    <xsl:param name="responses.dir"/>
    <!-- path to the deployed resources (css, ...); absolute, or relative to the WebTestReport.html -->
    <xsl:param name="resources.dir" select="'.'"/>
    <xsl:variable name="company.logo.alt" select="'WebTest'"/>
    <xsl:param name="indexTests" select="'WebTestReport.html'"/>
    <xsl:param name="singleReportsMode" select="'embedded'"/>
    <!-- 'external' or 'embedded' -->
    <!-- for 'external' mode: where the single reports should be written -->
    <xsl:param name="outputDir" select="'../reports'"/>


    <!-- global variable -->
    <xsl:variable name="duration.total"
                  select="sum(/summary/testresult/results/step/result/node()[name()='completed' or name()='failed']/@duration)"/>

    <xsl:variable name="img.ok" select="concat($resources.dir, '/images/ok.gif')"/>
    <xsl:variable name="img.todo" select="concat($resources.dir, '/images/todo.gif')"/>
    <xsl:variable name="img.optional" select="concat($resources.dir, '/images/optional.gif')"/>
    <xsl:variable name="img.expandall" select="concat($resources.dir, '/images/expandall.png')"/>
    <xsl:variable name="img.collapseall" select="concat($resources.dir, '/images/collapseall.png')"/>
    <xsl:variable name="img.expandPlus" select="concat($resources.dir, '/images/expandPlus.png')"/>
    <xsl:variable name="img.canoo" select="concat($resources.dir, '/images/canoo.gif')"/>

	<xsl:variable name="webtestVersion" select="/summary/@Implementation-Version"/>

    <xsl:template match="/">
        <!-- HTML prefix -->
        <html lang="en">
            <head>
                <title>
                    <xsl:value-of select="$title"/>
                    <xsl:text>&space;- Test Result</xsl:text>
                    <xsl:if test="$singleReportsMode='external'">&space;Overview</xsl:if>
                </title>
                <meta http-equiv="content-style-type" content="text/css"/>
                <link rel="stylesheet" type="text/css" href="{$resources.dir}/report.css"/>
                <script type="text/javascript" src="{$resources.dir}/showHide.js"></script>
                <script type="text/javascript" src="{$resources.dir}/sorttable.js"></script>
                <script type="text/javascript" src="{$resources.dir}/responseBrowser.js"
                        id="scriptResponseBrowser"></script>
            </head>

            <!-- ###################################################################### -->
            <!-- tj: hiding all substeps on load -->
            <body onload="hideAllSubstepsOnPage('expandall.png','collapseall.png')">
                <div class="header">
                    <img src="{$resources.dir}/images/webtest.jpg" alt="{$company.logo.alt}"/>
                    <br/>
                    <xsl:value-of select="$title"/>
                    <xsl:text>&nbsp;&nbsp;&nbsp;&nbsp;Tests started at&space;</xsl:text>
                    <xsl:value-of select="/summary/testresult[1]/@starttime"/>
                </div>

                <!-- Header and summary table -->
                <xsl:call-template name="StepStatistics"/>

                <xsl:call-template name="OverviewTable"/>
                <xsl:if test="$singleReportsMode = 'embedded' and .//step//step">
                    <p>
                        <xsl:text>Expand/Collapse nested steps for all tests:&space;</xsl:text>
                        <img onclick="showAllSubsteps(document)" src="{$img.expandall}"
                             class="withPointer"
                             alt="show all nested steps in document" title="show all nested steps in document"/>
                        <xsl:text>&space;</xsl:text>
                        <img onclick="hideAllSubsteps(document)" src="{$img.collapseall}"
                             class="withPointer"
                             alt="hide all nested steps in document" title="hide all nested steps in document"/>
                    </p>
                </xsl:if>

                <!-- All individual test results -->
                <xsl:apply-templates/>

                <!-- Footer & fun -->
                <hr/>

			<table width="100%">
			<tr>
			<td valign="bottom">
                <xsl:text>Created using&space;</xsl:text>
                <a href="http://webtest.canoo.com">
                    <xsl:value-of select="/summary/@Implementation-Title"/>
                </a>
                <xsl:text>&space;(</xsl:text>
                <xsl:value-of select="$webtestVersion"/>
                <xsl:text>). Report created at&space;</xsl:text>
                <xsl:value-of select="$reporttime"/>
			</td>
			<td align="right">
			<!-- remove this line if you want to avoid outgoing requests when looking at the results -->
			<img alt="WebTest" src="http://webtest.canoo.com/webtest/manual/images/webtest.jpg?build={$webtestVersion}"/>
			<xsl:text> is an Open Source project founded and hosted by </xsl:text>
			<a href="http://www.canoo.com"><img alt="Canoo" src="{$img.canoo}"/></a>
			</td>
			</tr>
			</table>
                <!-- HTML postfix -->
            </body>
        </html>

    </xsl:template>

    <xsl:template name="OverviewTable">
        <h4>
            <a name="overview">
                <xsl:text>Test Scenario Overview (</xsl:text>
                <xsl:call-template name="time">
                    <xsl:with-param name="msecs" select="$duration.total"/>
                </xsl:call-template>
                <xsl:text>)</xsl:text>
            </a>
        </h4>

        <!--
                        Create summary table entries choosing the td-class depending on successful yes/no
                        and create a link to the appropriate detail section (e.g. #testspec1).
                    -->
        <table cellpadding="5" border="0" cellspacing="0" width="100%" class="sortable">
            <thead>
                <tr>
                    <th rowspan="2">&no;</th>
                    <th rowspan="2">Result</th>
                    <th rowspan="2">Name</th>
                    <th rowspan="2" title="Number of successful executed steps / Total number of steps"># Steps</th>
                    <th colspan="3">Timing profile</th>
                    <th rowspan="2" style="white-space: nowrap">Failing step</th>
                </tr>
                <tr>
                    <th>Duration</th>
                    <th>%</th>
                    <th>Graph</th>
                </tr>
            </thead>
            <tbody>
                <xsl:apply-templates select="/summary/testresult" mode="summary"/>
            </tbody>
        </table>
    </xsl:template>

    <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
    <xsl:template name="StepStatistics_tests">
        <xsl:variable name="webtests.total" select="count(/summary/testresult)"/>
        <xsl:variable name="webtests.ok" select="count(/summary/testresult[@successful='yes'])"/>
        <xsl:variable name="webtests.failed" select="count(/summary/testresult[@successful='no'])"/>

        <thead>
            <tr>
                <th>WebTests</th>
                <th align="right">#</th>
                <th align="right">%</th>
                <th>Graph</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td class="light">
                    <img src="{$img.ok}" alt="ok"/>
                </td>
                <td class="light" align="right">
                    <xsl:value-of select="$webtests.ok"/>
                </td>
                <td class="light" align="right">
                    <xsl:value-of select="round($webtests.ok * 100 div $webtests.total)"/>
                </td>
                <xsl:call-template name="colorBar">
                    <xsl:with-param name="percentage" select="$webtests.ok * 100 div $webtests.total"/>
                    <xsl:with-param name="color" select="'lightgreen'"/>
                    <xsl:with-param name="title" select="'Successful WebTests'"/>
                </xsl:call-template>
            </tr>
            <tr>
                <td class="light">
                    <img src="{$img.todo}" alt="x"/>
                </td>
                <td class="light" align="right">
                    <xsl:value-of select="$webtests.failed"/>
                </td>
                <td class="light" align="right">
                    <xsl:value-of select="round($webtests.failed * 100 div $webtests.total)"/>
                </td>
                <xsl:call-template name="colorBar">
                    <xsl:with-param name="percentage" select="$webtests.failed * 100 div $webtests.total"/>
                    <xsl:with-param name="color" select="'red'"/>
                    <xsl:with-param name="title" select="'Failed WebTests'"/>
                </xsl:call-template>
            </tr>
        </tbody>
        <tbody>
            <tr>
                <td class="light">
                    <b>Sum</b>
                </td>
                <td class="light" align="right">
                    <b>
                        <xsl:text>&nbsp;</xsl:text>
                        <xsl:value-of select="$webtests.total"/>
                    </b>
                </td>
                <td class="light" align="right">
                    <b>&nbsp;100</b>
                </td>
                <td class="light">&nbsp;</td>
            </tr>
        </tbody>
    </xsl:template>

    <xsl:template name="StepStatistics_steps">
        <xsl:variable name="steps.total" select="count(//results/step)"/>
        <xsl:variable name="steps.ok" select="count(//results/step/result/completed)"/>
        <xsl:variable name="steps.failed" select="count(//results/step/result/failed)"/>
        <xsl:variable name="steps.else" select="count(//results/step/result/notexecuted)"/>

        <thead>
            <tr>
                <th>Steps</th>
                <th align="right">#</th>
                <th align="right">%</th>
                <th>Graph</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td class="light">
                    <img src="{$img.ok}" alt="ok"/>
                </td>
                <td class="light" align="right">
                    <xsl:value-of select="$steps.ok"/>
                </td>
                <td class="light" align="right">
                    <xsl:value-of select="round($steps.ok * 100 div $steps.total)"/>
                </td>
                <xsl:call-template name="colorBar">
                    <xsl:with-param name="percentage" select="$steps.ok * 100 div $steps.total"/>
                    <xsl:with-param name="color" select="'lightgreen'"/>
                    <xsl:with-param name="title" select="'Successful steps'"/>
                </xsl:call-template>
            </tr>
            <tr>
                <td class="light">
                    <img src="{$img.todo}" alt="x"/>
                </td>
                <td class="light" align="right">
                    <xsl:value-of select="$steps.failed"/>
                </td>
                <td class="light" align="right">
                    <xsl:value-of select="round($steps.failed * 100 div $steps.total)"/>
                </td>
                <xsl:call-template name="colorBar">
                    <xsl:with-param name="percentage" select="$steps.failed * 100 div $steps.total"/>
                    <xsl:with-param name="color" select="'red'"/>
                    <xsl:with-param name="title" select="'Failed steps'"/>
                </xsl:call-template>
            </tr>
            <tr>
                <td class="light">
                    <img src="{$img.optional}" alt="o"/>
                </td>
                <td class="light" align="right">
                    <xsl:value-of select="$steps.else"/>
                </td>
                <td class="light" align="right">
                    <xsl:value-of select="round($steps.else * 100 div $steps.total)"/>
                </td>
                <xsl:call-template name="colorBar">
                    <xsl:with-param name="percentage" select="$steps.else * 100 div $steps.total"/>
                    <xsl:with-param name="color" select="'yellow'"/>
                    <xsl:with-param name="title" select="'Skipped steps'"/>
                </xsl:call-template>
            </tr>
        </tbody>
        <tbody>
            <tr>
                <td class="light">
                    <b>Sum</b>
                </td>
                <td class="light" align="right">
                    <b>
                        <xsl:text>&nbsp;</xsl:text>
                        <xsl:value-of select="$steps.total"/>
                    </b>
                </td>
                <td class="light" align="right">
                    <b>&nbsp;100</b>
                </td>
                <td class="light">&nbsp;</td>
            </tr>
        </tbody>
    </xsl:template>

    <xsl:template name="StepStatistics_serverRoundtrips">
        <h4>Server Roundtrip Timing Profile</h4>
        <!--        ================================ server roundtrip statistic table =============================    -->

        <xsl:variable name="duration.steps"
                      select="//step[parameter[@name='taskName'][starts-with(@value, 'sqlunit') or @value='invoke' or @value='clickLink' or @value='clickButton' or @value='clickElement']]/result/node()[name()='completed' or name()='failed']"/>

        <xsl:variable name="steps.last" select="count($duration.steps[@duration > 30000])"/>
        <xsl:variable name="steps.fourth" select="count($duration.steps[@duration > 10000][30000 >= @duration])"/>
        <xsl:variable name="steps.third" select="count($duration.steps[@duration > 5000][100000 >= @duration])"/>
        <xsl:variable name="steps.second" select="count($duration.steps[@duration > 3000][50000 >= @duration])"/>
        <xsl:variable name="steps.first" select="count($duration.steps[@duration > 1000][3000 >= @duration])"/>
        <xsl:variable name="steps.begin" select="count($duration.steps[1000 >= @duration])"/>

        <table cellpadding="3" border="0" cellspacing="0" width="100%">
            <thead>
                <tr>
                    <th align="center">Secs</th>
                    <th align="right">#</th>
                    <th align="right">%</th>
                    <th>Histogram</th>
                </tr>
            </thead>
            <tfoot>
                <tr>
                    <td class="light">
                        <b>Sum</b>
                    </td>
                    <td class="light" align="right">
                        <b>
                            <xsl:text>&nbsp;</xsl:text>
                            <xsl:value-of select="count($duration.steps)"/>
                        </b>
                    </td>
                    <td class="light" align="right">
                        <b>&nbsp;100</b>
                    </td>
                    <td class="light">&nbsp;</td>
                </tr>
                <tr>
                    <td class="light">
                        <b>Avg</b>
                    </td>
                    <td class="light" align="right">
                        &nbsp;
                    </td>
                    <td class="light" align="right">
                        &nbsp;
                    </td>
                    <td class="light">
                        <b>
                            <xsl:value-of select="round(sum($duration.steps/@duration) div count($duration.steps))"/>
                            <xsl:text> ms</xsl:text>
                        </b>
                    </td>
                </tr>
            </tfoot>
            <tbody>
                <xsl:call-template name="histogramRow">
                    <xsl:with-param name="label">0 - 1</xsl:with-param>
                    <xsl:with-param name="steps" select="$steps.begin"/>
                    <xsl:with-param name="duration.steps" select="$duration.steps"/>
                </xsl:call-template>

                <xsl:call-template name="histogramRow">
                    <xsl:with-param name="label">1 - 3</xsl:with-param>
                    <xsl:with-param name="steps" select="$steps.first"/>
                    <xsl:with-param name="duration.steps" select="$duration.steps"/>
                </xsl:call-template>

                <xsl:call-template name="histogramRow">
                    <xsl:with-param name="label">3 - 5</xsl:with-param>
                    <xsl:with-param name="steps" select="$steps.second"/>
                    <xsl:with-param name="duration.steps" select="$duration.steps"/>
                </xsl:call-template>

                <xsl:call-template name="histogramRow">
                    <xsl:with-param name="label">5 - 10</xsl:with-param>
                    <xsl:with-param name="steps" select="$steps.third"/>
                    <xsl:with-param name="duration.steps" select="$duration.steps"/>
                </xsl:call-template>

                <xsl:call-template name="histogramRow">
                    <xsl:with-param name="label">10 - 30</xsl:with-param>
                    <xsl:with-param name="steps" select="$steps.fourth"/>
                    <xsl:with-param name="duration.steps" select="$duration.steps"/>
                </xsl:call-template>

                <xsl:call-template name="histogramRow">
                    <xsl:with-param name="label">&gt; 30</xsl:with-param>
                    <xsl:with-param name="steps" select="$steps.last"/>
                    <xsl:with-param name="duration.steps" select="$duration.steps"/>
                </xsl:call-template>
            </tbody>
        </table>
    </xsl:template>

    <xsl:template name="StepStatistics_results">
        <h4>Result Summary</h4>
        <table cellpadding="3" border="0" cellspacing="0" width="100%">
            <xsl:call-template name="StepStatistics_tests"/>
            <xsl:call-template name="StepStatistics_steps"/>
        </table>
    </xsl:template>

    <xsl:template name="StepStatistics">
        <table cellpadding="0" border="0" cellspacing="2" width="100%">
            <tr>
                <td valign="top" width="50%">
                    <xsl:call-template name="StepStatistics_results"/>
                </td>
                <td valign="top">
                    <xsl:call-template name="StepStatistics_serverRoundtrips"/>
                </td>
            </tr>
        </table>
    </xsl:template>

    <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
    <xsl:template name="histogramRow">
        <xsl:param name="label"/>
        <xsl:param name="steps"/>
        <xsl:param name="duration.steps"/>
        <tr>
            <td class="light" nowrap="nowrap" align="center">
                <xsl:value-of select="$label"/>
            </td>
            <td class="light" align="right">
                <xsl:value-of select="$steps"/>
            </td>
            <td class="light" align="right">
                <xsl:value-of select="round($steps * 100 div count($duration.steps))"/>
            </td>
            <xsl:call-template name="colorBar">
                <xsl:with-param name="percentage" select="$steps * 100 div count($duration.steps)"/>
                <xsl:with-param name="color" select="'lightblue'"/>
            </xsl:call-template>
        </tr>
    </xsl:template>

    <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
    <xsl:template name="colorBar">
        <xsl:param name="percentage"/>
        <xsl:param name="color"/>
        <xsl:param name="title"/>
        <xsl:param name="width" select="'80%'"/>

        <td width="{$width}" class="light">
            <xsl:if test="$percentage > 0">
                <div class="colorBar" style="width: {$percentage}%; background: {$color};">
                    <xsl:if test="$title">
                        <xsl:attribute name="title">
                            <xsl:value-of select="$title"/>
                        </xsl:attribute>
                    </xsl:if>
                </div>
            </xsl:if>
        </td>
    </xsl:template>

    <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
    <xsl:template name="time">
        <xsl:param name="msecs"/>
        <xsl:param name="detail"/>

        <xsl:choose>
            <xsl:when test="$msecs > 5000">
                <xsl:variable name="base" select="round($msecs div 1000)"/>
                <xsl:variable name="hours" select="floor($base div 3600)"/>
                <xsl:variable name="mins" select="floor(($base - $hours*3600) div 60)"/>
                <xsl:variable name="secs" select="floor(($base - $hours*3600) - $mins*60)"/>

                <xsl:if test="10 > $hours">0</xsl:if>
                <xsl:value-of select="$hours"/>
                <xsl:text>:</xsl:text>
                <xsl:if test="10 > $mins">0</xsl:if>
                <xsl:value-of select="$mins"/>
                <xsl:text>:</xsl:text>
                <xsl:if test="10 > $secs">0</xsl:if>
                <xsl:value-of select="$secs"/>

                <xsl:if test="$detail">
                    <xsl:text>&space;(</xsl:text>
                    <xsl:value-of select="$msecs"/>
                    <xsl:text>&space;ms)</xsl:text>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$msecs"/>
                <xsl:if test="$detail">&space;ms</xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="testresult[@successful='no']" mode="successIndicator">
        <a>
            <xsl:attribute name="onclick">showTargetStep(this)</xsl:attribute>

            <xsl:attribute name="href">
                <xsl:if test="$singleReportsMode = 'external'">
                    <xsl:apply-templates select="current()" mode="indexedFileName"/>
                </xsl:if>
                <xsl:text>#testspec</xsl:text>
                <xsl:number/>
                <xsl:text>-error</xsl:text>
            </xsl:attribute>
            <img src="{$img.todo}" alt="x"/>
        </a>
    </xsl:template>

    <xsl:template match="testresult[@successful='yes']" mode="successIndicator">
        <img src="{$img.ok}" alt="ok"/>
    </xsl:template>

    <xsl:template match="testresult" mode="indexedFileName">
        <xsl:text>File</xsl:text>
        <xsl:number/>
        <xsl:text>.html</xsl:text>
    </xsl:template>
    <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
    <xsl:template match="testresult" mode="summary">
        <tr>
            <td class="light" align="right">
                <xsl:number/>
            </td>
            <td class="light" align="center">
                <xsl:apply-templates select="." mode="successIndicator"/>
            </td>
            <td class="light" nowrap="nowrap">
                <a>
                    <xsl:choose>
                        <xsl:when test="$singleReportsMode = 'external'">
                            <xsl:attribute name="href">
                                <xsl:apply-templates select="current()" mode="indexedFileName"/>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="href">
                                <xsl:text>#testspec</xsl:text>
                                <xsl:number/>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:value-of select="@testspecname"/>
                </a>
            </td>
            <td class="light" align="right" style="white-space: nowrap">
                <xsl:value-of select="count(.//step/result/completed)"/>
                <xsl:text> / </xsl:text>
                <xsl:value-of select="count(.//step)"/>
            </td>
            <xsl:variable name="actual.testspec.duration"
                          select="sum(results/step/result/node()[name()='completed' or name()='failed']/@duration)"/>
            <td class="light" align="right" nowrap="nowrap">
                <xsl:call-template name="time">
                    <xsl:with-param name="msecs" select="$actual.testspec.duration"/>
                </xsl:call-template>
            </td>
            <td class="light" align="right">
                <xsl:value-of select="round($actual.testspec.duration * 100 div $duration.total)"/>
            </td>

            <xsl:call-template name="colorBar">
                <xsl:with-param name="percentage" select="$actual.testspec.duration * 100 div $duration.total"/>
                <xsl:with-param name="color" select="'lightblue'"/>
                <xsl:with-param name="width" select="'40%'"/>
            </xsl:call-template>

            <td class="light"> <!--  the failing top step, if any -->
                <xsl:if test="results/step[result/failed]">
                    <b>
                        <xsl:value-of select="results/step[result/failed]/parameter[@name='taskName']/@value"/>
                    </b>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="results/step[result/failed]/parameter[@name='description']/@value"/>
                </xsl:if>
            </td>
        </tr>
    </xsl:template>

    <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
    <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
    <xsl:template match="testresult">
        <xsl:choose>
            <xsl:when test="$singleReportsMode = 'external'">
                <xsl:apply-templates select="." mode="external"/>
            </xsl:when>
            <xsl:when test="$singleReportsMode = 'embedded'">
                <hr/>
                <xsl:apply-templates select="." mode="embedded"/>
                <!-- Create link back to overview (top) -->
                <p>
                    <a href="#overview">Back to Test Report Overview</a>
                </p>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="testresult" mode="external">
        <xsl:variable name="filename">
            <xsl:value-of select="concat($outputDir, '/')"/>
            <xsl:apply-templates select="current()" mode="indexedFileName"/>
        </xsl:variable>

        <!-- Create file only if doesn't already exist (it's only the case if a single webtest is executed and the result appended to the existing ones) -->
        <xsl:variable name="theFile" select="file:new($filename)"/>
        <xsl:if test="not(file:exists($theFile))">
            <redirect:write file="{$filename}">
                <html lang="en">
                    <head>
                        <title>
                            <xsl:text>Test results for: </xsl:text>
                            <xsl:value-of select="@testspecname"/>
                        </title>
                        <link rel="contents up" title="Test Report Overview" href="{$indexTests}"/>
                        <link rel="start first" title="First test Report">
                            <xsl:attribute name="href">
                                <xsl:apply-templates select="../testresult[1]" mode="indexedFileName"/>
                            </xsl:attribute>
                        </link>
                        <xsl:if test="preceding-sibling::testresult">
                            <link rel="prev" title="Previous result">
                                <xsl:attribute name="href">
                                    <xsl:apply-templates select="preceding-sibling::testresult[1]"
                                                         mode="indexedFileName"/>
                                </xsl:attribute>
                            </link>
                        </xsl:if>
                        <xsl:if test="following-sibling::testresult">
                            <link rel="next" title="Next result">
                                <xsl:attribute name="href">
                                    <xsl:apply-templates select="following-sibling::testresult[1]"
                                                         mode="indexedFileName"/>
                                </xsl:attribute>
                            </link>
                        </xsl:if>
                        <link rel="stylesheet" type="text/css" href="{$resources.dir}/report.css"/>
                        <script type="text/javascript" src="{$resources.dir}/showHide.js"></script>
                        <script type="text/javascript" src="{$resources.dir}/responseBrowser.js"
                                id="scriptResponseBrowser"></script>
                    </head>

                    <body onload="hideAllSubstepsOnPage('expandall.png','collapseall.png')">
                        <xsl:apply-templates select="." mode="embedded"/>
                        <br/>
                        <div style="text-align: center">
                            <xsl:choose>
                                <xsl:when test="preceding-sibling::testresult[@successful='no']">
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:apply-templates
                                                    select="(preceding-sibling::testresult[@successful='no'])[1]"
                                                    mode="indexedFileName"/>
                                        </xsl:attribute>
                                        <xsl:text>Previous error</xsl:text>
                                    </a>
                                </xsl:when>
                                <xsl:otherwise>Previous error</xsl:otherwise>
                            </xsl:choose>
                            &#160;&#160;&#160;
                            <xsl:choose>
                                <xsl:when test="preceding-sibling::testresult">
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:apply-templates select="preceding-sibling::testresult[1]"
                                                                 mode="indexedFileName"/>
                                        </xsl:attribute>
                                        <xsl:text>Previous result</xsl:text>
                                    </a>
                                </xsl:when>
                                <xsl:otherwise>Previous result</xsl:otherwise>
                            </xsl:choose>
                            &#160;&#160;&#160;
                            <a href="{$indexTests}">Back to Test Report Overview</a>
                            &#160;&#160;&#160;
                            <xsl:choose>
                                <xsl:when test="following-sibling::testresult">
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:apply-templates select="following-sibling::testresult[1]"
                                                                 mode="indexedFileName"/>
                                        </xsl:attribute>
                                        <xsl:text>Next result</xsl:text>
                                    </a>
                                </xsl:when>
                                <xsl:otherwise>Next result</xsl:otherwise>
                            </xsl:choose>
                            &#160;&#160;&#160;
                            <xsl:choose>
                                <xsl:when test="following-sibling::testresult[@successful='no']">
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:apply-templates
                                                    select="(following-sibling::testresult[@successful='no'])[1]"
                                                    mode="indexedFileName"/>
                                        </xsl:attribute>
                                        <xsl:text>Next error</xsl:text>
                                    </a>
                                </xsl:when>
                                <xsl:otherwise>Next error</xsl:otherwise>
                            </xsl:choose>
                        </div>
                    </body>
                </html>
            </redirect:write>
        </xsl:if>
    </xsl:template>

    <xsl:template match="testresult" mode="embedded">
        <!-- general info left -->
        <!-- Header and red/green box for success/failure overview-->
        <blockquote>
            <h4>
                <xsl:apply-templates select="." mode="successIndicator"/>
                <xsl:text>&nbsp;</xsl:text>
                <a>
                    <xsl:attribute name="name">
                        <xsl:text>testspec</xsl:text>
                        <xsl:number/>
                    </xsl:attribute>
                    <xsl:value-of select="@testspecname"/>
                </a>
            </h4>
            <xsl:apply-templates select="description"/>

            <xsl:call-template name="displayTestInfo"/>

            <xsl:text>Test started at&space;</xsl:text>
            <xsl:value-of select="@starttime"/>
            <xsl:text>, lasting&space;</xsl:text>
            <xsl:call-template name="time">
                <xsl:with-param name="msecs"
                                select="sum(results/step/result/node()[name()='completed' or name()='failed']/@duration)"/>
                <xsl:with-param name="detail" select="true()"/>
            </xsl:call-template>
            <xsl:text>.</xsl:text>
            <br/>
            <xsl:text>Source:&space;</xsl:text>
            <span class="location">
                <xsl:value-of select="@location"/>
            </span>
            <br/>
            <xsl:text>Base URL (used by&space;</xsl:text>
            <b>invoke</b>
            <xsl:text>&space;steps with a relative URL):&space;</xsl:text>
            <xsl:apply-templates select="config"/>
        </blockquote>

        <!-- ###################################################################### -->
        <!-- tj: show/hide all -->
        <xsl:if test="descendant::step/descendant::step">
            <p>
                <xsl:text>Expand/Collapse nested steps:</xsl:text>
                <img onclick="showAllSubstepsOfTestspec(this)" src="{$img.expandall}"
                     class="withPointer"
                     alt="show all nested steps in testspec" title="show all nested steps in testspec"/>
                <xsl:text>&nbsp; </xsl:text>
                <img onclick="hideAllSubstepsOfTestspec(this)" src="{$img.collapseall}"
                     class="withPointer"
                     alt="hide all nested steps in testspec" title="hide all nested steps in testspec"/>
            </p>
        </xsl:if>

        <!-- ###################################################################### -->
        <!-- The test step results in sequence below -->
        <xsl:apply-templates select="results"/>

        <xsl:apply-templates select="results/failure"/>
        <xsl:apply-templates select="results/error"/>
    </xsl:template>

    <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
    <!-- Special representation for the <testInfo .../> steps if any -->
    <xsl:template name="displayTestInfo">
        <xsl:if test=".//step/parameter[@name = 'taskName' and @value = 'testInfo']">
            <div class="testInfo">
                <div class="testInfoTitle">Test info</div>
                <ul class="testInfo">
                    <xsl:apply-templates mode="displayTestInfo"/>
                </ul>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template match="step[parameter[@name = 'taskName' and @value = 'testInfo']]" mode="displayTestInfo">
        <li>
            <b>
                <xsl:value-of select="parameter[@name = 'type']/@value"/>
                <xsl:text>:&space;</xsl:text>
            </b>
            <xsl:value-of select="concat(parameter[@name = 'info']/@value, parameter[@name = 'nested text']/@value)"/>
        </li>
    </xsl:template>

    <!-- Individual templates -->
    <xsl:template match="config">
        <xsl:variable name="port" select="parameter[@name='port']/@value"/>
        <xsl:variable name="basepath" select="parameter[@name='basepath']/@value"/>
        <xsl:variable name="configHref">
            <xsl:value-of select="parameter[@name='protocol']/@value"/>
            <xsl:text>://</xsl:text>
            <xsl:value-of select="parameter[@name='host']/@value"/>
            <xsl:if test="$port != 80">
                <xsl:text>:</xsl:text>
                <xsl:value-of select="$port"/>
            </xsl:if>
            <xsl:text>/</xsl:text>
            <xsl:if test="$basepath != 'null'">
                <xsl:value-of select="$basepath"/>
                <xsl:text>/</xsl:text>
            </xsl:if>
        </xsl:variable>
        <a target="_blank" href="{$configHref}" class="baseUrl"> <!-- class important to allow js code to read it  -->
            <xsl:value-of select="$configHref"/>
        </a>
    </xsl:template>

    <!-- Renders the description of a webtest. Currently a webtest can have 0 or 1 description -->
    <xsl:template match="description">
        <div class="description">
            <xsl:value-of select="text()"/>
        </div>
    </xsl:template>

    <!-- Renders the link to a saved result page -->
    <xsl:template match="parameter[@name='resultFilename']">
        <xsl:param name="linkText" select="'Resulting page'"/>
        <xsl:param name="class"/>
        <br/>
        <a target="_blank">
            <xsl:if test="string-length($class) > 0">
                <xsl:attribute name="class">
                    <xsl:value-of select="$class"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:attribute name="href">
                <xsl:if test="$responses.dir">
                    <xsl:value-of select="$responses.dir"/>
                    <xsl:text>/</xsl:text>
                </xsl:if>
                <xsl:value-of select="@value"/>
            </xsl:attribute>
            <xsl:value-of select="$linkText"/>
        </a>
    </xsl:template>

    <xsl:template match="parameter">
        <tr>
            <td class="parameterName">
                <xsl:value-of select="@name"/>
            </td>
            <td class="parameterValue">
                <xsl:choose>
                    <xsl:when test="@name='filename' and not(starts-with(@value, '#{'))">
                        <a target="_blank" href="concat('file://', translate(@value, '\', '/'))">
                            <xsl:value-of select="@value"/>
                        </a>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="@value"/>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="step">
        <tr>
            <!-- ###################################################################### -->
            <!-- tj: create image to show/hide substeps if row contains substeps and add onclick and groupStep attribute to image -->
            <td class="light" style="border-bottom:1px inset black;">
                <b>&nbsp;
                    <xsl:number/>
                </b>
                <xsl:if test="descendant::step">
                    <br/>
                    <img name="collapseButton" onclick="changeLevelOfDetailForGroup(this)"
                         src="{$img.expandall}" class="withPointer" alt="Shows all substeps"
                         title="Shows all substeps"/>
                </xsl:if>
            </td>
            <!-- ###################################################################### -->

            <xsl:apply-templates select="result"/>
            <xsl:call-template name="stepNameCell"/>
            <xsl:call-template name="stepParameterCell"/>
            <td style="border-bottom:1px inset black;" valign="top" align="right" class="light">
                <xsl:choose>
                    <xsl:when test="result/completed or result/failed">
                        <xsl:call-template name="time">
                            <xsl:with-param name="msecs"
                                            select="result/node()[name()='completed' or name()='failed']/@duration"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        &nbsp;
                    </xsl:otherwise>
                </xsl:choose>
            </td>
        </tr>
    </xsl:template>


    <xsl:template name="stepNameCell">
        <td style="border-bottom:1px inset black;" valign="top" class="light">
            <b>
                <xsl:value-of select="parameter[@name='taskName']/@value"/>
            </b>
            <br/>
            <!-- Hide title unknown-->
            <xsl:choose>
                <xsl:when test="parameter[@name='description']/@value='&lt;unknown&gt;'">
                    &nbsp;
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="parameter[@name='description']/@value"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="parameter[@name='resultFilename']"/>

            <!-- if the step is a container, display link to last result page of this container.
                              This allows to see the last result of a group without to have to click the nested steps of this group -->
            <xsl:if test="descendant::step[descendant::step]">
                <xsl:apply-templates select="(.//parameter[@name='resultFilename'])[last()]">
                    <xsl:with-param name="linkText" select="'last result page of this group'"/>
                </xsl:apply-templates>
            </xsl:if>
        </td>
    </xsl:template>

    <xsl:template name="stepParameterCell">
        <td style="border-bottom:1px inset black;" valign="top" class="light">
            <xsl:variable name="parameter.list"
                          select="parameter[@name!='taskName'][@name!='description'][@name!='resultFilename'][@name!='text' or @value!='null'][@name!='regex' or @value!='false']"/>
            <xsl:choose>
                <xsl:when test="count($parameter.list) > 0">
                    <div class="parameterWrapper"> <!-- to have a scrollbar when parameter values are very long -->
                        <table cellpadding="2" cellspacing="0">
                            <xsl:apply-templates select="$parameter.list[@name!='=> value']">
                                <xsl:sort select="@name"/>
                            </xsl:apply-templates>
                            <xsl:apply-templates select="$parameter.list[@name='=> value']"/>
                        </table>
                    </div>
                </xsl:when>
                <xsl:otherwise>
                    &nbsp;
                </xsl:otherwise>
            </xsl:choose>
            <xsl:call-template name="renderStepTable"/>
        </td>
    </xsl:template>

    <!--
          Template to be applied for the element results, and called by name for children of step (group, not, ...)
          -->
    <xsl:template match="results" name="renderStepTable">
        <xsl:if test="count(step) > 0">
            <table cellpadding="3" border="0" cellspacing="0" width="100%" class="expanded">
                <tr>
                    <th>&no;</th>
                    <th>Result</th>
                    <th>Name</th>
                    <th>Parameter</th>
                    <th>Duration</th>
                </tr>
                <xsl:apply-templates select="step"/>
            </table>
        </xsl:if>
    </xsl:template>

    <xsl:template match="result[completed]">
        <td style="border-bottom:1px inset black;" class="light" align="center">
            <xsl:choose>
                <!-- tj: set status of a step of type 'group' to 'failed' if one of its nested steps fails  -->
                <!-- tj: set status to 'ok' if step is not of type 'group' or if all substeps of a step of type 'group' have been executed successfully. -->
                <xsl:when
                        test="preceding-sibling::*[@name='taskName'][@value='group']/following-sibling::step/result/failed">
                    <img src="{$img.todo}" alt="x"/>
                </xsl:when>
                <xsl:otherwise>
                    <img src="{$img.ok}" alt="ok"/>
                </xsl:otherwise>
            </xsl:choose>
        </td>
    </xsl:template>

    <xsl:template match="result[failed]">
        <td style="border-bottom:1px inset black;" class="light" align="center">
            <img src="{$img.todo}" alt="x"/>
            <!--
                              Marks only the step that fails.
                              This step has an even number of 'not' ancestor and a single failing result in its desendants (itself!)
                              -->
            <xsl:if
                    test="(count(../ancestor::step/parameter[@name='taskName' and @value='not']) mod 2 = 0) and (count(../descendant::result[failed]) = 1)">
                <br/>
                <a class="linkToError">
                    <xsl:attribute name="name">
                        <xsl:text>testspec</xsl:text>
                        <xsl:number count="testresult"/>
                        <xsl:text>-error</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:text>#error</xsl:text>
                        <xsl:number count="testresult"/>
                    </xsl:attribute>
                    <xsl:text>Error</xsl:text>
                </a>
                <xsl:apply-templates
                        select="(ancestor::results//descendant::step[parameter[@name='resultFilename']])[last()]/parameter[@name='resultFilename']">
                    <xsl:with-param name="linkText">Page</xsl:with-param>
                    <xsl:with-param name="class">linkToError</xsl:with-param>
                </xsl:apply-templates>
            </xsl:if>
        </td>
    </xsl:template>

    <xsl:template match="result[notexecuted]">
        <td style="border-bottom:1px inset black;" class="light" align="center">
            <img src="{$img.optional}" alt="o"/>
        </td>
    </xsl:template>

    <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
    <!-- common handling for failure and error (perhaps should them be merged?) -->
    <xsl:template match="failure|error">
        <h2>
            <a>
                <xsl:attribute name="name">
                    <xsl:text>error</xsl:text>
                    <xsl:number count="testresult"/>
                </xsl:attribute>
                <xsl:text>Error</xsl:text>
            </a>
        </h2>

        <h3>Message</h3>
        <p>
            <xsl:value-of select="@message"/>
        </p>

        <h3>Location</h3>
        <p>
            <xsl:value-of select="@filename"/>
            <xsl:text> (line: </xsl:text>
            <xsl:value-of select="@line"/>
            <xsl:text>)</xsl:text>
        </p>

        <xsl:if test="detail">
            <h3>Details</h3>
            <table>
                <tbody>
                    <xsl:apply-templates select="detail"/>
                </tbody>
            </table>
        </xsl:if>

        <xsl:if test="@exception">
            <h3>Exception</h3>
            <p>
                <xsl:value-of select="@exception"/>
            </p>
        </xsl:if>

        <xsl:if test="stacktrace">
            <h3 onclick="toggleDisplayNext(this, 'PRE')" title="Show/hide stacktrace" class="withPointer">
                <img alt="Show " src="{$img.expandPlus}"/>
                <xsl:text> Stacktrace</xsl:text>
            </h3>
            <pre style="display: none">
                <xsl:value-of select="stacktrace/text()" disable-output-escaping="no"/>
            </pre>
        </xsl:if>
    </xsl:template>

    <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
    <!-- the details of a failure/error -->
    <xsl:template match="detail">
        <tr>
            <td class="detailName">
                <xsl:value-of select="@name"/>
            </td>
            <td class="detailText">
                <xsl:value-of select="text()"/>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
