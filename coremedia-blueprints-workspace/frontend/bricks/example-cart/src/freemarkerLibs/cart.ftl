<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#--
  Renders an addToCart button in different variations depending on the given product. If product has only
  one product variant the button includes an add to cart functionality, otherwise its just a link to the product
  detail page. If no product is available, it renders an unavailable labeled button.

  @param product The product that needs to be added to the cart.
  @param alwaysShow Forces the button to be shown even if product is not available.
  @param alwaysClickable Enables add to cart functionality even if there is more than one variant.
  @param enableShopNow Enable addToCart functionality. If false, only show the Details button.
  @param withLink Link to the product detail page. Will be used for the button, if enableShopNow is
                  disabled or product has more than one variant.
  @param attr Adds additional custom attributes as a hash map to the button.

  Example:
  <@addToCartButton product=self.product
                    withLink=cm.getLink(self.productInSite)
                    attr={"classes":["my-button-group__button", "my-button--linked"]} />
-->
<#macro addToCartButton product token="" alwaysShow=false alwaysClickable=false enableShopNow=true withLink="" attr={}>
  <#local numberOfVariants=(product.variants?size)!0 />
  <#local hasSingleSKU=(numberOfVariants == 1) />
  <#local isProductAvailable=(product.isAvailable())!false />

  <#-- variant 1) unavailable -->
  <#local buttonLabel=cm.getMessage("cart_unavailable") />
  <#local buttonData={} />
  <#local buttonClasses=[] />
  <#local iconClass="" />

  <#-- variant 2) available -->
  <#if (alwaysShow || isProductAvailable)>
    <#local buttonLabel=cm.getMessage("cart_view_variants") />
    <#local buttonClasses=buttonClasses + ["cm-button--primary"] />

  <#-- variant 3) available with one sku -->
    <#if (enableShopNow && (alwaysClickable || hasSingleSKU))>
      <#local cart=cm.substitute("cart", product) />
      <#local buttonLabel=cm.getMessage("cart_add_item") />
      <#local externalTechId=hasSingleSKU?then(product.variants[0].externalTechId, product.externalTechId)/>
      <#local buttonData={"data-cm-cart-add-item": {"id": externalTechId!"", "link": cm.getLink(cart, "ajax"), "token": token }} />
      <#-- provide non empty string for button macro -->
      <#local iconClass=" " />
    </#if>
  </#if>

  <#local attr=utils.extendSequenceInMap(attr, "classes", buttonClasses) />

  <#local link="" />
  <#if (withLink?has_content && ((!alwaysClickable && !hasSingleSKU) || !enableShopNow))>
    <#local link=withLink>
  </#if>
  <@components.button text=buttonLabel href=link baseClass="cm-button" iconClass=iconClass attr=(attr + buttonData) />
</#macro>
