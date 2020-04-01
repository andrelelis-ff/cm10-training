<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPlaceholder" -->
<#-- @ftlvariable name="cmpage.content" type="com.coremedia.blueprint.common.contentbeans.CMChannel" -->

<#-- load page/category as hero (only on categories/channels) -->
<div class="cm-category"<@preview.metadata self.content />>
  <@cm.include self=cmpage.content view="asHeroBanner" params={"additionalClass": "cm-hero--category"} />
</div>
