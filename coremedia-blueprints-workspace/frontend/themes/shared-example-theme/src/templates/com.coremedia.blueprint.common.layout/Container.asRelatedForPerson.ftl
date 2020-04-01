<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Container" -->

<#--
    Template Description:

    If pagination is enabled, it delegates to the Pagination object with the template Pagination.asLandscapeBanner.ftl,
    otherwise it redirects to the default view "asLandscapeBanner".

    @since 1901
-->

<#assign relatedView=cm.localParameters().relatedView!"asLandscapeBanner" />

<#if self.isPaginated()!false>
  <@cm.include self=self.asPagination() view=relatedView />
<#else>
  <@cm.include self=self view=relatedView />
</#if>
