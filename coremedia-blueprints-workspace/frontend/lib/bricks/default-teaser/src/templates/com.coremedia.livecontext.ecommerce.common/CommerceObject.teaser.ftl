<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.common.CommerceObject" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#assign blockClass=cm.localParameters().blockClass!"cm-teasable" />
<#assign additionalClass=cm.localParameters().additionalClass!"" />
<#assign renderWrapper=cm.localParameters().renderWrapper!true />

<div class="${blockClass} ${additionalClass}">
  <@utils.optionalTag condition=renderWrapper attr={"class": "${blockClass}__wrapper"}>
    <@cm.include self=self view="teaserMedia" params={
      "teaserBlockClass": blockClass
    } + cm.localParameters() />

    <@cm.include self=self view="teaserCaption" params={"teaserBlockClass": blockClass} + cm.localParameters() />
  </@utils.optionalTag>

  <#-- extensions -->
  <@cm.hook id=bp.viewHookEventNames.VIEW_HOOK_TEASER />
</div>
