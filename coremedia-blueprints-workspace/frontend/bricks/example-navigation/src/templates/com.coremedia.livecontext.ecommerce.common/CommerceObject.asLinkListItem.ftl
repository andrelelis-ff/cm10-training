<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.common.CommerceObject" -->
<#-- @ftlvariable name="cssClass" type="java.lang.String" -->
<#-- @ftlvariable name="collectionProperty" type="java.util.List" -->

<#assign cssClass=cm.localParameters().cssClass!""/>
<#assign depth=cm.localParameters().depth+1!1/>
<#assign showPicturesInNavigation=cm.localParameters().showPicturesInNavigation!true/>

<#-- add css class active, if this item is the actual page -->
<#if (self == cmpage.content)>
  <#assign cssClass= cssClass + " cm-navigation-item--active"/>
</#if>

<li class="${cssClass} cm-navigation-item cm-navigation-item--depth-${depth}" <@preview.metadata collectionProperty!["properties.children"]/>>
  <@cm.include self=self view="asLink" params={"cssClass" : "cm-navigation-item__title"}/>
</li>
