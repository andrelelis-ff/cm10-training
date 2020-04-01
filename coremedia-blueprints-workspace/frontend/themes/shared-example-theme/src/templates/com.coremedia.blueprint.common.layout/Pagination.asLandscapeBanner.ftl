<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Pagination" -->

<#--
    Template Description:

    This template is used for CMQueryLists inside the related property of CMPerson, if "load More" is enabled.

    @since 1901
-->

<#assign pageNumber=self.pageNum!0 />
<#assign nextPageNumber=pageNumber+1 />
<#assign numberOfPages=self.numberOfPages />
<#assign pagePrefixId=bp.generateId("cm-pagination-page") />

<#-- list paginated items for this page, starting with 0 -->
<div id="${pagePrefixId}" class="cm-pagination__block" data-cm-page="${pageNumber}">
  <@cm.include self=bp.getContainer(self.items) view="asLandscapeBanner" params={"additionalClass": "cm-related__items"} />
</div>

<#-- show button to load more results via ajax -->
<#if (nextPageNumber < numberOfPages)>
  <button disabled class="cm-pagination__more" data-cm-pagination-page="${cm.getLink(self, {"view": "asLandscapeBanner", "pageNum": nextPageNumber})}">
    ${cm.getMessage("pagination_load_more")}
  </button>
  <div class="cm-pagination__loading" data-cm-pagination-loading></div>
</#if>

