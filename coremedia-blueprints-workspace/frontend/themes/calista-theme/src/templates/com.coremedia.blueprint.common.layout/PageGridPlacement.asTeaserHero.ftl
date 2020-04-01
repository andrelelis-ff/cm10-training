<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.PageGridPlacement" -->

<#--
  Template Description:

  The CoreMedia content widget is configured for this view, redirect to "asHeroBanner".
-->

<!-- CoreMedia Placement ${self.name!""} -->
<div class="cm-placement cm-placement--${self.name!""}"<@preview.metadata data=[bp.getPlacementPropertyName(self), bp.getPlacementHighlightingMetaData(self)!""]/>>
  <#if self.items?has_content>
      <#list self.items![] as item>
        <@cm.include self=item view="asHeroBanner" />
      </#list>
  </#if>
</div>
