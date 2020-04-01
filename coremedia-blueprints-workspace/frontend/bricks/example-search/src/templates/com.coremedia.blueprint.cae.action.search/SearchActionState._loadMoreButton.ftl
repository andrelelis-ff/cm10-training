<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.search.SearchActionState" -->

<#import "../../freemarkerLibs/search.ftl" as search />

<#--
    Template Description:

    Displays a "load more" button below the results. A javascript click handler will load more results via ajax.
-->

<#assign searchResult=self.result!cm.UNDEFINED/>
<#assign pageNumber=(self.form.pageNum!0)+1/>
<#assign searchResultPerPage=searchResult.hitsPerPage!10 />

<#if ((pageNumber * searchResultPerPage) < searchResult.numHits)>
  <button disabled class="cm-search__more" data-cm-search-results="${search.getLink(self, {"view": "asResultList", "pageNum": pageNumber})}">
    ${cm.getMessage("search_load_more")}
  </button>
  <div class="cm-search__loading" data-cm-search-loading></div>
</#if>
