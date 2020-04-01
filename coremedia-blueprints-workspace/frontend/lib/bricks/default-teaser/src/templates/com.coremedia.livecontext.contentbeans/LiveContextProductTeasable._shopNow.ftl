<#-- @ftlvariable name="self" type="com.coremedia.livecontext.contentbeans.LiveContextProductTeasable" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />

<#--
  Template Description:

  This template renders an optional call-to-action button with a link to the product detail page, if "shop now" is enabled.

  @since 1907
-->

<#assign teaserBlockClass=cm.localParameters().teaserBlockClass!"" />

<#if self.isShopNowEnabled(cmpage.context)>
  <div class="${teaserBlockClass}__shop-now">
    <@components.button text=cm.getMessage("button_shop_now")  href="${cm.getLink(self.productInSite!cm.UNDEFINED)}" attr={
      "classes": ["${teaserBlockClass}__shop-now-button"]
    } />
  </div>
</#if>
