<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#assign blockClass=cm.localParameters().blockClass!"cm-teasable" />
<#assign additionalClass=cm.localParameters().additionalClass!"" />
<#assign renderWrapper=cm.localParameters().renderWrapper!true />
<#assign enableTeaserOverlay=cm.localParameters().enableTeaserOverlay!true /> <#-- de-/activate teaser overlay generally-->
<#assign renderCTA=cm.localParameters().renderCTA!true />

<div class="${blockClass} ${additionalClass}"<@preview.metadata self.content />>
  <@utils.optionalTag condition=renderWrapper attr={"class": "${blockClass}__wrapper"}>
    <@cm.include self=self view="teaserMedia" params={
      "teaserBlockClass": blockClass
    } + cm.localParameters() />

    <#if (enableTeaserOverlay && self.teaserOverlaySettings.enabled)>
      <@cm.include self=self view="teaserOverlay" params={"renderCTA": renderCTA } />
    <#else>
      <@cm.include self=self view="teaserCaption" params={"teaserBlockClass": blockClass} + cm.localParameters() />
    </#if>

  </@utils.optionalTag>

  <#-- extensions -->
  <@cm.hook id=bp.viewHookEventNames.VIEW_HOOK_TEASER />
</div>
