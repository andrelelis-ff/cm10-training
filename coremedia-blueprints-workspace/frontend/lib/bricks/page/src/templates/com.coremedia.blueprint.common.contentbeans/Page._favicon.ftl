<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<#--
    Template Description:

    This partial template renders the favicon link for the page head. It can be overwritten in a theme to load more
    specific favicon files from the theme.

    @since 1907
-->

<#if self.favicon?has_content>
  <link rel="shortcut icon" href="${cm.getLink(self.favicon)}"<@preview.metadata "properties.favicon" />>
</#if>
