<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.search.SearchActionState" -->

<#--
    Template Description:

    Displays all search results with the view "asSearchResult". This template is used as part of SearchActionState.ftl.
    It can also be rendered as fragment via ajax call, if the pagination is disabled (loadMore = true, default).
-->

<#assign showLoadMoreButton=cm.localParameters().loadMore!true />
<#assign searchResultView=cm.localParameters().searchResultView!"asSearchResult" />
<#assign searchResultHits=(self.result.hits)![]/>
<#assign pageNumber=(self.form.pageNum!0)/>

<#-- list search results -->
<div id="page${pageNumber}" class="cm-search__results-block">
  <#list searchResultHits as hit>
    <@cm.include self=hit view=searchResultView params={"highlightingItem": self.result.getHighlightingResultsItem(hit)!{}} />
  </#list>
</div>

<#-- show button to load more results via ajax (default = true) -->
<#if showLoadMoreButton>
  <@cm.include self=self view="_loadMoreButton" />
</#if>
