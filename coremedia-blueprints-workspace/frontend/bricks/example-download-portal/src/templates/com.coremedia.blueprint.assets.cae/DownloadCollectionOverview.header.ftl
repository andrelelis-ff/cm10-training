<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.cae.DownloadCollectionOverview" -->

<div class="am-download-portal__breadcrumb">
    <ul class="am-breadcrumb">
        <li class="am-breadcrumb__item am-breadcrumb-item am-breadcrumb-item--link">
            <a class="am-breadcrumb-item__text" data-hash-based-fragment-link="">${cm.getMessage("am_download_portal")}</a>
        </li>
        <li class="am-breadcrumb__item am-breadcrumb-item am-breadcrumb-item--child">
            <span class="am-breadcrumb-item__text">${cm.getMessage("am_download_collection_overview")}</span>
        </li>
    </ul>
</div>

<div class="am-download-portal__search">
  <@cm.include self=self view="search"/>
</div>

<h1 class="am-download-portal__title am-heading-1"<@preview.metadata data="properties.name" />>${cm.getMessage("am_download_collection_overview_title")}</h1>
