<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.contentbeans.AMAsset" -->

<div class="am-overlay">
  <div class="am-overlay--shadow am-rendition">
    <div class="am-overlay--content">
      <div class="am-icon am-overlay__close-button"/>
      <div class="am-overlay__checkboxes">
        <#list self.publishedRenditions as rendition>
          <div class="am-overlay__checkbox">
            <input id="checkbox-${rendition.name}-${self.contentId}" type="checkbox" <@cm.dataAttribute name="data-am-overlay__checkbox" data={"assetId" : "${self.contentId}", "rendition" : "${rendition.name}"}/>>
            <label for="checkbox-${rendition.name}-${self.contentId}"><span class="label-text">${cm.getMessage("am_rendition_${rendition.name}")}</span></label>
          </div>
        </#list>
      </div>
    </div>
  </div>
  <div class="am-overlay__submit-button" <@cm.dataAttribute name="data-am-overlay__update-button" data={"assetId" : "${self.contentId}"}/>>
    <span class="am-overlay__add-to-collection">${cm.getMessage("am_overlay_add_to_download_collection")}</span>
    <span class="am-overlay__update-collection">${cm.getMessage("am_overlay_update_download_collection")}</span>
  </div>
</div>
