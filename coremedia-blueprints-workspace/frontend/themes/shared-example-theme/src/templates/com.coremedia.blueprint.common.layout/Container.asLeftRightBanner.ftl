<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Container" -->

<#--
    Template Description:

    This template renders all items as flattened list with the view "asLeftRightBanner" and adds pagination, if available.

    @since 1907
-->

<#assign even=cm.localParameters().even!false />
<#assign isPaginated=self.isPaginated() />
<#assign modifierClass=isPaginated?then("cm-left-right-banner-container--paginated", "") />

<div class="cm-left-right-banner-container ${modifierClass}" <@preview.metadata data=bp.getContainerMetadata(self) + [bp.getPlacementHighlightingMetaData(self)!""] />>
  <#-- headline -->
  <#if self.teaserTitle?has_content>
    <h2 class="cm-left-right-banner-container__headline" <@preview.metadata "properties.teaserTitle"/>>${self.teaserTitle}</h2>
  </#if>
  <#-- teasertext -->
  <#if self.teaserText?has_content>
    <div class="cm-left-right-banner-container__text" <@preview.metadata "properties.teaserText"/>>
      <@cm.include self.teaserText />
    </div>
  </#if>
  <div class="cm-left-right-banner-container__items cm-left-right-banner-grid">
    <#--items paginated -->
    <#if isPaginated>
      <@cm.include self=self.asPagination() view="asLeftRightBanner" />
    <#else>
      <#list self.flattenedItems![] as item>
        <div class="cm-left-right-banner-grid__item">
          <@cm.include self=item view="asLeftRightBanner" params={"even": even?then(item?is_odd_item, item?is_even_item)} />
        </div>
      </#list>
    </#if>
  </div>
</div>
