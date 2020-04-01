<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->
<#-- @ftlvariable name="previewFacade" type="com.coremedia.objectserver.view.freemarker.PreviewFacade" -->

<#--
  Template Description:

  Copy of Page.externalHead.ftl in fragment-scenario brick.
  Slider metadata is solved differently in SFCC, so we need to put the information in a html tag of the head element.
-->

<#-- same as in cms-only pages -->
<@cm.include self=self view="_additionalHead" />

<#-- add preview metadata -->
<#if preview.isPreviewCae()>
  <#assign sliderMetadata=bp.setting(cmpage, "sliderMetaData", [])/>
  <script<@preview.metadata data=sliderMetadata />></script>
</#if>
<#-- make the crawler index the coremedia content id -->
<#if self.content.content?has_content && self.content.contentId?has_content>
  <meta name="coremedia_content_id" content="${self.content.contentId!""}"<@preview.metadata data=[self.content.content!bp.getPageMetadata(cmpage)!""]/>>
</#if>
