<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->
<#-- @ftlvariable name="self.content" type="com.coremedia.livecontext.ecommerce.catalog.Product" -->
<#-- @ftlvariable name="orientation" type="java.lang.String" -->
<#-- @ftlvariable name="types" type="java.lang.String" -->

<#if self.content?has_content>
  <@cm.include self=self.content view="asAssets" params={"orientation":"${orientation!''}", "types":"${types!''}"}/>
</#if>
