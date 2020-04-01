<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.contentbeans.AMAssetRendition" -->
<#-- @ftlvariable name="classBox" type="java.lang.String" -->

<#assign renditionCollectionItemData={
  "assetId" : self.asset.contentId,
  "rendition" : self.name
} />
<#assign linkData={
  "requestParams": {
    "asset": self.asset.contentId
  }
} />
<div class="am-download-collection-item ${classBox}" <@cm.dataAttribute name="data-am-rendition-collection-item" data=renditionCollectionItemData /><@preview.metadata data="properties." + self.name />>
  <@cm.include self=self.asset view="asPictureBox" params={"classBox": "am-download-collection-item__picture-box", "classImage": "am-download-collection-item__picture", "scalePicture": true} />
  <div class="am-download-collection-item__info">
    <h3 class="am-download-collection-item__title am-heading-3">
      <a <@cm.dataAttribute name="data-hash-based-fragment-link" data=linkData />>${self.asset.title!""}</a>
    </h3>
    <div class="am-download-collection-item__description">
      ${cm.getMessage("am_rendition_${self.name}")} (${bp.getDisplayFileSize(self.size)})
    </div>
    <div class="am-download-collection-item__type">
      <#if self.mimeType?has_content>
        <span class="am-rendition-type">${(bp.getDisplayFileFormat(self.mimeType!"")!"bin")?upper_case}</span>
      </#if>
    </div>
    <div class="am-download-collection-item__controls">
      <@cm.include self=self view="asDownloadLink" />
      <@cm.include self=self view="asCollectionControl" />
    </div>
  </div>
</div>
