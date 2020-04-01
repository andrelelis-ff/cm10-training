<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->
<#-- @ftlvariable name="self.content" type="com.coremedia.blueprint.common.contentbeans.CMLinkable" -->

<head>
  <#-- add encoding first! -->
  <meta charset="UTF-8">
  <#-- make the crawler index the coremedia content id-->
  <#if self.content.contentId?has_content>
    <meta name="coremedia_content_id" content="${self.content.contentId}">
  </#if>
  <#-- SEO: title -->
  <title>${(self.content.htmlTitle)!""}</title>
  <#-- SEO: description -->
  <#if self.content.htmlDescription?has_content>
    <meta name="description" content="${self.content.htmlDescription}">
  </#if>
  <#-- favicon -->
  <#if self.favicon?has_content>
    <link rel="shortcut icon" href="${cm.getLink(self.favicon)}">
  </#if>
  <#-- SEO: canonical -->
  <#if self.content?has_content>
    <link rel="canonical" href="${cm.getLink(self.content)}">
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

</head>
