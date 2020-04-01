<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.contentbeans.AMAsset" -->

<#--
    Template Description:

    This template extends the brick "preview".
-->

<#assign fragmentViews=[] />
<#if am.hasDownloadPortal()>
  <#assign fragmentViews=fragmentViews + [{"viewName": "asDownloadPortal", "titleKey": "preview_am_label_download_portal"}] />
</#if>
<#list self.renditions as rendition>
  <#if rendition.blob?has_content>
    <#assign name=rendition.name!"" />
    <#if name?has_content && name != "original">
      <#assign viewName="[" + name + "]" />
      <#assign titleKey="preview_am_label_rendition_" + name />
      <#assign title=name?cap_first />
      <#assign fragmentViews=fragmentViews + [{"bean": rendition, "viewName": viewName, "titleKey": titleKey, "title": title}] />
    </#if>
  </#if>
</#list>

<#if (fragmentViews?size > 0)>
  <@cm.include self=self view="multiViewPreview" params={
    "fragmentViews": fragmentViews
  }/>
<#else>
  <span>${cm.getMessage('preview_am_no_renditions')}</span>
</#if>
