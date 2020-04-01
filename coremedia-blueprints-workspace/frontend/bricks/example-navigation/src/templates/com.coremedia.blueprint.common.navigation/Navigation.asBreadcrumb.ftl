<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.navigation.Navigation" -->
<#-- @ftlvariable name="navigation" type="com.coremedia.blueprint.common.navigation.Navigation" -->
<#-- @ftlvariable name="classBreadcrumb" type="java.lang.String" -->
<#-- @ftlvariable name="metadata" type="java.util.List" -->

<#assign classBreadcrumb=cm.localParameters().classBreadcrumb!"" />
<#assign hideRootElement=bp.setting(cmpage, "breadcrumbHideRootElement", false) />
<#assign hideLastElement=bp.setting(cmpage, "breadcrumbHideLastElement", false) />

<#-- Show breadcrumb, if more than a root channel exist or detailview of an element is dislayed -->
<#if (self.navigationPathList?size > 1) || cmpage.detailView>
  <ul class="cm-breadcrumb ${classBreadcrumb}">
  <#list self.navigationPathList![] as navigation>
    <#-- link navigation in breadcrumb, if not last or detailview of an element is dislayed -->
    <#if (navigation_has_next || cmpage.detailView)>
      <#if !navigation.isHidden() || (navigation.isRoot() && !hideRootElement)>
        <li class="cm-breadcrumb__item">
          <@cm.include self=navigation view="asLink" />
        </li>
      </#if>
    <#-- last item (leaf) is a channel -->
    <#else>
      <li class="cm-breadcrumb__item active"<@preview.metadata data=[self.content, "properties.teaserTitle"]/>>${self.teaserTitle!""}</li>
    </#if>
  </#list>
  <#--if detailview add content element too as last item (leaf), if not disabled -->
  <#if (cmpage.detailView && !hideLastElement)>
<#--
  TODO: See CMS-9172. We would like to render the metadata like:
  <li class="cm-breadcrumb__item active"<@preview.metadata data=[cmpage.content, "properties.teaserTitle"]/>>${cmpage.content.teaserTitle!""}</li>
-->
  <li class="cm-breadcrumb__item active">${cmpage.content.teaserTitle!""}</li>
  </#if>
  </ul>
</#if>
