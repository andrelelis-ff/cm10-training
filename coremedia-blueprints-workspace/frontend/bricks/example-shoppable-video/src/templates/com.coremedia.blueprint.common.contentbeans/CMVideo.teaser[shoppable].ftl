<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMVideo" -->

<#assign additionalClass=cm.localParameters().additionalClass!"" />
<#assign blockClass=cm.localParameters().blockClass!"cm-teasable" />
<#assign renderTeaserText=cm.localParameter("renderTeaserText", false) />
<#assign timelineEntries=self.timeLineSequences![] />
<#assign overlay={
  "displayTitle": true,
  "displayShortText": true,
  "displayPicture": true,
  "displayDefaultPrice": true,
  "displayDiscountedPrice": true,
  "displayOutOfStockLink": true
} />

<#if (timelineEntries?size > 0 || self.timeLineDefaultTarget?has_content)>
  <#-- including the shoppable video html -->
  <@cm.include self=self view="_shoppable" params={
  "additionalClass": additionalClass,
  "blockClass": blockClass,
  "renderTeaserText": renderTeaserText,
  "timelineEntries": timelineEntries,
  "overlay": overlay
  }/>
<#else>
  <#-- open default teaser without shoppable extras -->
  <@cm.include self=self view="teaser[]" params={
  "blockClass": blockClass,
  "additionalClass": additionalClass,
  "renderTeaserText": false
  }/>
  <#if preview.isPreviewCae()>
    <p>This is a shoppable video without timeline entries.</p>
  </#if>
</#if>
