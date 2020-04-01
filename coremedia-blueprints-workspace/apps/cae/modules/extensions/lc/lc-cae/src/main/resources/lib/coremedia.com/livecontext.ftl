<#ftl strip_whitespace=true>
<#-- @ftlvariable name="liveContextFreemarkerFacade" type="com.coremedia.livecontext.web.taglib.LiveContextFreemarkerFacade" -->
<#-- @ftlvariable name="liveContextLoginFreemarkerFacade" type="com.coremedia.livecontext.web.taglib.LiveContextLoginFreemarkerFacade" -->

<#-- -------------------------------------------------------------------------------------------------------------------
 *
 * Please check the section "CoreMedia FreeMarker API" in chapter "Reference" in the "Frontend Developer Guide" for
 * details and examples for the following directives.
 * Any changes, additions or removals need to be documented in the manual.
 *
 ------------------------------------------------------------------------------------------------------------------- -->


<#-- FORMAT PRICE -->
<#function formatPrice amount currency locale>
  <#return liveContextFreemarkerFacade.formatPrice(amount, currency, locale)>
</#function>

<#-- PRODUCT -->
<#function createProductInSite product>
  <#return liveContextFreemarkerFacade.createProductInSite(product)/>
</#function>

<#-- PREVIEW METADATA -->
<#function previewMetaData>
  <#return liveContextFreemarkerFacade.getPreviewMetadata()>
</#function>

<#-- AUGMENTED CHECK -->
<#function augmentedContent>
  <#return liveContextFreemarkerFacade.isAugmentedContent()>
</#function>

<#-- GET VENDOR NAME -->
<#function getVendorName>
  <#return liveContextFreemarkerFacade.getVendorName()>
</#function>

<#-- GET LOGIN STATUS URL -->
<#function getStatusUrl>
  <#return liveContextLoginFreemarkerFacade.getStatusUrl()>
</#function>

<#-- GET ABSOLUTE URL -->
<#function getLoginFormUrl>
  <#return liveContextLoginFreemarkerFacade.getLoginFormUrl()>
</#function>

<#-- GET LOGOUT URL -->
<#function getLogoutUrl>
  <#return liveContextLoginFreemarkerFacade.getLogoutUrl()>
</#function>
