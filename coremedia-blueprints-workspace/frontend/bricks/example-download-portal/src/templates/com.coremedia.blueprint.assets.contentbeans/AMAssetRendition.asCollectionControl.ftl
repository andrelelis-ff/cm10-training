<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.contentbeans.AMAssetRendition" -->

<#if self.blob?has_content>
  <#assign controlData={
    "assetId" : self.asset.contentId,
    "rendition" : self.name
  } />
  <span class="am-download-collection-rendition-control" <@cm.dataAttribute name="data-am-download-collection-rendition-control" data=controlData />>
    <button type="button" class="am-download-collection-rendition-control__add am-text-link-with-icon">
      <span class="am-text-link-with-icon__icon am-icon am-icon--add"></span>
      <span class="am-text-link-with-icon__text">${cm.getMessage("am_add_rendition_to_download_collection")}</span>
    </button>
    <button type="button" class="am-download-collection-rendition-control__remove am-text-link-with-icon">
      <span class="am-text-link-with-icon__icon am-icon am-icon--remove"></span>
      <span class="am-text-link-with-icon__text">${cm.getMessage("am_remove_rendition_from_download_collection")}</span>
    </button>
  </span>
</#if>
