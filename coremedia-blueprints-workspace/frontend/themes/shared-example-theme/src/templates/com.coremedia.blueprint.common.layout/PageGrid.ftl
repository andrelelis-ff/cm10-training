<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.PageGrid" -->
<#if self?has_content>
  <div class="cm-grid ${(self.cssClassName)!""}">
    <#-- Iterate over each row -->
    <#list self.rows![] as row>
      <#-- Iterate over each placement -->
      <#list row.placements![] as placement>
        <@cm.include self=placement />
      </#list>
    </#list>
  </div>
</#if>

