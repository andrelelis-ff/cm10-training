<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMObject" -->
<#-- @ftlvariable name="classBox" type="java.lang.String" -->
<#-- @ftlvariable name="classMedia" type="java.lang.String" -->
<#-- @ftlvariable name="metadata" type="java.util.List" -->
<#-- @ftlvariable name="metadataMedia" type="java.util.List" -->

<#-- This is just a fallback template if only the media brick is used, but not the brick for the desired media type -->

<div class="${cm.localParameters().classBox!""}"<@preview.metadata data=cm.localParameters().metadata![] + [self.content]/>>
  <div class="${cm.localParameters().classMedia!""}"<@preview.metadata data=cm.localParameters().metadataMedia![]/>>
    <#if preview.isPreviewCae()>
      This is just a fallback template. Please add the bricks for your desired media types to your theme.
    </#if>
  </div>
</div>

