<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMLinkable" -->

<#--
  Template Description:

  This template simply delegates, but is still necessary since fragmentHandler resolves to linkable.
-->

<#-- Breadcrumb above Pagegrid for IBM -->
<div class="cm-breadcrumb--outer">
  <@cm.include self=cmpage.navigation!cm.UNDEFINED view="asBreadcrumbFragment"/>
</div>
<@cm.include self=self view="detail" params={"relatedView": "asRelated", "renderTags": false}/>
