<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.p13n.MarketingSpot" -->

<#if self?has_content && self.name?has_content>
    <#noautoesc><!--CM {"objectType":"espot","renderType":"display","id":"${self.name}"} CM--></#noautoesc>
</#if>
