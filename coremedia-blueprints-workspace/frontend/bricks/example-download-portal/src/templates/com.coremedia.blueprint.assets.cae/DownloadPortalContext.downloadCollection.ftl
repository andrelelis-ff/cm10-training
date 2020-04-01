<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.cae.DownloadPortalContext" -->

<#assign linkData={
  "requestParams": {
    "download-collection": "" <#-- TODO: maybe we could use cm.UNDEFINED and attach a serializer for it. -->
  }
} />
<div class="am-download-portal__download-collection am-download-collection">
  <a <@cm.dataAttribute name="data-hash-based-fragment-link" data=linkData />>
    <span class="am-download-collection__button am-button am-button--parent-hover am-text-scalable">
      ${cm.getMessage("am_download_collection_overview_button")}
    </span>
    <span class="am-download-collection__counter am-button am-button--circle am-button--parent-hover am-text-scalable" data-am-download-collection-counter="">0</span>
  </a>
</div>
