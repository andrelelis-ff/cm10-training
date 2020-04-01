<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->
<#-- @ftlvariable name="navigation" type="com.coremedia.blueprint.common.navigation.Linkable" -->

<#list self.navigation.rootNavigation.visibleChildren>
  <#items as navigation>
  <li><@cm.include self=navigation view="asLink"/></li>
  </#items>
</#list>
