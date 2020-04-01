<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.search.SearchActionState" -->

<#import "../../freemarkerLibs/search.ftl" as search />

<#--
    Template Description:

    Displays a simple search pagination with links to the first, previous, next and last page of the results.
    Between the links are the current page and the number of all pages shown.
-->

<#assign searchForm=self.form!cm.UNDEFINED />
<#assign searchResultPerPage=(self.result.hitsPerPage)!10 />
<#assign pageNumber=(searchForm.pageNum!0)+1 />
<#assign totalPages=(((self.result.numHits)!0) / searchResultPerPage)?ceiling />

<div class="cm-search__pagination" role="navigation" aria-label="${cm.getMessage("search_aria_label_pagination")}">
  <#-- first page -->
  <#if (pageNumber > 1) >
    <span class="cm-search__pagination-first" data-cm-search-link="${search.getLink(self, {"pageNum": 0})}" aria-label="${cm.getMessage("search_aria_label_pagination_first")}">
      <span></span>
    </span>
  </#if>

  <#-- previous page -->
  <#if (pageNumber > 1) >
    <span class="cm-search__pagination-prev" data-cm-search-link="${search.getLink(self, {"pageNum": pageNumber-2})}" aria-label="${cm.getMessage("search_aria_label_pagination_prev")}">
      <span></span>
    </span>
  </#if>

  <#-- index -->
  <#if (totalPages > 1) >
    <span class="cm-search__pagination-index">${pageNumber} / ${totalPages}</span>
  </#if>

  <#-- next page -->
  <#if (pageNumber < totalPages) >
    <span class="cm-search__pagination-next" data-cm-search-link="${search.getLink(self, {"pageNum": pageNumber})}" aria-label="${cm.getMessage("search_aria_label_pagination_next")}">
      <span></span>
    </span>
  </#if>

  <#-- last page -->
  <#if (pageNumber < totalPages) >
    <span class="cm-search__pagination-last" data-cm-search-link="${search.getLink(self, {"pageNum": totalPages-1})}" aria-label="${cm.getMessage("search_aria_label_pagination_last")}">
      <span></span>
    </span>
  </#if>
</div>

