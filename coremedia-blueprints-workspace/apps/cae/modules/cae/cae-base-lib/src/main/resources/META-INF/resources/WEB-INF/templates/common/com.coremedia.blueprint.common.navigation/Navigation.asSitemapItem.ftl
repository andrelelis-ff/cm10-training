<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.navigation.Navigation" -->
<#-- @ftlvariable name="maxDepth" type="java.lang.Integer" -->

<#--
    Template Description:

    This template renders a navigation tree as link and its children as list for a sitemap.
-->

<#assign maxDepth=cm.localParameter("maxDepth", 0) />

<li class="cm-sitemap__item">
  <#-- link to this item in navigation -->
  <a href="${cm.getLink(self)}">${self.title!""}</a>

  <#-- include child items visible in sitemap, if exist -->
  <#if self.sitemapChildren?has_content>
    <@cm.include self=self view="asSitemapTree" params={"maxDepth": maxDepth} />
  </#if>
</li>

