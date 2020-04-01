<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.contentbeans.AMTaxonomy" -->
<#-- @ftlvariable name="lastItemAsLink" type="java.lang.Boolean" -->

<#assign lastItemAsLink=cm.localParameters().lastItemAsLink!false />
<ul class="am-breadcrumb">
  <li class="am-breadcrumb__item am-breadcrumb-item am-breadcrumb-item--link"><a class="am-breadcrumb-item__text" data-hash-based-fragment-link="">${cm.getMessage("am_download_portal")}</a></li>
  <#list self.taxonomyPathList as category>
    <#assign showAsLink=(category_has_next || lastItemAsLink) />
    <#assign classBreadcrumbItem=showAsLink?string("am-breadcrumb-item--link", "") />
    <li class="am-breadcrumb__item am-breadcrumb-item am-breadcrumb-item--child ${classBreadcrumbItem}">
      <#if showAsLink>
        <@cm.include self=category view="asDownloadPortalLink" params={"class": "am-breadcrumb-item__text"} />
      <#else>
        <span class="am-breadcrumb-item__text"<@preview.metadata data=[category.content, "properties.value"] />>${category.value!""}</span>
      </#if>
    </li>
  </#list>
</ul>
