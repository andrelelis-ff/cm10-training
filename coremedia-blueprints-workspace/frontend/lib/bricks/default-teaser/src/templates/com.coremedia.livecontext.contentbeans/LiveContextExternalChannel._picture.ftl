<#-- @ftlvariable name="self" type="com.coremedia.livecontext.contentbeans.LiveContextExternalChannel" -->

<#assign blockClass=cm.localParameters().blockClass!"cm-teasable" />
<#assign renderEmptyImage=cm.localParameter("renderEmptyImage", true) />
<#assign pictureParams={
  "classBox": "${blockClass}__picture-box",
  "classMedia": "${blockClass}__picture",
  <#--player settings for video and audio -->
  "hideControls": true,
  "autoplay": true,
  "loop": true,
  "muted": true,
  "preload": true
} />
<#if self.firstMedia?has_content>
  <@cm.include self=self.firstMedia view="media" params=pictureParams + {"metadata": ["properties.pictures"]}/>
<#elseif self.category?has_content && self.category.catalogPicture?has_content>
  <@cm.include self=(self.category.catalogPicture)!cm.UNDEFINED view="media" params=pictureParams />
<#elseif renderEmptyImage>
<div class="${blockClass}__picture-box" <@preview.metadata "properties.pictures" />>
    <div class="${blockClass}__picture"></div>
</div>
</#if>
