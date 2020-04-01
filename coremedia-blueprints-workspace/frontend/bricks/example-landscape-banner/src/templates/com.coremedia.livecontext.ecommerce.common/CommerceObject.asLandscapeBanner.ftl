<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.common.CommerceObject" -->

<#--
  Template Description:

  This templates utilizes the "teaser" view of the brick "default-teaser" to configure the banner accordingly.

  All passed template parameters will be delegated to the "teaser" view.

  @since 1907
-->

<@cm.include self=self view="teaser" params={
  "blockClass": "cm-landscape-banner",
  "renderWrapper": false,
  "renderTeaserText": true,
  "renderEmptyImage": false,
  "enableTeaserOverlay": false
} + cm.localParameters() />
