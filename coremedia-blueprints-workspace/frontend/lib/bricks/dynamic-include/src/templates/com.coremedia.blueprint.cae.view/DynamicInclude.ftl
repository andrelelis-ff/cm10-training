<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/><#-- could be used as fragment -->
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.view.DynamicInclude" -->

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
  <#-- include AHAH fragment via AJAX -->
  <div class="cm-fragment" data-cm-fragment="${fragmentLink}"></div>
</#if>
