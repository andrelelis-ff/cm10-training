<#-- @ftlvariable name="self" type="com.coremedia.blueprint.personalization.contentbeans.CMP13NSearch" -->

<#assign fragmentViews=[
  {
    "viewName": "",
    "titleKey": "preview_label_DEFAULT"
  }
]/>

<#assign
  fragmentedPreviews=bp.previewTypes(cmpage, self, fragmentViews)
  additionalAttr={}
  <#-- searchStatusAsJSON is only assigned if getItems() has been called -->
  items=self.getItems()
/>
<#if self.searchStatusAsJSON?has_content>
  <#assign additionalAttr = additionalAttr + {"data-cm-personalization-editorplugin-searchstatus": self.searchStatusAsJSON} />
</#if>

<@cm.include self=self view="multiViewPreview" params={
"fragmentViews": fragmentedPreviews,
"additionalAttr": additionalAttr
}/>

