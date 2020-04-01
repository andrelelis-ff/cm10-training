<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<#-- Breadcrumb above Pagegrid for IBM -->
<div class="cm-breadcrumb--outer">
  <@cm.include self=self.navigation!cm.UNDEFINED view="asBreadcrumbFragment"/>
</div>

<#-- Pagegrid: Iterate over each row -->
<#if cmpage.pageGrid?has_content>
  <div class="cm-grid cm-grid--1-per-row ${self.pageGrid.cssClassName!""}" <@preview.metadata data=bp.getPageMetadata(cmpage)!"" />>
    <#-- Iterator over each row -->
    <#list self.pageGrid.rows![] as row>
      <div class="cm-grid__item cm-row">
      <#-- Iterate over each placement-->
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
