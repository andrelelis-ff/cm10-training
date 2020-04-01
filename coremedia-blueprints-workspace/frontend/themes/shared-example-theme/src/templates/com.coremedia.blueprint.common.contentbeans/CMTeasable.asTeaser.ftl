<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#--
    Template Description:

    This template redirects to the "default" view "asLeftRightBanner".

    @since 1907
-->

<#assign even=cm.localParameters().even!false />

<@cm.include self=self view="asLeftRightBanner" params={"even": even} />
