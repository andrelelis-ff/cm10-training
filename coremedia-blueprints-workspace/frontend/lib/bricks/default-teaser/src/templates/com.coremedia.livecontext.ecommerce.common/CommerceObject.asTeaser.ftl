<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.common.CommerceObject" -->

<#-- fallback template, nothing to render -->
<#assign isLast=cm.localParameter("islast", false)/>
<@cm.include self=self view="teaser" params={
  "isLast": isLast,
  "renderTeaserText": false
}/>
