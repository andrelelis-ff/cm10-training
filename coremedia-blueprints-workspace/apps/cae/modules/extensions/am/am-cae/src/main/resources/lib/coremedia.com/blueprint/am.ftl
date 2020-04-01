<#ftl strip_whitespace=true>
<#-- @ftlvariable name="amFreemarkerFacade" type="com.coremedia.blueprint.assets.cae.tags.AMFreemarkerFacade" -->

<#-- -------------------------------------------------------------------------------------------------------------------
 *
 * Please check the section "CoreMedia FreeMarker API" in chapter "Reference" in the "Frontend Developer Guide" for
 * details and examples for the following directives.
 * Any changes, additions or removals need to be documented in the manual.
 *
 ------------------------------------------------------------------------------------------------------------------- -->

<#-- GET DOWNLOAD PORTAL -->
<#function getDownloadPortal>
  <#return amFreemarkerFacade.getDownloadPortal() />
</#function>

<#-- HAS DOWNLOAD PORTAL -->
<#function hasDownloadPortal>
  <#return amFreemarkerFacade.hasDownloadPortal() />
</#function>
