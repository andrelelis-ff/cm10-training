<#-- @ftlvariable name="self" type="com.coremedia.blueprint.analytics.google.GoogleAnalytics" -->

<#if self.content?has_content>
  <#assign currentPageUrl= cm.getLink(self.content)/>
</#if>
<#-- google analytics -->
<#if self.enabled>
  <script type="text/javascript"><!--
          <#include "/WEB-INF/includes/js/alx-integration-googleanalytics.js">

          (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
                    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
                  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
          })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  var gaAccountData = new GaAccountData("${self.webPropertyId!""}", "${self.domainName!""}");

  var gaPageData = new GaPageviewData(
          "${self.contentId!""}",
          "${self.contentType!""}",
          "${self.navigationPathIds?join('_')}",
          "${currentPageUrl!""}", null, null, null,
          ${self.advertisingFeaturesPluginDisabled?c}
  );

  gaTrackPageview(ga, gaAccountData, gaPageData);

  //-->
</script>
</#if>