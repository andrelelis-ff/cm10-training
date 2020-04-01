<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.contentbeans.AMTaxonomy" -->
<#-- @ftlvariable name="class" type="java.lang.String" -->

<#assign class=cm.localParameters().class!"" />
<#assign linkData={
  "requestParams": {
    "category": self.contentId
  }
} />
<a class="${class}" <@cm.dataAttribute name="data-hash-based-fragment-link" data=linkData /><@preview.metadata data=[self.content, "properties.value"] />>${self.value!""}</a>
