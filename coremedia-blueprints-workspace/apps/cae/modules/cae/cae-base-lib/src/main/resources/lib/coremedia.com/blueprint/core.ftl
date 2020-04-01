<#ftl strip_whitespace=true>
<#-- @ftlvariable name="blueprintFreemarkerFacade" type="com.coremedia.blueprint.cae.web.taglib.BlueprintFreemarkerFacade" -->
<#-- @ftlvariable name="cmFacade" type="com.coremedia.objectserver.view.freemarker.CAEFreemarkerFacade" -->
<#-- @ftlvariable name="springMacroRequestContext" type="org.springframework.web.servlet.support.RequestContext" -->

<#-- -------------------------------------------------------------------------------------------------------------------
 *
 * Please check the section "CoreMedia FreeMarker API" in chapter "Reference" in the "Frontend Developer Guide" for
 * details and examples for the following directives.
 * Any changes, additions or removals need to be documented in the manual.
 *
 ------------------------------------------------------------------------------------------------------------------- -->


<#assign viewHookEventNames=blueprintFreemarkerFacade.getViewHookEventNames()/>

<#-- NAVIGATION -->
<#function isActiveNavigation navigation navigationPathList>
    <#return blueprintFreemarkerFacade.isActiveNavigation(navigation, navigationPathList)>
</#function>

<#-- SETTINGS -->
<#function setting self key default=cm.UNDEFINED>
  <#return blueprintFreemarkerFacade.setting(self, key, default, cmpage)>
</#function>

<#-- GENERATE UNIQUE ID -->
<#function generateId prefix="">
  <#return blueprintFreemarkerFacade.generateId(prefix)>
</#function>

<#-- TRUNCATE TEXT -->
<#function truncateText text maxLength>
  <#return blueprintFreemarkerFacade.truncateText(text, maxLength)>
</#function>

<#-- TRUNCATE HIGHLIGHTED TEXT -->
<#function truncateHighlightedText text maxLength>
  <#return blueprintFreemarkerFacade.truncateHighlightedText(text, maxLength)>
</#function>

<#-- GET FRAGMENTS FOR PREVIEW -->
<#function previewTypes page self defaultFragmentViews=[]>
  <#return blueprintFreemarkerFacade.getPreviewViews(self, page, defaultFragmentViews)>
</#function>

<#-- STACK TRACE EXCEPTION -->
<#function getStackTraceAsString exception>
  <#return blueprintFreemarkerFacade.getStackTraceAsString(exception)>
</#function>

<#-- SPRING WEB FLOW REQUEST -->
<#function isWebflowRequest>
  <#return blueprintFreemarkerFacade.isWebflowRequest()!false>
</#function>

<#-- GET FILE SIZE AS HUMAN READABLE FORMAT -->
<#function getDisplayFileSize size locale=cm.UNDEFINED>
  <#if !size?is_number>
    <#return 0>
  </#if>
  <#if cm.isUndefined(locale)>
    <#local locale=(cmpage.locale)!cm.UNDEFINED />
    <#if cm.isUndefined(locale)>
      <#local locale=springMacroRequestContext.getLocale()>
    </#if>
  </#if>
  <#return blueprintFreemarkerFacade.getDisplayFileSize(size, locale) />
</#function>

<#-- GET FILE EXTENSION -->
<#function getDisplayFileFormat mimeType>
  <#return blueprintFreemarkerFacade.getDisplayFileFormat(mimeType) />
</#function>

<#-- SIZE AS INTEGER -->
<#function isDisplayableImage blob>
  <#return blueprintFreemarkerFacade.isDisplayableImage(blob) />
</#function>

<#-- TYPE CHECK -->
<#function isDisplayableVideo blob>
  <#return blueprintFreemarkerFacade.isDisplayableVideo(blob) />
</#function>

<#-- RESOURCE PATH IN THEMES -->
<#function getLinkToThemeResource path>
  <#return blueprintFreemarkerFacade.getLinkToThemeResource(path)>
</#function>


<#-- --- DEPRECATED/UNUSED ----------------------------------------------------------------------------------------- -->

<#-- DEPRECATED -->
<#function createBeanFor content>
  <#return blueprintFreemarkerFacade.createBeanFor(content)>
</#function>

<#-- DEPRECATED -->
<#function createBeansFor contents>
  <#return blueprintFreemarkerFacade.createBeansFor(contents)>
</#function>

<#-- DEPRECATED, use getDisplayFileSize instead -->
<#function getDisplaySize size>
  <#return blueprintFreemarkerFacade.getDisplaySize(size) />
</#function>
