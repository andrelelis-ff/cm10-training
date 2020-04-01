<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMAction" -->

<#assign substitution=cm.substitute(self.id!"", self) />
<@cm.include self=substitution view="asHeader" />
