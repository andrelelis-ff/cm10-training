<#ftl strip_whitespace=true>
<#-- @ftlvariable name="blueprintFreemarkerFacade" type="com.coremedia.blueprint.cae.web.taglib.BlueprintFreemarkerFacade" -->

<#-- -------------------------------------------------------------------------------------------------------------------
 *
 * Please check the section "CoreMedia FreeMarker API" in chapter "Reference" in the "Frontend Developer Guide" for
 * details and examples for the following directives.
 * Any changes, additions or removals need to be documented in the manual.
 *
 ------------------------------------------------------------------------------------------------------------------- -->

<#-- GET PAGE METADATA -->
<#function getPageMetadata page>
  <#return blueprintFreemarkerFacade.getPageContext(page).content />
</#function>

<#-- GET PLACEMENT NAME -->
<#function getPlacementPropertyName placement>
  <#return blueprintFreemarkerFacade.getPlacementPropertyName(placement) />
</#function>

<#-- GET CONTAINER METADATA -->
<#function getContainerMetadata container>
  <#return blueprintFreemarkerFacade.getContainerMetadata(container) />
</#function>

<#-- GET CONTAINER WITH ITEMS -->
<#function getContainer items=[]>
  <#return blueprintFreemarkerFacade.getContainer(items) />
</#function>

<#--
 * Utility function to allow an dynamizable container.
 * A strategy determines if the container will be rendered as dynamic include.
 *
 * @param object The object that can be persisted in a link
 * @param propertyPath The property path to retrieve the container's items
 * @return a new container
 -->
<#function getDynamizableContainer object propertyPath>
  <#return blueprintFreemarkerFacade.getDynamizableContainer(object, propertyPath) />
</#function>

<#-- GET CONTAINER WITH ITEMS OF GIVEN CONTAINER -->
<#--
 * Utility function to allow rendering of containers with custom items, e.g. partial containers with a subset of
 * the items the original container had.
 *
 * @param baseContainer The base container the new container shall be created from
 * @param items The items to be put inside the new container
 -->
<#function getContainerFromBase baseContainer items=[]>
  <#return blueprintFreemarkerFacade.getContainer(baseContainer, items) />
</#function>

<#-- GET LOCALE -->
<#function getPageLanguageTag object>
  <#return blueprintFreemarkerFacade.getLanguageTag(object) />
</#function>

<#-- GET DIRECTION -->
<#function getPageDirection object>
  <#return blueprintFreemarkerFacade.getDirection(object) />
</#function>

<#-- GET METADATA OF GIVEN PLACEMENT -->
<#function getPlacementHighlightingMetaData placement>
  <#return blueprintFreemarkerFacade.getPlacementHighlightingMetaData(placement)>
</#function>
