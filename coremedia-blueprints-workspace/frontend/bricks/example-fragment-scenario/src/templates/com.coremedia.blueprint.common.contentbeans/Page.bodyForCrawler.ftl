<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<body>
<#-- Iterate over each row -->
<#if self.pageGrid?has_content>
  <#list self.pageGrid.rows![] as row>
    <#list row.placements![] as placement>
      <#if placement.name! == "main" && cmpage.detailView><#-- replace main section with the main content to render -->
      <div>
        <@cm.include self=cmpage.content/>
      </div>
      </#if>
    </#list>
  </#list>
</#if>
</body>

