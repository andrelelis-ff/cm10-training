<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.search.SearchActionState" -->
<#-- @ftlvariable name="facetItem" type="com.coremedia.blueprint.cae.search.facet.FacetValue" -->

<#import "../../freemarkerLibs/search.ftl" as search />

<#--
    Template Description:

    Displays all enabled and available filters for a search.
    The folllowing filters are available:
     - categories
     - facets (multiple facets like doctypes, tags, locations ...)
-->

<#assign enableCategory=cm.localParameters().enableCategory!true />
<#assign enableFacets=cm.localParameters().enableFacets!true />

<#assign searchResult=self.result!cm.UNDEFINED/>
<#assign searchCategories=bp.setting(self, "searchChannelSelect", bp.setting(self, "search.channelselect", []))/>
<#assign searchActiveCategory=self.form.channelId!"">
<#assign searchFacets=(searchResult.facetResult.getFacets())!{}/>

<#-- show filters only if at least one filter is enabled and with at least one entry-->
<#if ((enableCategory && searchCategories?size > 0) || (enableFacets && searchFacets?size > 0))>
  <div class="cm-search__filters">
    <#-- 1. filter: categories -->
    <#if enableCategory>
      <#list searchCategories>
        <div class="cm-search__filter" data-cm-search-filter="category">
          <h3 class="cm-search__filter-title" data-cm-search-filter-toggle="">
            ${cm.getMessage("search_filter_category")}
            <i class="cm-search__filter-title-icon"></i>
          </h3>
          <ul class="cm-search__filter-list" data-cm-search-filter-links="">
            <#if searchActiveCategory?has_content>
              <li>
                <i class="cm-button__icon cm-search__filter-all-icon"></i>
                <span class="cm-search__filter-list-link cm-search__link" data-cm-search-link="${search.getLink(self, {"channelId": ""})}">${cm.getMessage("search_filter_all_categories")}</span>
              </li>
            </#if>
            <#items as category>
               <li class="cm-search__filter-list-item">
                 <#if searchActiveCategory == category.contentId?string>
                   ${category.title!""}
                 <#else>
                   <span class="cm-search__filter-list-link cm-search__link" data-cm-search-link="${search.getLink(self, {"channelId": category.contentId})}">${category.title!""}</span>
                 </#if>
               </li>
            </#items>
          </ul>
        </div>
      </#list>
    </#if>

    <#-- 2. filter: facets -->
    <#if enableFacets>
      <#-- iterate over all enabled facets -->
      <#list searchFacets as facet, facetList>
        <#list facetList![]>
          <#assign facetLabel=cm.getMessage("search_filter_" + facet)?has_content?then(cm.getMessage("search_filter_" + facet), facet) />
          <div class="cm-search__filter" data-cm-search-filter="facet">
            <h3 class="cm-search__filter-title" data-cm-search-filter-toggle="">
              ${facetLabel}
              <i class="cm-search__filter-title-icon"></i>
            </h3>
            <ul class="cm-search__filter-list" data-cm-search-filter-links="">
              <#-- add "all" entry, if a filter is selected -->
              <#if (searchResult.facetResult.isFacetWithFilter(facet))>
              <#assign allLabel=cm.getMessage("search_filter_all_" + facet)?has_content?then(cm.getMessage("search_filter_all_" + facet), cm.getMessage("search_filter_all")) />
              <li>
                <i class="cm-button__icon cm-search__filter-all-icon"></i>
                <span class="cm-search__filter-list-link cm-search__link" data-cm-search-link="${search.getLink(self, {"clearFacetFilters": facet})}">${allLabel}</span>
              </li>
              </#if>
              <#items as facetItem>
                <li class="cm-search__filter-list-item" data-cm-search-link="${search.getLink(self, {"facetFilters": facetItem})}">
                  <#assign itemId=bp.generateId("filter_item") />
                  <#-- use the label of the facetItem, if empty use a translation of it with a key like "search_filter_document_CMArticle -->
                  <#assign facetLabel=facetItem.label?has_content?then(facetItem.label, cm.getMessage("search_filter_" + facet + "_" + facetItem.value)) />
                    <input id="${itemId}" class="cm-search__filter-list-checkbox" type="checkbox" ${facetItem.filter?then("checked", "")}>
                    <span class="cm-search__filter-list-checkbox-icon ${facetItem.filter?then("cm-search__filter-list-checkbox-icon--checked", "")}"></span>
                    <label for="${itemId}" class="cm-search__filter-list-link ${facetItem.filter?then("cm-search__filter-list-link--checked", "cm-search__link")}">
                    ${facetLabel}
                    <span class="cm-search__filter-list-count">${facetItem.count}</span>
                  </label>
                </li>
              </#items>
            </ul>
          </div>
        </#list>
      </#list>
    </#if>
  </div>
</#if>
