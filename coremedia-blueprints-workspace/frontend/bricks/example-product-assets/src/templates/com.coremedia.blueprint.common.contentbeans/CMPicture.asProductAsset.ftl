<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMVisual" -->

<#assign modifier=cm.localParameters().modifier!cm.UNDEFINED />
<#assign renderAsThumbnail=cm.localParameters().renderAsThumbnail!false />

<div class="cm-product-asset<#if modifier?has_content> cm-product-asset--${modifier}</#if>" data-cm-product-asset-gallery-item="">
  <@cm.include self=self
               view="media"
               params={
                 "classBox": "cm-product-asset__media-box",
                 "classMedia": "cm-product-asset__media"
               } />
</div>
