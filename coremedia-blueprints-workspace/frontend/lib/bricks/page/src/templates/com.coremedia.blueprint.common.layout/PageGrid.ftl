<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.PageGrid" -->

<#--
  Template Description:

  This template renders the grid of the page based on the rows and placements.
-->

<#if self?has_content>
  <div class="cm-grid ${(self.cssClassName)!""}">
    <#-- Iterate over each row -->
    <#list self.rows![] as row>
      <div class="cm-grid__row">
        <#-- Iterate over each placement -->
        <#list row.placements![] as placement>
          <@cm.include self=placement />
        </#list>
      </div>
    </#list>
  </div>
</#if>
