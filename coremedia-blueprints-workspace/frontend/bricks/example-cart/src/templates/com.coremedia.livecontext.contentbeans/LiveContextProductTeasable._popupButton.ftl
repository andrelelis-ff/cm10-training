<#-- @ftlvariable name="self" type="com.coremedia.livecontext.contentbeans.LiveContextProductTeasable" -->
<#-- @ftlvariable name="_CSRFToken" type="java.lang.String" -->

<#import "../../freemarkerLibs/cart.ftl" as cart />

<#assign overlay={
  "displayTitle": false,
  "displayShortText": false,
  "displayPicture": false,
  "displayDefaultPrice": false,
  "displayDiscountedPrice": false,
  "displayOutOfStockLink": false
} + cm.localParameters().overlay!{} />

<#-- add-to-cart button -->
<div class="cm-popup__button cm-button-group">
  <@cart.addToCartButton product=self.product!cm.UNDEFINED
                         token=_CSRFToken!""
                         withLink=cm.getLink(self.productInSite!(cm.UNDEFINED))
                         enableShopNow=self.isShopNowEnabled(cmpage.context)
                         attr={"classes": ["cm-button-group__button", "cm-button--popup"]} />
</div>
