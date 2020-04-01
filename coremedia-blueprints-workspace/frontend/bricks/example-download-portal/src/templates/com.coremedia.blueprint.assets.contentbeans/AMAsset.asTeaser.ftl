<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.contentbeans.AMAsset" -->
<#-- @ftlvariable name="baseRequestParams" type="java.util.Map" -->

<#assign baseRequestParams=cm.localParameters().baseRequestParams!{} />
<#assign requestParams={
  "asset": self.contentId
} />
<#-- only accept category from base request params -->
<#if baseRequestParams.category?has_content>
  <#assign requestParams={"category" : baseRequestParams.category} + requestParams />
</#if>
<#assign linkData={
  "requestParams": requestParams
} />

<div class="am-asset-teaser am-asset-teaser--dimmer am-asset-teaser--overlay" data-am-asset-id="${self.contentId}"<@preview.metadata data=self.content />>
  <div class="am-asset-teaser__wrapper">
    <#-- picture with link -->
    <a <@cm.dataAttribute name="data-hash-based-fragment-link" data=linkData />>
      <@cm.include self=self view="asPictureBox" params={"classBox": "am-asset-teaser__picture-box", "classImage": "am-asset-teaser__picture", "scalePicture": true, "showQuickSelectButton": true} />
    </a>
    <#-- overlay without link -->
    <@cm.include self=self view="_overlay"/>
  </div>
  <#-- caption with link -->
  <a <@cm.dataAttribute name="data-hash-based-fragment-link" data=linkData />>
    <h3 class="am-asset-teaser__caption am-heading-3"<@preview.metadata data="properties.name" />>${self.title!""}</h3>
  </a>
</div>
