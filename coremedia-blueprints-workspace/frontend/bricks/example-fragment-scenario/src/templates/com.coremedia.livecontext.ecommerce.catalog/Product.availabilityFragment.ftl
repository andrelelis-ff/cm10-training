<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.catalog.Product" -->

<#if (self.totalStockCount > 0) >
  ${self.totalStockCount}
<#else>
  N/A
</#if>
