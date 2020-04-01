<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.search.SearchActionState" -->
<#-- @ftlvariable name="searchResultPageView" type="java.lang.String" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#--
  * Helper function to generate search links including all mandatory and optional parameters
  -->
<#function getLink self params={}>
  <#-- --- required params --- -->

  <#-- query -->
  <#local localParams = { "query": params.query!self.form.query!"" } />
  <#-- page -->
  <#local localParams = localParams + { "page": params.page!cmpage } />
  <#-- view -->
  <#local localParams = localParams + { "view": params.view!searchResultPageView!"asSearchResultPage" } />

  <#-- --- optional params --- -->

  <#-- category filter -->
  <#if params.channelId?has_content || self.form.channelId?has_content>
    <#local localParams = localParams + { "channelId": params.channelId!self.form.channelId } />
  </#if>
  <#-- type filter -->
  <#if params.docType?has_content || self.form.docType?has_content>
    <#local localParams = localParams + { "docType": params.docType!self.form.docType } />
  </#if>
  <#-- facets filter -->
  <#if params.facetFilters?has_content>
    <#local localParams = localParams + { "facetFilters": (self.result.facetResult.filter().toggle(params.facetFilters).build())!"" } />
  <#elseif params.clearFacetFilters?has_content>
    <#local localParams = localParams + { "facetFilters": (self.result.facetResult.filter().clear(params.clearFacetFilters).build())!"" } />
  <#else>
    <#local localParams = localParams + { "facetFilters": (self.result.facetResult.filter().build())!"" } />
  </#if>
  <#-- page number -->
  <#if params.pageNum?has_content || self.form.pageNum?has_content>
    <#local localParams = localParams + { "pageNum": params.pageNum!self.form.pageNum!0} />
  </#if>
  <#-- sort by date -->
  <#if params.sortByDate?has_content || self.form.sortByDate!false>
    <#local localParams = localParams + { "sortByDate": params.sortByDate!self.form.sortByDate } />
  </#if>

  <#return cm.getLink(self, localParams) />
</#function>

<#--
  * Helper function to render the externalyDisplayDate in the search results
  -->
<#macro renderDate date cssClass="cm-search-result__date">
  <#if date?has_content>
    <@utils.renderDate date=date.time cssClass=cssClass metadata=["properties.externallyDisplayedDate"] />
  </#if>
</#macro>
