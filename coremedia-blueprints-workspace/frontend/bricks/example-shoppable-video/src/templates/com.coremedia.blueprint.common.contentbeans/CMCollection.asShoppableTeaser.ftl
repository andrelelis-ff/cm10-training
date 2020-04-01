<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMCollection" -->

<#list self.items![] as item>
  <@cm.include self=item view="teaser" params={
  "blockClass": "cm-shoppable-teaser",
  "renderWrapper": false,
  "renderTeaserText": true,
  "renderEmptyImage": false,
  "renderLink": false,
  "enableTeaserOverlay": false
  }/>
</#list>
