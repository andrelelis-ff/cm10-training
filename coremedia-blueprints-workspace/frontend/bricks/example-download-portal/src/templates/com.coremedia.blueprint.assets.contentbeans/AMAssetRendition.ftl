<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.contentbeans.AMAssetRendition" -->

<#--
    Template Description:

    This template extends the brick "preview".
-->

<#-- image -->
<#if self.blob?has_content && bp.isDisplayableImage(self.blob)>
  <img <@preview.metadata data=[self.asset.content, "properties." + self.name]/> src="${cm.getLink(self)}" style="max-width: 100%">

<#-- video -->
<#elseif self.blob?has_content && bp.isDisplayableVideo(self.blob)>
  <video <@preview.metadata data=[self.asset.content, "properties." + self.name]/>
          poster=""
          src="${cm.getLink(self.blob)}"
          style="max-width: 100%"
          controls></video>

<#-- error -->
<#else>
  <span<@preview.metadata data=[self.asset.content, "properties." + self.name] />>${cm.getMessage('preview_am_rendition')}</span>
</#if>
