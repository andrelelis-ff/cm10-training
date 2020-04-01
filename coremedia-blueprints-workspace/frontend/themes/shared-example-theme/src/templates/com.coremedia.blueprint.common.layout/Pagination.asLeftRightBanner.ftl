<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Pagination" -->

<#--
    Template Description:

    This template is used for CMQueryLists inside PagegridPlacements without layout variant, if "load More" is enabled.

    @since 1907
-->

<#assign pageNumber=self.pageNum!0 />
<#assign nextPageNumber=pageNumber+1 />
<#assign numberOfPages=self.numberOfPages />
<#assign pagePrefixId=bp.generateId("cm-pagination-page") />
<#assign itemStartsWithEven=(((pageNumber*(self.itemsPerPage))+1)%2 == 0) />

<#-- list paginated items for this page, starting with 0 -->
<div id="${pagePrefixId}" class="cm-pagination__block" data-cm-page="${pageNumber}"<@preview.metadata "properties.items"/>>
  <#list self.items![] as item>
    <#assign itemEven=itemStartsWithEven?then(item?is_odd_item, item?is_even_item) />
    <div class="cm-left-right-banner-grid__item">
      <@cm.include self=item view="asLeftRightBanner" params={"even": itemEven} />
    </div>
  </#list>
</div>

<#-- show button to load more results via ajax -->
<#if (nextPageNumber < numberOfPages)>
  <button disabled class="cm-button cm-pagination__more" data-cm-pagination-page="${cm.getLink(self, {"view": "asLeftRightBanner", "pageNum": nextPageNumber})}">
    ${cm.getMessage("pagination_load_more")}
  </button>
  <div class="cm-pagination__loading" data-cm-pagination-loading></div>
</#if>

