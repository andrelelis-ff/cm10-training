<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.contentbeans.AMTaxonomy" -->
<#-- @ftlvariable name="assetCount" type="java.lang.Long" -->

<#assign linkData={
  "requestParams": {
    "category": self.contentId
  }
} />

<div class="am-category-teaser am-category-teaser--dimmer" data-am-category-id="${self.contentId}"<@preview.metadata data=self.content />>
  <a <@cm.dataAttribute name="data-hash-based-fragment-link" data=linkData />>
    <#-- picture -->
    <@cm.include self=self view="asPictureBox" params={"classBox": "am-category-teaser__picture-box", "classImage": "am-category-teaser__picture", "scalePicture": true}  />
    <#-- caption -->
    <h4 class="am-category-teaser__caption am-heading-3">${self.value!""}</h4>
  </a>
</div>
