<#-- @ftlvariable name="self" type="com.coremedia.livecontext.contentbeans.CMMarketingSpot" -->

<#if self?has_content && self.marketingSpot?has_content>
  <div class="cm-marketingspot" <@preview.metadata self.content />>
    <@cm.include self=self.marketingSpot/>
  </div>
</#if>
