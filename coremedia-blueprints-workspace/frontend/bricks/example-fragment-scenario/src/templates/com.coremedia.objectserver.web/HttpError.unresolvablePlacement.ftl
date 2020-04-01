<#-- @ftlvariable name="self" type="com.coremedia.objectserver.web.HttpError" -->
<#-- @ftlvariable name="placementName" type="java.lang.String" -->
<#if preview.isPreviewCae()>
<div <@preview.metadata data=[bp.getPlacementHighlightingMetaData(placementName)!""] />>
</div>
</#if>
