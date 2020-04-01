<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMAction" -->

<#--
    Template Description:

    This templates delegates to the SearchActionState with the view asSearchField.
-->

<@cm.include self=cm.substitute(self.id!"", self) view="asSearchField" />
