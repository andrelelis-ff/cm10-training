<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.PageGridPlacement" -->

<#-- This placement is used for the footer section -->
<div class="cm-footer-navigation"<@preview.metadata data=[bp.getPlacementPropertyName(self), bp.getPlacementHighlightingMetaData(self)!""]/>>
  <#list self.items![]>
    <div class="cm-footer-navigation__columns">
      <#items as column>
        <#compress>
          <div class="cm-footer-navigation__column"<@preview.metadata column.content />>
            <@cm.include self=column view="asFooterNavigationColumn" />
          </div>
        </#compress>
      </#items>
    </div>
  </#list>
</div>
