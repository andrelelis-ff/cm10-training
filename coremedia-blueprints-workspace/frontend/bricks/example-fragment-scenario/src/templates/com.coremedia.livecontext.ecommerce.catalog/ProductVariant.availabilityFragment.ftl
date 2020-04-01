<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.catalog.ProductVariant" -->

<#if (self.availabilityInfo.quantity > 0) >
  ${self.availabilityInfo.quantity}
<#else>
  N/A
</#if>
