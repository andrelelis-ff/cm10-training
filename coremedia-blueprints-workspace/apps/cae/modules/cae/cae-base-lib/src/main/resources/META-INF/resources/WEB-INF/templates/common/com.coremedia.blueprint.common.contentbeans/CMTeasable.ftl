<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#--
    Template Description:

    Displays content in a full default detail variant with title, image and text. Please check example brick "details"
    for a more detailed version.
-->

<div class="cm-teasable">
  <h1 class="cm-teasable__title">${self.title!""}</h1>
  <div class="cm-teasable__picture">
    <@cm.include self=self.picture!cm.UNDEFINED />
  </div>
  <div class="cm-teasable__text">
    <@cm.include self=self.detailText!cm.UNDEFINED />
  </div>
</div>
