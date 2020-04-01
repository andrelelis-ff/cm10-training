<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<#--
  Template Description:

  This template includes all JavaScripts at the end of the page. Same as in cms-only pages. Could be used as a
  fragment in a LiveContext environment. Please check the brick "fragment-scenario" for a more detailed version.
-->

<#assign sliderMetadata=bp.setting(self, "sliderMetaData", []) />
<div style="display: none"<@preview.metadata data=sliderMetadata />></div>

<@cm.include self=self view="_bodyEnd" />
