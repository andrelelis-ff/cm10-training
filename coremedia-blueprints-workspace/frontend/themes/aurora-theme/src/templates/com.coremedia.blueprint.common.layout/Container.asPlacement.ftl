<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Container" -->

<#assign even=cm.localParameters().even!false />

<#list self.items![] as item>
  <@cm.include self=item view="asTeaser" params={"even": even?then(item?is_odd_item, item?is_even_item) }/>
</#list>
