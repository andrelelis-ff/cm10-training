<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<#--
    Template Description:

    This template delegates to Page._additionalHead.ftl which includes all CSS and javascripts in the head. Same as in
    cms-only pages. Could be used as a fragment in a LiveContext environment. Please check the brick "fragment-scenario"
    for a more detailed version.
-->

<@cm.include self=self view="_additionalHead" />
