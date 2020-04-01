<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.navigation.Navigation" -->
<#-- @ftlvariable name="maxDepth" type="java.lang.Integer" -->

<#--
    Template Description:

    This template renders a navigation tree as list for a sitemap. See following templates
    - CMSitemap.ftl
    - CMLinkable.asSitemapItem.ftl
    - Navigation.asSitemapItem.ftl

    More informations can be found in the Blueprint Developer Manual, chapter "Content Type Sitemap".
-->

<#assign maxDepth=cm.localParameter("maxDepth", 0) />
<#assign cssClass=cm.localParameter("cssClass", "")/>
<#assign childrenCssClass=cm.localParameter("childrenCssClass", "")/>

<#-- check if navigation has visible in sitemap children and max tree depth isn't reached yet -->
<#if self.sitemapChildren?has_content && (maxDepth > 0)>

  <#-- list children visible in sitemap -->
  <#list self.sitemapChildren![]>
    <ul class="cm-sitemap__items">
      <#items as child>
        <@cm.include self=child view="asSitemapItem" params={"maxDepth": maxDepth-1} />
      </#items>
    </ul>
  </#list>
</#if>
