<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<#assign content=self.content>
<#assign augmentedContent = lc.augmentedContent()>

<#if augmentedContent>
<!--VTL $include.metadata('${content.htmlTitle?replace("'", "''")}','${content.htmlDescription?replace("'", "''")}','${content.keywords?replace("'", "''")}') VTL-->
</#if>
