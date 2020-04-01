<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.contentbeans.AMTaxonomy" -->
<#-- @ftlvariable name="classBox" type="java.lang.String" -->
<#-- @ftlvariable name="classImage" type="java.lang.String" -->
<#-- @ftlvariable name="scalePicture" type="java.lang.Boolean" -->

<#assign classBox=cm.localParameters().classBox!"" />
<#assign classImage=cm.localParameters().classImage!"" />
<#assign scalePicture=cm.localParameters().scalePicture!false />

<#if self.assetThumbnail?has_content && self.assetThumbnail.thumbnail?has_content>
  <@cm.include self=self.assetThumbnail view="asPictureBox" params={
    "classBox": classBox,
    "classImage": classImage,
    "scalePicture": scalePicture,
    "showBadge": false,
    "metadata": ["properties.assetThumbnail", self.assetThumbnail.content]
  } />
<#else>
  <div class="am-picture-box am-picture-box--empty ${classBox}"<@preview.metadata data=["properties.assetThumbnail"]/>>
    <div class="am-picture-box__picture ${classImage}"></div>
  </div>
</#if>
