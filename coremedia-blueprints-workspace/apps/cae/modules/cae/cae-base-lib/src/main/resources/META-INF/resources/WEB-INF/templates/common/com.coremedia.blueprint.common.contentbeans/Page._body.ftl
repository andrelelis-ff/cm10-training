<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->
<#-- @ftlvariable name="self.content" type="com.coremedia.blueprint.common.contentbeans.CMChannel" -->

<#--
    Template Description:

    This template renders the body of every page with its content or the pagegrid and its placements. It includes also
    javascripts and the hook VIEW_HOOK_END at the end the body element. The setting "sliderMetaData" defines the
    responsive device slider in the CoreMedia Studio above the preview.
    Please check the brick "bootstrap" or "generic templates" for a more detailed version.
-->

<body<@preview.metadata data=bp.setting(self, "sliderMetaData", "")/>>

  <#-- show pagegrid -->
  <@cm.include self=self.pageGrid />

  <#-- include javascript files at the end -->
  <@cm.include self=self view="_bodyEnd" />
</body>
