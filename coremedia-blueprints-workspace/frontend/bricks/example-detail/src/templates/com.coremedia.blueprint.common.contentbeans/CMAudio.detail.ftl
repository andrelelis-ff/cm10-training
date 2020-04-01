<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMAudio" -->

<#--
    Template Description:

    This template provides a detail view for the CMAudio content type
-->

<#assign blockClass=cm.localParameters().blockClass!"cm-details" />
<#assign renderTags=cm.localParameter("renderTags", true) />

<article class="${blockClass} ${blockClass}--audio"<@preview.metadata self.content/>>

  <#-- title -->
  <h1 class="${blockClass}__headline"<@preview.metadata "properties.title"/>>${self.title!""}</h1>

  <#-- media -->
  <@cm.include self=self view="_detailMedia" params=cm.localParameters() />

  <#-- audio -->
  <div class="${blockClass}__medias">
    <@cm.include self=self view="media" params={
      "classBox": "${blockClass}__audio-box",
      "classMedia": "${blockClass}__audio",
      "preload": true
    }/>
    <span class="${blockClass}__copyright"<@preview.metadata "properties.copyright"/>>${self.copyright!""}</span>
  </div>

  <#-- text -->
  <#if self.detailText?has_content>
    <div class="${blockClass}__text cm-richtext"<@preview.metadata "properties.detailText"/>>
      <@cm.include self=self.detailText!cm.UNDEFINED />
    </div>
  </#if>

  <#-- tags -->
  <#if renderTags>
    <@cm.include self=self view="_tagList"/>
  </#if>
</article>

<#-- extensions -->
<@cm.hook id=bp.viewHookEventNames.VIEW_HOOK_END />
