<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMVideo" -->

<#--
  Template Description:

  This templates utilizes the "teaser" view of the brick "default-teaser" to configure the banner accordingly.

  All passed template parameters will be delegated to the "teaser" view.

  @since 1907
-->

<@cm.include self=self view="teaser" params={
  "blockClass": "cm-hero-banner",
  "renderWrapper": false,
  "renderVideoInline": true,
  "inlineMedia": self
} + cm.localParameters() />
