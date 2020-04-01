<#-- @ftlvariable name="self" type="com.coremedia.blueprint.ecommerce.contentbeans.CMProduct" -->

<#--
    Template Description:

    This template provides a detail view for the CMProduct content type
-->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#assign blockClass=cm.localParameters().blockClass!"cm-details" />
<#assign relatedView=cm.localParameters().relatedView!"asRelated" />

<#assign renderDate=cm.localParameter("renderDate", true) />
<#assign renderTags=cm.localParameter("renderTags", true) />
<#assign renderRelated=cm.localParameter("renderRelated", true) />
<#assign additionalSlickConfig={
  "controlIcon": "triangle",
  "displayPagination": true}
/>

<article class="${blockClass} ${blockClass}--product"<@preview.metadata self.content />>

  <#-- title -->
  <h1 class="${blockClass}__headline"<@preview.metadata "properties.productName"/>>${self.productName!""}</h1>

  <#-- media -->
  <@cm.include self=self view="_detailMedia" params=cm.localParameters() + { "slickConfig": additionalSlickConfig } />

  <#-- product code -->
  <div class="${blockClass}__code"<@preview.metadata "properties.productCode"/>>
      <span>${cm.getMessage("product_code")}</span> ${self.productCode!""}
  </div>

  <#-- text -->
  <#if self.detailText?has_content>
    <div class="${blockClass}__text cm-richtext"<@preview.metadata "properties.detailText"/>>
      <@cm.include self=self.detailText!cm.UNDEFINED />
    </div>
  </#if>

  <#-- downloads -->
  <#if self.downloads?has_content>
    <div class="${blockClass}__downloads cm-downloads"<@preview.metadata "properties.downloads"/>>
      <h3>${cm.getMessage("download_label")}</h3>
      <ul class="cm-downloads__items">
        <#list self.downloads![] as download>
          <li class="cm-downloads__item">
            <a href="${cm.getLink(download.target!cm.UNDEFINED)}" <@preview.metadata data=[download.content, "properties.teaserTitle"] />>${download.teaserTitle!""}</a>
          </li>
        </#list>
      </ul>
    </div>
  </#if>

  <#-- date -->
  <#if renderDate && self.externallyDisplayedDate?has_content>
    <div class="${blockClass}__date"<@preview.metadata "properties.externallyDisplayedDate"/>>
      <@utils.renderDate date=self.externallyDisplayedDate.time cssClass="${blockClass}__time" />
    </div>
  </#if>

  <#-- tags -->
  <#if renderTags>
    <@cm.include self=self view="_tagList"/>
  </#if>
</article>

<#-- extensions -->
<@cm.hook id=bp.viewHookEventNames.VIEW_HOOK_END />

<#-- related -->
<#if renderRelated>
  <@cm.include self=self view="_related" params={"relatedView": relatedView, "additionalClass": "${blockClass}__related"}/>
</#if>
