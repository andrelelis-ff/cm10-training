<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPicture" -->

<#assign fragmentedPreviews=[]/>

<#if self.disableCropping>
  <#assign fragmentedPreviews=[{
    "viewName": "asPreviewOriginal",
    "titleKey": "preview_image_original"
  }] />
<#else>
  <#--<#assign fragmentedPreviews=bp.previewTypes(cmpage, self, [])/>-->
  <#assign allAspectRatios=bp.setting(self, "responsiveImageSettings") />
  <#list allAspectRatios?keys as ratio>
    <#assign fragmentedPreviews=fragmentedPreviews + [{
      "viewName": "asPreviewCropped",
      "viewParams": {
        "crop": ratio
      },
      "titleKey": "preview_image_" + ratio
    }] />
  </#list>
</#if>

<#--  -->

<@cm.include self=self view="multiViewPreview" params={
"fragmentViews": fragmentedPreviews
}/>
