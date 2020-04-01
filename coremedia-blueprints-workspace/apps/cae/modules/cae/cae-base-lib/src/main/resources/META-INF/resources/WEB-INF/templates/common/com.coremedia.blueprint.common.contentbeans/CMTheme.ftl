<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTheme" -->

<#--
    Template Description:

    This template renders a preview of a theme.
-->

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>CoreMedia Theme Preview for ${self.content.name!""}</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <@preview.previewScripts />
</head>

<body id="top" class="cm-page-preview">

  <#-- simple preview of a theme -->
  <div class="cm-theme"<@preview.metadata self.content />>
    <#-- theme name -->
    <h1 class="cm-theme__title">${self.content.name!""}</h1>
    <#-- theme preview image -->
    <#if self.icon?has_content>
      <img class="cm-theme__screenshot" src="${cm.getLink(self.icon)}"<@preview.metadata "properties.icon" />>
    </#if>
    <#-- theme description-->
    <div class="cm-theme__description"<@preview.metadata "properties.detailText" />>
      <@cm.include self=self.detailText!cm.UNDEFINED />
    </div>
  </div>

</body>
</html>
