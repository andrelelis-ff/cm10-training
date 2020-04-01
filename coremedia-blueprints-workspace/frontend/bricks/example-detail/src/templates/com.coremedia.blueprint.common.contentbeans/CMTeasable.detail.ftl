<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#--
    Template Description:

    This template provides a detail view for the CMTeasable content type
-->

<#assign blockClass=cm.localParameters().blockClass!"cm-details" />
<#assign relatedView=cm.localParameters().relatedView!"asRelated" />
<#assign renderDate=cm.localParameter("renderDate", true) />
<#assign renderTags=cm.localParameter("renderTags", true) />
<#assign renderAuthors=cm.localParameter("renderAuthors", true) />
<#assign renderRelated=cm.localParameter("renderRelated", true) />

<div class="${blockClass}" <@preview.metadata self.content />>
  <article class="${blockClass}__content">
    <#-- title -->
    <h1 class="${blockClass}__headline"<@preview.metadata "properties.title"/>>${self.title!""}</h1>

    <#-- media -->
    <@cm.include self=self view="_detailMedia" params=cm.localParameters() />

    <#-- text -->
    <#if self.detailText?has_content>
      <div class="${blockClass}__text cm-richtext"<@preview.metadata "properties.detailText"/>>
        <@cm.include self=self.detailText!cm.UNDEFINED />
      </div>
    </#if>

    <#-- date -->
    <#if renderDate && self.externallyDisplayedDate?has_content>
      <div class="${blockClass}__date">
        <@utils.renderDate date=self.externallyDisplayedDate.time cssClass="${blockClass}__time" />
      </div>
    </#if>
  </article>

  <#-- authors -->
  <#if renderAuthors && self.authors?has_content>
    <@cm.include self=self view="_authors" params={"parentClass": blockClass} />
  </#if>

  <#-- tags -->
  <#if renderTags && self.subjectTaxonomy?has_content>
    <@cm.include self=self view="_tagList" params={"parentClass": blockClass} />
  </#if>

  <#-- related -->
  <#if renderRelated && self.related?has_content>
    <@cm.include self=self view="_related" params={"relatedView": relatedView, "additionalClass": "${blockClass}__related"}/>
  </#if>

  <#-- extensions -->
  <@cm.hook id=bp.viewHookEventNames.VIEW_HOOK_END />
</div>
