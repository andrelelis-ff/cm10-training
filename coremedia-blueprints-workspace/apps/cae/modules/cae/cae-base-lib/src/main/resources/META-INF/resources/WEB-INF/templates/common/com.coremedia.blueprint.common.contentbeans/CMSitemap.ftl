<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMSitemap" -->

<#--
    Template Description:

    This template renders a CMSitemap navigation tree as list (without the root element). It uses the following
    templates recursively until the max depth is reached. The default is 3.
    - Navigation.asSitemapTree.ftl
    - Navigation.asSitemapItem.ftl
    - CMLinkable.asSitemapItem.ftl
-->

<div class="cm-sitemap"<@preview.metadata self.content />>
  <#-- title -->
  <h1 class="cm-sitemap__title"<@preview.metadata "properties.title"/>>${self.title!""}</h1>
  <#-- tree of navigation with default max depth of 3 -->
  <#if self.root?has_content>
    <@cm.include self=self.root view="asSitemapTree" params={"maxDepth": self.sitemapDepth!3} />
  </#if>
</div>
