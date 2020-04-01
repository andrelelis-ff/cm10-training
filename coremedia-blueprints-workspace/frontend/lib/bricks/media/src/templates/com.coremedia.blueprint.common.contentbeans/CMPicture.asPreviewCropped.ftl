<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPicture" -->

<#assign crop=cm.localParameters().crop!"" />
<#assign imageLink=bp.getBiggestImageLink(self, crop)!"" />

<#if imageLink?has_content>
  <img class="cm-preview-item__image" src="${imageLink}" alt="${cm.getMessage("preview_image_" + crop)}"<@preview.metadata "properties.data.${crop}" />>
</#if>
