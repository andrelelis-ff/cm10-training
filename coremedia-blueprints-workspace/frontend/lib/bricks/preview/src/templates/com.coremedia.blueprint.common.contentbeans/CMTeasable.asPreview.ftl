<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#assign fragmentViews=[
  {
    "viewName": "",
    "titleKey": "preview_label_DEFAULT"
  }] />

<#assign fragmentedPreviews=bp.previewTypes(cmpage, self, fragmentViews)/>

<@cm.include self=self view="multiViewPreview" params={
  "fragmentViews": fragmentedPreviews
}/>
