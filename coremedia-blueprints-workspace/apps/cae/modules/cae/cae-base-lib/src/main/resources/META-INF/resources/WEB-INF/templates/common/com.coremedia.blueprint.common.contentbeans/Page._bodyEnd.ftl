<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->
<#-- @ftlvariable name="js" type="com.coremedia.blueprint.common.contentbeans.CMJavaScript" -->

<#--
    Template Description:

    This template renders the javascripts and the hook VIEW_HOOK_END at the end of the body element. Additional
    informations are added for developers.
    Please check the brick "generic templates" for a more detailed version.
-->

<#-- include fragment preview javascript  -->
<#if preview.isFragmentPreview()>
  <#assign fragmentPreviewJs=bp.setting(self, "fragmentPreviewJs", []) />
  <#list fragmentPreviewJs as js>
    <@cm.include self=js view="asJSLink"/>
  </#list>
</#if>

<#-- include javascript at bottom for performance -->
<#list self.javaScript![] as js>
  <#-- items in result of getJavaScript() are always CMJavaScript -->
  <#if !js.ieExpression?has_content>
    <@cm.include self=js view="asJSLink"/>
  </#if>
</#list>

<#-- include additional javascript for preview -->
<#if preview.isPreviewCae()>
  <#assign previewJs=bp.setting(self, "previewJs", []) />
  <#list previewJs as js>
    <@cm.include self=js view="asJSLink"/>
  </#list>
</#if>

<#-- hook for extensions at bottom of page (for e.g. javascripts) -->
<@cm.hook id=bp.viewHookEventNames.VIEW_HOOK_END />

<#-- icon and tools for developer mode -->
<@cm.include self=self view="_developerMode" />
