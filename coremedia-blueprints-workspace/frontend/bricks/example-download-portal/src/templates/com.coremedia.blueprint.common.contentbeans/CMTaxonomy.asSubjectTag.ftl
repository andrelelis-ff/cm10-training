<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTaxonomy" -->

<#assign linkData={
    "requestParams": {
    "subject": self.contentId
  }
} />
<a class="am-subject-tag" data-am-subject-tag-id="${self.contentId}"<@cm.dataAttribute name="data-hash-based-fragment-link" data=linkData/>>${self.value}</a>