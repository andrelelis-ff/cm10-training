<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.navigation.Navigation" -->

<#list cmpage.navigation.navigationPathList>
  <#items as navigation>
  <@cm.include self=navigation view="asLink" params={"cssClass":"breadcrumb-element"}/>
  </#items>
</#list>

