<#ftl strip_whitespace=true>
<#-- @ftlvariable name="blueprintFreemarkerFacade" type="com.coremedia.blueprint.cae.web.taglib.BlueprintFreemarkerFacade" -->

<#-- -------------------------------------------------------------------------------------------------------------------
 *
 * Please check the section "CoreMedia FreeMarker API" in chapter "Reference" in the "Frontend Developer Guide" for
 * details and examples for the following directives.
 * Any changes, additions or removals need to be documented in the manual.
 *
 ------------------------------------------------------------------------------------------------------------------- -->

<#-- SET ASPECT RATIOS -->
<#function responsiveImageLinksData picture aspectRatios=[]>
  <#return blueprintFreemarkerFacade.responsiveImageLinksData(picture, cmpage, aspectRatios)>
</#function>

<#-- GET IMAGE LINK OF THE BIGGEST PREDEFINED IMAGE FOR A GIVEN ASPECTRATIO -->
<#function getBiggestImageLink picture aspectRatio="">
  <#return blueprintFreemarkerFacade.getLinkForBiggestImageWithRatio(picture, cmpage, aspectRatio)>
</#function>

<#-- GET IMAGE LINK FR A GIVEN ASPECTRATIO AND SIZE -->
<#function transformedImageUrl picture aspectRatio width height>
  <#return blueprintFreemarkerFacade.transformedImageUrl(picture, aspectRatio, width, height)>
</#function>
