<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Container" -->

<#assign even=cm.localParameters().even!false />

<@cm.include self=self view="asPlacement" params={"even": even} />
