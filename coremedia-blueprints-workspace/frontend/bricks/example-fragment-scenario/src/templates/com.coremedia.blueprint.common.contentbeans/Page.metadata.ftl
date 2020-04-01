<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<#assign content=self.content>
<#assign augmentedContent = lc.augmentedContent()>
<#if !augmentedContent>
  <!--CM { "objectType":"page","renderType":"metadata","title":"","description":"","keywords":"","pageName":"" } CM-->
<#else>
<#noautoesc>
  <!--CM { "objectType":"page","renderType":"metadata","title":"${content.htmlTitle?json_string}","description":"${content.htmlDescription?json_string}",
  "keywords":"${content.keywords?json_string}","pageName":"${content.title?json_string}" } CM-->
</#noautoesc>
</#if>