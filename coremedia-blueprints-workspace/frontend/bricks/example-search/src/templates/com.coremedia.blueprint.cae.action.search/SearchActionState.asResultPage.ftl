<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.search.SearchActionState" -->
<#-- @ftlvariable name="category" type="com.coremedia.blueprint.common.contentbeans.CMChannel" -->

<#import "../../freemarkerLibs/search.ftl" as search />
<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />

<#--
    Template Description:

    Displays the search result page. It includes a header with title and status, like number of results or error text.
    Below the header are the search results rendered by the template "SearchActionState.asResultList.ftl".
    It is possible to enable a normal pagination at the bottom of the page instead of dynamically load more results
    via ajax. Add a Boolean Content Setting "searchResultPagination" to the Search Configuration in the site
    and set it to "true".
-->

<#-- escape query before using it in the status, which is not escaped to avoid XSS -->
<#assign searchQuery=self.form.query!""/>
<#assign searchResult=self.result!cm.UNDEFINED/>
<#assign searchResultHits=searchResult.hits![]/>
<#assign searchResultView=cm.localParameters().searchResultView!"asSearchResult" />
<#assign searchResultPageView=cm.localParameters().searchResultPageView!"asSearchResultPage" />
<#assign searchPagination=bp.setting(self, "searchResultPagination", false) />
<#assign searchSpellSuggestions=!bp.setting(self, "searchDisableSpellingSuggestions", false) />
<#assign searchCategories=bp.setting(self, "searchChannelSelect", bp.setting(self, "search.channelselect", []))/>
<#assign searchFacets=(searchResult.facetResult.getFacets())!{}/>
<#assign isSortByDate=self.form.sortByDate!false />
<#-- enable filter at least one filter -->
<#assign searchFiltersAvailable=(searchCategories?size > 0 || searchFacets?size > 0)>

<div id="cm-search-result-page" class="cm-search cm-search--results">

  <#-- results header -->
  <div class="cm-search__header">
    <#-- headline -->
    <h1 class="cm-search__headline">${cm.getMessage("search_results")}</h1>
    <#-- box with infos about this search -->
    <div class="cm-search__status">
      <#if searchResultHits?has_content>
        ${cm.getMessage("search_searchTerm", [searchResult.numHits, searchQuery?esc?markup_string])?no_esc}
      </#if>
    </div>
  </div>

  <#-- results main area -->
  <div class="cm-search__wrapper">

    <#-- search input -->
    <div class="cm-search--form-result-page ${searchFiltersAvailable?then("", "cm-search__form-without-filters")}">
      <form class="cm-search--form" data-cm-search-form-submit="${search.getLink(self, {"query": ""})}" autocomplete="off" role="search">
        <fieldset class="cm-search__form-fieldset">
          <label for="cm-search-result-page-query" class="cm-search__form-label">${cm.getMessage("search_label")}</label>
          <input id="cm-search-result-page-query" placeholder="${cm.getMessage("search_placeholder")}" type="search" class="cm-search__form-input" value="${searchQuery}" data-cm-search-form-input="" required/>
        </fieldset>
        <@components.button text=cm.getMessage("search_button_label") baseClass="" attr={"type": "submit", "class": "cm-search__form-button", "title":  self.action.title!"", "metadata": "properties.title"} />
      </form>
    </div>

    <#-- search filter button on mobile, only if results are available -->
    <#if searchResultHits?has_content>
      <div class="cm-search__filter-switch">
        <@components.button iconClass="cm-search__filter-switch-icon" text=cm.getMessage("search_filters") attr={"class": "cm-search__filter-switch-button", "data-cm-search-filter-popup-toggle": ""}/>
      </div>
    </#if>

    <div class="cm-search__filter-popup" data-cm-search-filter-popup="">

      <#-- headline and close button -->
      <div class="cm-search__filter-popup-head">
        <h2 class="cm-search__filter-popup-heading">${cm.getMessage("search_filters")}</h2>
        <@components.button baseClass="" iconClass="cm-search__filter-close-icon" text=cm.getMessage("search_button_close") attr={"class": "cm-search__filter-popup-close", "data-cm-search-filter-popup-toggle": ""}/>
      </div>

      <#-- search sort, only if results are available -->
      <div class="cm-search__sort">
        <#if searchResultHits?has_content>
          <label for="cm-search-sort" class="cm-search__sort-label">
            ${cm.getMessage("search_sort_by")}
          </label>
          <select id="cm-search-sort" class="cm-search__sort--dropdown" data-cm-search-dropdown>
            <option data-cm-search-sort-link="${search.getLink(self, {"sortByDate": false})}">${cm.getMessage("search_sort_by_relevance")}</option>
            <option <#if isSortByDate>selected</#if> data-cm-search-sort-link="${search.getLink(self, {"sortByDate": true})}">
              ${cm.getMessage("search_sort_by_date")}
            </option>
          </select>
        </#if>
      </div>

      <#-- filters -->
      <@cm.include self=self view="_filters" params={
        "enableCategory": true,
        "enableFacets": true
      } />
    </div>

    <#-- results -->
    <div id="cm-search-results" class="${searchFiltersAvailable?then("cm-search__results", "cm-search__results-without-filters")}">
      <#if searchResultHits?has_content>
        <@cm.include self=self view="asResultList" params={"searchResultView": searchResultView, "loadMore": !searchPagination} />
      <#else>
        <#if self.queryTooShort!false>
          <h3 class="cm-search__warning">${cm.getMessage("search_error_belowMinQueryLength", [self.minimalSearchQueryLength])}</h3>
        <#else>
          <h3 class="cm-search__warning">${cm.getMessage("search_error_noresults", [searchQuery?esc?markup_string])?no_esc}</h3>
        </#if>
        <#-- optional spell suggestion-->
        <#if (searchSpellSuggestions && searchResult.spellSuggestion?has_content)>
          <div class="cm-search__suggestion">
            ${cm.getMessage("search_spell_suggestion")}
            <span class="cm-search__suggestion-link cm-search__link" data-cm-search-link="${search.getLink(self, {"query": searchResult.spellSuggestion})}" data-cm-search-suggestion="${searchResult.spellSuggestion}">${searchResult.spellSuggestion}</span>
          </div>
          </#if>
      </#if>

      <#-- optional pagination -->
      <#if searchPagination>
        <@cm.include self=self view="_pagination" />
      </#if>
    </div>
  </div>
</div>
