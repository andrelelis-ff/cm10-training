<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMLinkable" -->

<#--
  Template Description:

  This template simply delegates, but is still necessary since fragmentHandler resolves to linkable.
-->

<@cm.include self=self view="detail" params={"relatedView": "asRelated", "renderTags": false}/>
