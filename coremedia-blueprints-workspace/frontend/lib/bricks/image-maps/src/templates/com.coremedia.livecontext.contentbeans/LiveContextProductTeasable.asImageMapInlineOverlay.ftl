<#-- @ftlvariable name="self" type="com.coremedia.livecontext.contentbeans.LiveContextProductTeasable" -->
<#-- @ftlvariable name="overlay" type="java.util.Map" -->
<#-- @ftlvariable name="classOverlay" type="java.lang.String" -->
<#-- @ftlvariable name="metadata" type="java.util.List" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#-- if overlay configuration is not set explicitly assert false for each key not set -->
<#assign overlay={
  "displayTitle": false,
  "displayDefaultPrice": false,
  "displayDiscountedPrice": false
} + overlay!{} />

<div class="cm-overlay ${classOverlay}"<@preview.metadata data=(metadata![]) + [self.content] />>
  <@utils.optionalLink href=cm.getLink(self.productInSite!(cm.UNDEFINED)) attr={"class":"cm-overlay__link"}>
    <#if overlay.displayTitle && self.teaserTitle?has_content>
      <div class="cm-overlay__item cm-overlay__item--title"<@preview.metadata "properties.teaserTitle" />>${self.teaserTitle}</div>
    </#if>
    <div class="cm-overlay__item"<@preview.metadata "properties.externalId" />>
      <@cm.include self=self.product!cm.UNDEFINED view="pricing" params={
        "showListPrice": overlay.displayDefaultPrice,
        "showOfferPrice": overlay.displayDiscountedPrice,
        "classListPrice": "cm-price--overlay",
        "classOfferPrice": "cm-price--overlay"
      } />
    </div>
  </@utils.optionalLink>
</div>
