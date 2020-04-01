<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.catalog.Product" -->
<#-- @ftlvariable name="showListPrice" type="java.lang.Boolean" -->
<#-- @ftlvariable name="showOfferPrice" type="java.lang.Boolean" -->
<#-- @ftlvariable name="classListPrice" type="java.lang.String" -->
<#-- @ftlvariable name="classOfferPrice" type="java.lang.String" -->

<#-- default values if param is not set -->
<#assign showListPrice=showListPrice!true />
<#assign showOfferPrice=showOfferPrice!true />
<#assign classListPrice=classListPrice!"" />
<#assign classOfferPrice=classOfferPrice!"" />

<#assign listPriceFormatted="" />
<#assign offerPriceFormatted="" />
<#if self.currency?has_content && self.locale?has_content>
  <#if self.listPrice?has_content>
    <#assign listPriceFormatted=lc.formatPrice(self.listPrice, self.currency, self.locale)/>
  </#if>
  <#if self.offerPrice?has_content>
    <#assign offerPriceFormatted=lc.formatPrice(self.offerPrice, self.currency, self.locale)/>
  </#if>
</#if>
<#assign showListPrice=showListPrice && listPriceFormatted?has_content />
<#assign showOfferPrice=showOfferPrice && offerPriceFormatted?has_content && (!showListPrice || (offerPriceFormatted != listPriceFormatted)) />

<#if showOfferPrice>
  <div class="cm-price ${classOfferPrice}">${offerPriceFormatted}</div>
</#if>
<#if showListPrice>
  <div class="cm-price<#if showOfferPrice> cm-price--old</#if>">${listPriceFormatted}</div>
</#if>
