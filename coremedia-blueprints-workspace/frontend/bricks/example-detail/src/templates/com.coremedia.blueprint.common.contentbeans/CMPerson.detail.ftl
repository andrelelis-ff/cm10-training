<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPerson" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#--
    Template Description:

    This template provides a detail view for the CMPerson content type

-->

<#assign blockClass=cm.localParameters().blockClass!"cm-details" />
<#assign renderRelated=cm.localParameters().renderRelated!true />

<div class="${blockClass} ${blockClass}--person"<@preview.metadata self.content/>>
  <article class="${blockClass}__content">
    <#-- introduction with picture on the left and descritoion on the right-->
    <div class="${blockClass}__introduction">
      <#if self.picture?has_content>
        <div class="${blockClass}__picture-person">
          <@cm.include self=self.picture view="media" params={
            "classBox": "${blockClass}__picture-box",
            "classMedia": "${blockClass}__picture",
            "metadata": ["properties.picture"]
          }/>
        </div>
      </#if>
      <div class="${blockClass}__description">
        <#if self.displayName?has_content>
          <h1 class="${blockClass}__name"<@preview.metadata "properties.displayName"/>>${self.displayName}</h1>
        </#if>

        <#if self.jobTitle?has_content>
          <h2 class="${blockClass}__job"<@preview.metadata "properties.jobTitle"/>>${self.jobTitle}</h2>
        </#if>

        <#if self.organization?has_content>
          <p class="${blockClass}__organization"<@preview.metadata "properties.organization"/>>${self.organization}</p>
        </#if>

        <#if self.EMail?has_content>
          <p class="${blockClass}__email"<@preview.metadata "properties.EMail"/>>
            <@utils.optionalLink href="mailto:${self.EMail}">
              <span class="${blockClass}__email-icon"></span>
              <span class="${blockClass}__email-text">${self.EMail}</span>
            </@utils.optionalLink>
          </p>
        </#if>

        <#if self.furtherDetails?has_content>
          <@cm.include self=self view="_furtherDetails" />
        </#if>
      </div>
    </div>

    <#if self.detailText?has_content>
      <div class="${blockClass}__text cm-richtext"<@preview.metadata "properties.detailText"/>>
        <@cm.include self=self.detailText />
      </div>
    </#if>
  </article>

  <#-- related -->
  <#if renderRelated && self.related?has_content>
    <@cm.include self=self view="_related" params={"additionalClass": "${blockClass}__related"}/>
  </#if>

  <#-- extensions -->
  <@cm.hook id=bp.viewHookEventNames.VIEW_HOOK_END />
</div>

