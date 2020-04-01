<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.cae.AssetDetails" -->

<!-- todo: <@preview.metadata data=self.asset.content /> -->

<div class="am-download-portal__breadcrumb"<@preview.metadata data="properties.assetTaxonomy" />>
<#if self.category?has_content>
    <@cm.include self=self.category view="_breadcrumb" params={"lastItemAsLink": true} />
</#if>
</div>


<@cm.include self=self view="downloadCollection"/>

<div class="am-download-portal__search">
<@cm.include self=self view="search"/>
</div>

<h1 class="am-download-portal__title am-heading-1"<@preview.metadata data="properties.name" />>${self.asset.title!""}</h1>
