<%@ taglib prefix="cm" uri="http://www.coremedia.com/2004/objectserver-1.0-2.0" %>
<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<%@ include file="/WEB-INF/includes/taglibs.jinc" %>
<%--@elvariable id="self" type="com.coremedia.blueprint.cae.action.webflow.WebflowActionState"--%>
<%--@elvariable id="cmpage" type="com.coremedia.blueprint.common.contentbeans.Page"--%>
<%--@elvariable id="settingsService" type="com.coremedia.blueprint.base.settings.SettingsService"--%>
<c:if test="${bp:settingWithDefault(settingsService, cmpage, 'userDetails.interests.enabled', true)}">

<%--
 Display "Explicit Interests" form.
 This form is loaded and updated via Ajax!
--%>
<%
// hack creating the personalization from manually. This is required only for building the link pointing to the ajax handler.
request.setAttribute("form", new com.coremedia.blueprint.personalization.forms.PersonalizationForm());
%>
<cm:link target="${form}" view="ajax" var="explicitInterestsAjaxLink"  escape="false">
  <cm:param name="page" value="${cmpage}"/>
</cm:link>
<%-- Note: Need to prevent URL escaping by using 'escape="false"'. Otherwise ajax call and browser crashes --%>
<script type="text/javascript">
  //<![CDATA[
  jQuery(document).ready(function(jQuery) {
    var request = jQuery.ajax({
      url: '${explicitInterestsAjaxLink}',
      type: "GET",
      dataType: "html"
    });

    request.done(function(response) {
      //request has completed. render response
      jQuery("#explicitInterestsSection").html( response );
    });

  });
  //]]>
</script>
<div id="explicitInterestsSection">
  <%--
  intentionally left empty, form will be rendered here.
  --%>
</div>
</c:if>