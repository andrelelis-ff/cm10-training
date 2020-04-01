<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->
<#-- @ftlvariable name="self.content" type="com.coremedia.blueprint.common.contentbeans.CMLinkable" -->

<#assign studioExtraFilesMetadata=preview.getStudioAdditionalFilesMetadata(bp.setting(self, "studioPreviewCss"), bp.setting(self, "studioPreviewJs"))/>
<#assign titleSuffix=bp.setting(self, "customTitleSuffixText", cm.getMessage('title_suffix'))/>

<head<@preview.metadata data=studioExtraFilesMetadata/>>
<#-- add encoding first! -->
    <meta charset="UTF-8">
<#-- SEO: title -->
    <title<@preview.metadata "properties.htmlTitle" />>${self.content.htmlTitle!"CoreMedia CMS - No Page Title"}${' ' + titleSuffix}</title>
<#-- SEO: description -->
<#if self.content.htmlDescription?has_content>
    <meta name="description" content="${self.content.htmlDescription}">
</#if>
<#-- SEO: keywords -->
<#if self.content.keywords?has_content>
  <meta name="keywords" content="${self.content.keywords}">
</#if>
<#-- viewport for responsive design -->
    <meta name="viewport" content="width=device-width, initial-scale=1">
<#-- generator -->
    <meta name="generator" content="CoreMedia Content Cloud">
<#-- favicon -->
<@cm.include self=self view="_favicon" />
<#-- SEO: canonical -->
<#if self.content?has_content>
    <link rel="canonical" href="${cm.getLink(self.content, {"absolute":true})}">
</#if>
<#-- SEO: i18n -->
<#if (self.content.localizations)?has_content>
  <#assign localizations=self.content.localizations![] />
  <#list localizations as localization>
    <#-- list all localized variants without self -->
    <#if localization.locale != self.content.locale>
      <#assign variantLink=cm.getLink(localization) />
      <#if variantLink?has_content>
          <link rel="alternate" hreflang="${bp.getPageLanguageTag(localization)}" href="${variantLink}" title="${localization.locale.getDisplayName(self.content.locale)} | ${localization.locale.getDisplayName()}">
      </#if>
    </#if>
  </#list>
</#if>

<#-- remove no-js class before loading css and more -->
<script>document.documentElement.className = document.documentElement.className.replace(/no-js/g, 'js');</script>

<@cm.include self=self view="_additionalHead" />

</head>
