<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMAction" -->
<#-- this template is called by IBM's CheckoutLogon.jsp -->

<#assign substitution=cm.substitute(self.id, self) />
<@cm.include self=substitution view="asFragment" />
