<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMLinkable" -->

<#--
    Template Description:

    Displays title and link for a linkable object.
-->

<div class="cm-linkable">
  <h1 class="cm-linkable__title">${self.title!""}</h1>
  <a class="cm-linkable__link" href="${cm.getLink(self)}">${cm.getLink(self)}</a>
</div>
