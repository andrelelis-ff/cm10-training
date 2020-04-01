<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#--
  Template Description:

  This templates utilizes the "teaser" view of the brick "default-teaser" to configure the banner accordingly.

  All passed template parameters will be delegated to the "teaser" view.

  @since 1907
-->

<@cm.include self=self view="teaser" params={
  "blockClass": "cm-square-banner",
  "renderWrapper": false,
  "renderTeaserText": true,
  "renderEmptyImage": true,
  "renderDimmer": false,
  "enableTeaserOverlay": true,
  "mediaView": "squareBannerMedia",
  "renderVideoInline": true
} + cm.localParameters() />
