<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPlaceholder" -->

<#--
    Template Description:

    This templates delegates to the CMAction bean, defined in the content settings, with the view "asSearchField".
    If you want to add a search field on your website, add this placeholder to your pagegrid.
-->

<@cm.include self=bp.setting(self,"searchAction") view="asSearchField" />
