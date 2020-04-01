<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.asset.CatalogPicture" -->
<#-- @ftlvariable name="classBox" type="java.lang.String" -->
<#-- @ftlvariable name="classMedia" type="java.lang.String" -->

<#assign classBox=cm.localParameters().classBox!"" />
<#assign classMedia=cm.localParameters().classMedia!"" />

<#if self.picture?has_content>
  <@cm.include self=bp.createBeanFor(self.picture) view="media" params={
    "classBox": classBox,
    "classMedia": classMedia
  }/>
<#elseif self.url?has_content>
  <div class="${classBox}">
    <img class="${classMedia} cm-media cm-media--uncropped" src="${self.url!""}" alt="">
  </div>
</#if>
