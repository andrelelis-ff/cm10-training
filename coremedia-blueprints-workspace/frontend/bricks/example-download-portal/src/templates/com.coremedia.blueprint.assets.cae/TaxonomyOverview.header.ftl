<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.cae.TaxonomyOverview" -->

<div class="am-download-portal__breadcrumb">
  <ul class="am-breadcrumb">
      <li class="am-breadcrumb__item am-breadcrumb-item am-breadcrumb-item--link">
        <a class="am-breadcrumb-item__text" data-hash-based-fragment-link="">${cm.getMessage("am_download_portal")}</a>
      </li>
        <li class="am-breadcrumb__item am-breadcrumb-item am-breadcrumb-item--child">
          <span class="am-breadcrumb-item__text"<@preview.metadata data=[self.taxonomy.content, "properties.value"] />>${cm.getMessage("am_tag")}: ${self.taxonomy.value!""}</span>
        </li>
    </ul>
</div>

<@cm.include self=self view="downloadCollection"/>

<div class="am-download-portal__search">
<@cm.include self=self view="search"/>
</div>
<h1 class="am-download-portal__title am-heading-1">
  ${cm.getMessage("am_download_portal")}
</h1>
