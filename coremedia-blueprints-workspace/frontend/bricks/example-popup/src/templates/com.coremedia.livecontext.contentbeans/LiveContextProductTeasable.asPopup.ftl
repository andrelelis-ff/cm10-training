<#-- @ftlvariable name="self" type="com.coremedia.livecontext.contentbeans.LiveContextProductTeasable" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#--
  Template Description:

  This template is the same as CMTeasable.asPopup.ftl, except for the eCommerce extensions like
  catalogPicture and price.

  @since 1907
-->

<#-- if overlay configuration is not set explicitly assert false for each key not set -->
<#assign popupId=cm.localParameters().popupId?has_content?then('id="' + cm.localParameters().popupId + '"', "") />
<#assign additionalClass=cm.localParameters().additionalClass!"" />
<#assign overlay={
  "displayTitle": false,
  "displayShortText": false,
  "displayPicture": false,
  "displayDefaultPrice": false,
  "displayDiscountedPrice": false,
  "displayOutOfStockLink": false
} + cm.localParameters().overlay!{} />
<#assign showTitle=self.teaserTitle?has_content && overlay.displayTitle />
<#assign showTeaserText=self.teaserText?has_content && overlay.displayShortText />

<div ${popupId?no_esc} class="cm-popup ${additionalClass}" <@preview.metadata self.content />>
  <#-- image -->
  <#if overlay.displayPicture>
    <div class="cm-popup__container">
      <#if (self.product?has_content && self.product.isAvailable()) || overlay.displayOutOfStockLink>
        <a href="${cm.getLink(self.productInSite!(cm.UNDEFINED))}" class="cm-popup__picture-link">
      </#if>
        <#assign pictureParams={
          "classBox": "cm-popup__picture-box",
          "classMedia": "cm-popup__picture",
          "metadata": ["properties.pictures"]
        } />
        <#if self.picture?has_content>
          <@cm.include self=self.picture!cm.UNDEFINED view="media" params=pictureParams />
        <#else>
          <@cm.include self=(self.product.catalogPicture)!cm.UNDEFINED view="media" params=pictureParams />
        </#if>
      <#if (self.product?has_content && self.product.isAvailable()) || overlay.displayOutOfStockLink>
        </a>
      </#if>
    </div>
  </#if>
  <div class="cm-popup__container">
    <div class="cm-popup__content">
      <#-- teaserTitle -->
      <div class="cm-popup__header">
        <#if showTitle>
          <h5 class="cm-popup__title"<@preview.metadata "properties.teaserTitle" />>${self.teaserTitle}</h5>
        </#if>
      </div>
      <#-- price -->
      <#if overlay.displayDefaultPrice || overlay.displayDiscountedPrice>
        <div class="cm-popup__price">
          <@cm.include self=self.product!cm.UNDEFINED view="pricing" params={
            "showListPrice": overlay.displayDefaultPrice,
            "showOfferPrice": overlay.displayDiscountedPrice,
            "classListPrice": "cm-popup__listprice", "classOfferPrice": "cm-popup__offerprice"
          } />
        </div>
      </#if>
      <#-- teaserText -->
      <div class="cm-popup__text"<@preview.metadata "properties.teaserText" />>
        <#if showTeaserText>
          <#-- strip wrong <p/> tags from ecommerce, happens in hybris -->
          <@utils.renderWithLineBreaks text=bp.truncateText(self.teaserText!"", 175)?replace("&lt;p&gt;", "")?replace("&lt;/p&gt;", "") />
        </#if>
      </div>
      <#-- call to action button -->
      <#if (self.product?has_content && self.product.isAvailable()) || overlay.displayOutOfStockLink>
        <@cm.include self=self view="_popupButton" />
      </#if>
    </div>
  </div>
</div>
