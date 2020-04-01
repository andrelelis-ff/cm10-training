<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.contentbeans.AMAssetRendition" -->

<#if self.blob?has_content>
<a class="am-text-link-with-icon am-download-collection-rendition-control__download" href="${cm.getLink(self)}">
      <span class="am-text-link-with-icon__icon am-icon am-icon--download"></span>
    <span class="am-text-link-with-icon__text">${cm.getMessage("am_download")}</span>
  </a>
</#if>
