<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.cae.CategoryOverview" -->

<div class="am-category-overview">

  <div class="am-category-overview__sub-categories">
    <#assign subcategories=self.subcategories />
    <#if subcategories?has_content>
      <h2 class="am-heading-2">${cm.getMessage("am_categories")}</h2>
      <#list subcategories as subcategory>
        <div class="am-category-overview__sub-category">
          <@cm.include self=subcategory.category view="asTeaser" params={"assetCount" : subcategory.assetsInCategory}/>
        </div>
      </#list>
    </#if>
  </div>

  <div class="am-category-overview__paginated-assets">
      <#-- see AMPaginatedAssetsPredicate -->
      <@cm.include self=self view="_assets" />
  </div>

</div>

