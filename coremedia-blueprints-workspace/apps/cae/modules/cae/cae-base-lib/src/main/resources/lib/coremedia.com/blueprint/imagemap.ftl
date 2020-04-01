<#-- @ftlvariable name="blueprintFreemarkerFacade" type="com.coremedia.blueprint.cae.web.taglib.BlueprintFreemarkerFacade" -->

<#-- -------------------------------------------------------------------------------------------------------------------
 *
 * Please check the section "CoreMedia FreeMarker API" in chapter "Reference" in the "Frontend Developer Guide" for
 * details and examples for the following directives.
 *
 * Use the brick "image-maps" instead of direct accessing the following imagemap functions.
 *
 * Any changes, additions or removals need to be documented in the manual.
 *
 ------------------------------------------------------------------------------------------------------------------- -->

<#-- --- PRIVATE --------------------------------------------------------------------------------------------------- -->

<#--
 * The width all image transformations are based on.
 * PRIVATE
 -->
<#assign IMAGE_TRANSFORMATION_BASE_WIDTH=blueprintFreemarkerFacade.imageTransformationBaseWidth />

<#--
 * Return list of area configurations with the 'coords' attribute being transformed according to the image map's
 * picture transformations. If cropping is disabled, an empty list is returned.
 *
 * @param imageMap CMImageMap to retrieve areas from
 * PRIVATE
 -->
<#function responsiveImageMapAreas imageMap>
  <#return blueprintFreemarkerFacade.responsiveImageMapAreas(imageMap, [])>
</#function>

<#--
 * Returns Map containing information to be rendered as data attribute delivering informationen about the ImageMap
 * areas to JavaScript.
 *
 * @param coords map of transformation => points key/value pairs
 * PRIVATE
 -->
<#function responsiveImageMapAreaData coords>
  <#return blueprintFreemarkerFacade.responsiveImageMapAreaData(coords) />
</#function>
