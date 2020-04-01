<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.view.DynamicInclude" -->

<#--
    Template Description:

    Renders an ESI fragment, if a CDN is used, otherwise it will call the fragment via AJAX as AHAH fragment.
    Please check the brick "dynamic-include" for a more detailed version. More informations can be found in the
    Blueprint Developer Manual, chapter "Using Dynamic Fragments in HTML Responses".
-->

<#assign isWebflowRequest=bp.isWebflowRequest()/>
<#assign fragmentLink=cm.getLink(self.delegate, "fragment", {
  "targetView": self.view!cm.UNDEFINED,
  "webflow": isWebflowRequest
})/>

<#--
    don't use ESI include fragment if the fragment link is to be processed on the server side and
    therefore not a valid ESI include link, e.g. "<!--CM ...".
-->
<#if ((!fragmentLink?starts_with("<!--")) && (cm.getRequestHeader("Surrogate-Capability")?contains("ESI/1.0"))!false)>
  <${'esi'}:include src="${fragmentLink}" onerror="continue"/>
<#else>
  <#-- include AHAH fragment, see also brick "dynamic-include" for a deeper integration based on jQuery -->
  <#-- see http://microformats.org/wiki/rest/ahah for more informatations -->
  <#assign fragmentId=bp.generateId('fragment') />
  <div id="${fragmentId}"></div>
  <script>
    var request = new XMLHttpRequest();
    request.open('GET', '${fragmentLink}', true);
    request.setRequestHeader("X-Requested-With", "XMLHttpRequest");
    request.withCredentials = true;
    request.onload = function() {
      if (request.status === 200) {
        var fragment = document.getElementById('${fragmentId}');
        fragment.innerHTML = request.responseText;
      }
    };
    request.send();
  </script>
</#if>
