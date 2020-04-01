<#-- @ftlvariable name="self" type="com.coremedia.blueprint.ecommerce.contentbeans.CMProduct" -->

<@cm.include self=self view="detail" params={
  "relatedView": "asRelated",
  "renderTags": true,
  "renderDate": false
} />
