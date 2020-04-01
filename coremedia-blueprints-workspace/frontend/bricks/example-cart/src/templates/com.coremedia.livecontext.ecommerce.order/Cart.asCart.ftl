<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.order.Cart" -->
<#-- @ftlvariable name="_CSRFToken" type="java.lang.String" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />

<#--
  Template Description:

  Renders a cart.

  If it contains items the items will be rendered with their product's thumbnail image, name and description.
  In addition to this the quantity and price will be displayed. Otherwise a message will be printed that the cart is
  empty.

  In all cases a button will be rendered leading to the detail page of the cart.

  @since 1907
-->

<div class="cm-cart">
  <div class="cm-cart__arrow"></div>

  <#-- list all items in cart -->
  <#if self.orderItems?size gt 0>
    <div class="cm-cart__list">
      <#list self.orderItems![] as item>
          <#assign productInSite=lc.createProductInSite(item.product)/>
          <div class="cm-cart__item cm-cart-item">
            <#if item.product?has_content>
              <#if item.product.currency?has_content && item.product.locale?has_content>
                <#if item.unitPrice?has_content>
                  <#assign unitPriceFormatted=lc.formatPrice(item.unitPrice, item.product.currency, item.product.locale)/>
                </#if>
                <#if item.price?has_content>
                  <#assign totalPriceFormatted=lc.formatPrice(item.price, item.product.currency, item.product.locale)/>
                </#if>
              </#if>

              <#-- product image -->
              <#if item.product.defaultImageUrl?has_content>
                  <div class="cm-cart-item__image-box">
                      <a href="${cm.getLink(productInSite)}">
                          <img class="cm-cart-item__image cm-media cm-media--uncropped" src="${item.product.defaultImageUrl}" alt="${item.product.defaultImageAlt!""}">
                      </a>
                  </div>
              </#if>

              <#-- product details -->
              <div class="cm-cart-item__properties">
                <div class="cm-property cm-property--title">
                  <div class="cm-property__name"><@cm.message "cart_product" />:</div>
                  <div class="cm-property__value">
                      <a class="cm-property__link" href="${cm.getLink(productInSite)}">${item.product.name!""}</a>
                  </div>
                </div>
                <div class="cm-property cm-property--quantity">
                  <div class="cm-property__name"><@cm.message "cart_quantity" />:</div>
                  <div class="cm-property__value">${item.quantity!0}</div>
                </div>
                <div class="cm-property cm-property--price">
                  <div class="cm-property__name"><@cm.message "cart_price" />:</div>
                  <div class="cm-property__value">${totalPriceFormatted!""}</div>
                </div>
                <div class="cm-property cm-property--description">
                  <div class="cm-property__name"><@cm.message "cart_description" />:</div>
                  <div class="cm-property__value"><@cm.include self=item.product.shortDescription/></div>
                </div>
              </div>

              <#-- remove from cart button -->
              <button class="cm-cart-item__remove"
                      title="${cm.getMessage("cart_remove_item")}"
                      <@cm.dataAttribute name="data-cm-cart-remove-item"
                                         data={
                                           "id": item.externalId!"",
                                           "link": cm.getLink(self, "ajax"),
                                           "token": _CSRFToken!""
                                         } />>
                <span>${cm.getMessage("cart_remove_item")}</span>
              </button>
            </#if>
          </div>
      </#list>
    </div>
  <#-- cart is empty -->
  <#else>
    <div class="cm-cart__empty">
      <@cm.message "cart_empty" />
    </div>
  </#if>

  <div class="cm-cart__footer cm-button-group cm-button-group--equal">
    <@components.button href=cm.getLink(self)
                        text=cm.getMessage("cart_go_to_cart")
                        attr={"classes": ["cm-button-group__button cm-button--primary"]} />
  </div>
</div>
