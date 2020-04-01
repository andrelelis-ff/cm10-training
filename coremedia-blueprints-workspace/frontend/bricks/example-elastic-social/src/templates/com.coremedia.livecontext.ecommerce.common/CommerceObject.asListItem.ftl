<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.common.CommerceObject" -->
<#-- @ftlvariable name="viewItem" type="java.lang.String" -->
<#-- @ftlvariable name="metadata" type="java.util.List" -->

<div class="cm-collection__item"<@preview.metadata metadata![] />>
  <@cm.include self=self view=viewItem!"asTeaser" />
</div>