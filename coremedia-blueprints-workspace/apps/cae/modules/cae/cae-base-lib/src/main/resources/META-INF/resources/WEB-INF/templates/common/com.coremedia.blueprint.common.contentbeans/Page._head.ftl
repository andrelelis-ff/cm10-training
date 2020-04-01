<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->
<#-- @ftlvariable name="self.content" type="com.coremedia.blueprint.common.contentbeans.CMLinkable" -->

<#--
    Template Description:

    This template renders the head of every page with title and other meta tags. It includes also css and javascripts.
    Please check the brick "generic templates" for a more detailed version.
-->

<#assign studioExtraFilesMetadata=preview.getStudioAdditionalFilesMetadata(bp.setting(self, "studioPreviewCss"), bp.setting(self, "studioPreviewJs"))/>

<head<@preview.metadata data=studioExtraFilesMetadata/>>
  <meta charset="UTF-8">
  <title<@preview.metadata "properties.htmlTitle" />>${self.content.htmlTitle!"CoreMedia CMS"}</title>
  <#if self.content.htmlDescription?has_content>
    <meta name="description" content="${self.content.htmlDescription}">
  </#if>
  <#if self.content.keywords?has_content>
    <meta name="keywords" content="${self.content.keywords}">
  </#if>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="generator" content="CoreMedia CMS">
  <#if self.favicon?has_content>
    <link rel="shortcut icon" href="${cm.getLink(self.favicon)}"<@preview.metadata "properties.favicon" />>
  </#if>
  <#if self.content?has_content>
    <link rel="canonical" href="${cm.getLink(self.content, {"absolute":true})}">
  </#if>
  <#if (self.content.localizations)?has_content>
    <#assign localizations=self.content.localizations![] />
    <#list localizations as localization>
      <#if localization.locale != self.content.locale>
        <#assign variantLink=cm.getLink(localization) />
        <#if variantLink?has_content>
          <link rel="alternate" hreflang="${bp.getPageLanguageTag(localization)}" href="${variantLink}">
        </#if>
      </#if>
    </#list>
  </#if>

  <#-- includes css and javascripts -->
  <@cm.include self=self view="_additionalHead" />
</head>
