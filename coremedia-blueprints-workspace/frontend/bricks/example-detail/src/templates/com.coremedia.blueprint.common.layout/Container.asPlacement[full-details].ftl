<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Container" -->

<#assign items=self.items![] />
<#assign numberOfItems=items?size />

<div class="cm-details-container"<@preview.metadata bp.getContainerMetadata(self) />>
  <#list items as item>
    <@cm.include self=item />
  </#list>
</div>
