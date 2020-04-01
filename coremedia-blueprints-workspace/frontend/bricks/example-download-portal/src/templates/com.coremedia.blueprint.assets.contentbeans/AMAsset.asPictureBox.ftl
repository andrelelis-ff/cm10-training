<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.contentbeans.AMAsset" -->
<#-- @ftlvariable name="classBox" type="java.lang.String" -->
<#-- @ftlvariable name="classImage" type="java.lang.String" -->
<#-- @ftlvariable name="showBadge" type="java.lang.Boolean" -->
<#-- @ftlvariable name="showQuickSelectButton" type="java.lang.Boolean" -->
<#-- @ftlvariable name="scalePicture" type="java.lang.Boolean" -->
<#-- @ftlvariable name="metadata" type="java.util.List" -->

<#assign classBox=cm.localParameters().classBox!"" />
<#assign classImage=cm.localParameters().classImage!"" />
<#assign showBadge=cm.localParameters().showBadge!true />
<#assign showQuickSelectButton=cm.localParameters().showQuickSelectButton!false />
<#assign scalePicture=cm.localParameters().scalePicture!false />
<#assign emptyModifierClass=self.thumbnail?has_content?string("", "am-picture-box--empty") />
<#assign scaleModifierClass=scalePicture?string("am-picture-box--scale", "") />
<#assign metadata=cm.localParameters().metadata![] />

<div class="am-picture-box ${emptyModifierClass} ${scaleModifierClass} ${classBox}"<@preview.metadata data=metadata + ["properties.thumbnail"] />>
  <#if self.thumbnail?has_content>
    <#assign imageSrc=cm.getLink(self.thumbnail)!"" />
    <img src="${imageSrc}" alt="${(self.title)!""}" class="am-picture-box__picture ${classImage}">
  <#else>
    <div class="am-picture-box__picture ${classImage}"></div>
  </#if>
  <#if showBadge>
    <#-- document asset icon is default -->
    <#assign assetTypeName=(self.content.type.name)!"AMDocumentAsset" />
    <#assign badgeIconModifier="am-icon--" + assetTypeName />
    <div class="am-picture-box__badge">
      <#if showQuickSelectButton>
        <div class="am-picture-box__badge-icon-left am-icon am-icon--picture-overlay" <@cm.dataAttribute name="data-am-picture-box__badge-icon-left" data={"assetId" : "${self.contentId}"}/>></div>
      </#if>
      <div class="am-picture-box__badge-icon-right am-icon ${badgeIconModifier}"></div>
    </div>
  </#if>
</div>
