<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#--
    Template Description:

    Displays content as an embeded element in richtext with title, image and text. This templated is called by a
    programmed view (LinkEmbedFilter). Please check example brick "richtext-embedded" for a more detailed version.
-->

<div class="cm-teasable cm-teasable--embedded">
  <h2 class="cm-teasable_title">${self.teaserTitle!""}</h2>
  <div class="cm-teasable__picture">
    <@cm.include self=self.picture!cm.UNDEFINED />
  </div>
  <div class="cm-teasable__text">
    <@cm.include self=self.teaserText!cm.UNDEFINED />
  </div>
</div>
