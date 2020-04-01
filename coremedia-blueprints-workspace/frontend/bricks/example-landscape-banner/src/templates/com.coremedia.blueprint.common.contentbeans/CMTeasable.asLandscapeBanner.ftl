<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#--
  Template Description:

  This templates utilizes the "teaser" view of the brick "default-teaser" to configure the banner accordingly.
  All passed template parameters will be delegated to the "teaser" view.
  Do not render an inline video, instead always render the picture or the emptyImage fallback.

  @since 1907
-->

<@cm.include self=self view="teaser[]" params={
  "blockClass": "cm-landscape-banner",
  "renderWrapper": false,
  "renderTeaserText": true,
  "renderEmptyImage": true,
  "enableTeaserOverlay": false,
  "inlineMedia": self.picture!cm.UNDEFINED,
  "renderInlineMediaOnly": true
} + cm.localParameters() />
