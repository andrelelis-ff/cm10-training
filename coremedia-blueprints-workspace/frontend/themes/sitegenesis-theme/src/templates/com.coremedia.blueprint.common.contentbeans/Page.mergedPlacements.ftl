<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->
<#-- @ftlvariable name="placement" type="com.coremedia.blueprint.common.layout.PageGridPlacement" -->

<#--
  Custom page template that renders all placement (excluding the header and footer placements) fragments without surrounding HTML page markup.
-->

<#-- Iterate over each row -->
<#if self.pageGrid?has_content>
  <#list self.pageGrid.rows![] as row>
  <#-- Iterate over each placement-->
    <#list row.placements![] as placement>
      <#--Exclude header placement-->
      <#if placement.name != "header" && placement.name != "footer">
        <@cm.include self=placement />
      </#if>
    </#list>
  </#list>
</#if>
