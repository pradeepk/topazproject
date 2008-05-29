<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2008 by Topaz, Inc.
  http://topazproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- end : container -->	

<script src="http://www.google-analytics.com/urchin.js" type="text/javascript">
</script>
<script type="text/javascript">
_uacct = "UA-338393-1";
  <#-- BEGIN MAJOR HACK FOR CONDITIONAL JOURNAL INCLUDE -->
  <#if journalContext = "PLoSClinicalTrials" >
_udn = "clinicaltrials.ploshubs.org";
  <#elseif journalContext = "PLoSCompBiol" >
_udn = "www.ploscompbiol.org";
  <#elseif journalContext = "PLoSGenetics" >
_udn = "www.plosgenetics.org";
  <#elseif journalContext = "PLoSNTD" >
_udn = "www.plosntds.org";
  <#elseif journalContext = "PLoSPathogens" >
_udn = "www.plospathogens.org";
  <#else>
_udn = "www.plosone.org";
  </#if>
  <#-- END HACK -->
urchinTracker();
</script>
</body>
</html>