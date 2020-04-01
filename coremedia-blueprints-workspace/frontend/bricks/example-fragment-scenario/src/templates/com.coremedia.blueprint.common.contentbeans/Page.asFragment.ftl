<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<#-- Pagegrid: Iterate over each row -->
<#if cmpage.pageGrid?has_content>
  <div class="cm-grid ${self.pageGrid.cssClassName!""}" <@preview.metadata data=bp.getPageMetadata(cmpage)!"" />>
    <#-- Iterator over each row -->
    <#list self.pageGrid.rows![] as row>
      <#-- Iterate over each placement-->
      <div class="cm-row">
        <#list row.placements![] as placement>
          <#-- do not render header and footer placements -->
          <#if !["header", "footer"]?seq_contains(placement.name?lower_case)>
            <@cm.include self=placement/>
          <#else>
            <!-- ## Not rendered: ${placement.name} ## -->
          </#if>
        </#list>
      </div>
    </#list>
  </div>
</#if>
