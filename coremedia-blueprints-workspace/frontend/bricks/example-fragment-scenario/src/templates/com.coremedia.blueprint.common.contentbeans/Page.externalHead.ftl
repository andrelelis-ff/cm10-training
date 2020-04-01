<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->
<#-- @ftlvariable name="previewFacade" type="com.coremedia.objectserver.view.freemarker.PreviewFacade" -->

<#-- same as in cms-only pages -->
<@cm.include self=self view="_additionalHead" />

<#-- add preview metadata -->
<#if preview.isPreviewCae()>
  <#assign sliderMetadata=bp.setting(cmpage, "sliderMetaData", "")/>
  <#assign previewMetadata = previewFacade.metadata(lc.previewMetaData())>
  <#assign sliderMetadata = previewFacade.metadata(sliderMetadata)>
  <!--CM { "objectType":"page","renderType":"metadata","pbe":${previewMetadata!""}, "slider":${sliderMetadata!""} } CM-->
</#if>
<#-- make the crawler index the coremedia content id -->
<#if self.content.content?has_content && self.content.contentId?has_content>
  <meta name="coremedia_content_id" content="${self.content.contentId!""}"<@preview.metadata data=[self.content.content!bp.getPageMetadata(cmpage)!""]/>>
</#if>
