<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMVideo" -->

<#--
    Template Description:

    This template provides a detail view for the CMVideo content type
-->

<#assign blockClass=cm.localParameters().blockClass!"cm-details" />
<#assign relatedView=cm.localParameters().relatedView!"related" />
<#assign renderTags=cm.localParameter("renderTags", true) />
<#assign renderRelated=cm.localParameter("renderRelated", true) />
<#assign hideControls = self.playerSettings.hideControls />
<#assign muted = self.playerSettings.muted />
<#assign autoplay = self.playerSettings.autoplay />
<#assign loop = self.playerSettings.loop />

<article class="${blockClass} ${blockClass}--video"<@preview.metadata self.content />>

  <#-- title -->
  <h1 class="${blockClass}__headline"<@preview.metadata "properties.title"/>>${self.title!""}</h1>

  <#-- video -->
  <div class="${blockClass}__medias">
    <@cm.include self=self view="media" params={
      "classBox": "${blockClass}__media-box",
      "classMedia": "${blockClass}__media",
      "loop": loop,
      "autoplay": autoplay,
      "muted": muted,
      "hideControls": hideControls,
      "preload": true
    } />
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

<#-- related -->
<#if renderRelated>
  <@cm.include self=self view="_related" params={"relatedView": relatedView, "additionalClass": "${blockClass}__related"}/>
</#if>

<#-- extensions -->
<@cm.hook id=bp.viewHookEventNames.VIEW_HOOK_END />
