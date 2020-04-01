<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.PageGridPlacement" -->

<#--
  Template Description:

  This template renders a placement and all its items or the detailview of a content in the main section.
-->

<div id="cm-placement-${self.name!""}" class="cm-placement"<@preview.metadata data=[bp.getPlacementPropertyName(self), bp.getPlacementHighlightingMetaData(self)!""]/>>

  <#if cmpage.detailView && (self.name!"") == "main">
    <#-- replace main section with the content in detailView -->
    <@cm.include self=cmpage.content/>
  <#else>
    <#-- render the placement items -->
    <#list self.items![] as item>
      <@cm.include self=item />
    </#list>
  </#if>
</div>
