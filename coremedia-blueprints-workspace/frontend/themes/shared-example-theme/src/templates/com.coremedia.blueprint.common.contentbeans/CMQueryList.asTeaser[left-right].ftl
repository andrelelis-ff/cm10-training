<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMQueryList" -->

<#--
    Template Description:

    This template renders a CMQueryList with the view "asTeaser", which is used if a CMQueryList is added to a
    PageGridPlacement without layout variant. If pagination is enabled this template redirects to Pagination.asLeftRightBanner.ftl
    otherwise it renders the items as Container.asTeaser.ftl does.

    @since 1901
-->

<#assign isPaginated=self.isPaginated() />
<#assign modifierClass=isPaginated?then("cm-left-right-banner-container--paginated", "") />

<div class="cm-left-right-banner-container ${modifierClass}"<@preview.metadata self.content />>
  <div class="cm-left-right-banner-container__items cm-left-right-banner-grid">
    <#if isPaginated>
      <@cm.include self=self.asPagination() view="asLeftRightBanner" />
    <#else>
      <#list self.items![] as item>
        <div class="cm-left-right-banner-grid__item">
          <@cm.include self=item view="asTeaser" params={"even": item?is_even_item}/>
        </div>
      </#list>
    </#if>
  </div>
</div>
