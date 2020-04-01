<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPicture" -->

<#assign allAspectRatios=bp.setting(self, "responsiveImageSettings") />
<#list allAspectRatios?keys as ratio>
  <div class="cm-preview-item__image-box">
    <img class="cm-preview-item__image" src="${bp.getBiggestImageLink(self, ratio)}" alt="${cm.getMessage("preview_image_" + ratio)}" title="${cm.getMessage("preview_image_" + ratio)}"<@preview.metadata "properties.data.${ratio}" />>
    <span>${cm.getMessage("preview_image_" + ratio)}</span>
  </div>
</#list>
