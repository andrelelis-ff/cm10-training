<#-- @ftlvariable name="self" type="com.coremedia.livecontext.contentbeans.LiveContextProductTeasable" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />
<#import "*/node_modules/@coremedia/brick-default-teaser/src/freemarkerLibs/defaultTeaser.ftl" as defaultTeaser />

<#--
  Template Description:

  This templates overwrites and extends LiveContextProductTeasable._shopNow.ftl of @coremedia/brick-default-teaser.
  Instead of rendering a shop now button with a link to the pdp, the shop now button will trigger magnific popup
  to render the popup view of this product.

  @since 1907
-->

<#assign teaserBlockClass=cm.localParameters().teaserBlockClass!"" />
<#assign popupId=bp.generateId("cm-popup-") />

<#if self.isShopNowEnabled(cmpage.context)>
  <#-- button -->
  <div class="${teaserBlockClass}__shop-now">
    <@components.button text=cm.getMessage("popup_button_shop_now") href="${cm.getLink(self.productInSite!cm.UNDEFINED)}" attr={
      "classes": ["${teaserBlockClass}__shop-now-button", "cm-button--popup-loading"],
      "data-mfp-src": "#${popupId}"
    } />
  </div>

  <#-- popup -->
  <@cm.include self=self view="asPopup" params={
      "popupId" : "${popupId}",
      "additionalClass": "mfp-hide",
      "overlay": {
        "displayTitle": true,
        "displayShortText": true,
        "displayPicture": true,
        "displayDefaultPrice": true,
        "displayDiscountedPrice": true,
        "displayOutOfStockLink": true
      }
  } />
</#if>
