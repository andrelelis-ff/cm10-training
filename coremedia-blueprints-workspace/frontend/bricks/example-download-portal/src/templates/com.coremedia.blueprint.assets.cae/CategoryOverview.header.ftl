<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.cae.CategoryOverview" -->

<div class="am-download-portal__subheader">
  <div class="am-download-portal__breadcrumb">
  <#if self.category?has_content>
      <@cm.include self=self.category view="_breadcrumb" params={"lastItemAsLink": false} />
    </#if>
  </div>

<@cm.include self=self view="downloadCollection"/>
</div>

<div class="am-download-portal__search">
<@cm.include self=self view="search"/>
</div>

<h1 class="am-download-portal__title am-heading-1">
  ${cm.getMessage("am_download_portal")}
  <#if self.category?has_content>
    - <span class="am-download-portal__category"<@preview.metadata data=[self.category.content, "properties.value"] />>${self.category.value!""}</span>
  </#if>
</h1>

