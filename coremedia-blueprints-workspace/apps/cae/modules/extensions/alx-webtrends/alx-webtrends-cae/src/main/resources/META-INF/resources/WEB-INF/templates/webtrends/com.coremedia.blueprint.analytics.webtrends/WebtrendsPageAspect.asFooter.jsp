<%@ taglib prefix="cm" uri="http://www.coremedia.com/2004/objectserver-1.0-2.0" %>
<%@ page contentType="text/html; charset=UTF-8" session="false"
%><%@ include file="/WEB-INF/includes/taglibs.jinc"
%><%--@elvariable id="self" type="com.coremedia.blueprint.analytics.webtrends.WebtrendsPageAspect"--%>
<c:if test="${self.enabled}">
  <!-- START OF SmartSource Data Collector TAG v10.2.36 -->
  <!-- Copyright (c) 2012 Webtrends Inc.  All rights reserved. -->
  <script><!--
    window.webtrendsAsyncInit = function () {
      var dcs = new Webtrends.dcs().init({
        dcsid:"${self.dcsid}",
        dcssip:"${self.dcssip}",
        domain:"statse.webtrendslive.com",
        timezone:1,
        download:true,
        downloadtypes:"xls,doc,pdf,txt,csv,zip,docx,xlsx,rar,gzip",
        metanames:"segments,segmentIds,contentId,contentType"
      });
      dcs.track();
    };

  /*
   * For evaluating the clicks configure a report which takes the WT.ti query parameter as dimension with measures of your choice
   *
   * The WT.ti pass this example value:
   * Download:foobar.zip:1122 {category}{delimiter}{filename}{delimiter}{content id}
   */
  if (typeof registerAlxEventHandler === 'function') {
    registerAlxEventHandler(
            function webtrendsTrackEvent(eventCategory, eventAction, eventName, eventValue) {
              if (eventCategory == 'Download') {
                dcsMultiTrack('DCS.dcssip', '${self.dcssip}', 'WT.ti', eventCategory+':'+eventAction+':'+eventName, 'WT.dl', '20');
              }
            }
    )
  } else {
    if (window.console && window.console.log) {
      window.console.log('[WARN] Cannot enable event tracking for Webtrends: ALX tracking API not loaded.');
    }
  }


  // -->
  </script>
  <%-- this include is supposed to load webtrends.min.js --%>
  <%@include file="/WEB-INF/analyticsHeader.jsp"%>
  <noscript><img alt="dcsimg" id="dcsimg" width="1" height="1"
                 src="//statse.webtrendslive.com/${self.dcsid}/njs.gif?dcsuri=/nojavascript&amp;WT.js=No&amp;WT.tv=10.2.36&amp;dcssip=${self.dcssip}"/>
  </noscript>
  <!-- END OF SmartSource Data Collector TAG v10.2.36 -->
</c:if>