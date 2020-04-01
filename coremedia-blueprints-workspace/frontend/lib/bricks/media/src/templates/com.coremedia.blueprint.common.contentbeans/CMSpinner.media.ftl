<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMSpinner" -->

<#-- Just render the first picture of the sequence -->
<@cm.include self=(self.sequence?first)!cm.UNDEFINED view="media" params=cm.localParameters() />
