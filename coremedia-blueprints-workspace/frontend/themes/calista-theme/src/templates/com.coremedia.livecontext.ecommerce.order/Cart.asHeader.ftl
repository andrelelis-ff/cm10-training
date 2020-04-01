<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/><#-- could be used as fragment -->
<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.order.Cart" -->

<#assign labelCart=cm.getMessage("cart") />

<div class="cm-icon--cart"
     data-cm-cart-control=""
     data-cm-refreshable-fragment='{"url": "${cm.getLink(self, "fragment", {"targetView": "asHeader"})}"}'>
  <a href="${cm.getLink(self)}" title="${labelCart}" class="cm-cart-icon" aria-label="${labelCart}">
    <span class="cm-cart-icon__badge" data-cm-cart-badge="">${self.totalQuantity}</span>
  </a>
  <div class="cm-cart-popup">
    <@cm.include self=self view="asCart" />
  </div>
</div>
