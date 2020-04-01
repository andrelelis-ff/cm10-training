<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="java.lang.Object" -->
<#assign sliderMetadata=(cmpage?has_content)?then(bp.setting(self, "sliderMetaData", ""), '')/>

<!DOCTYPE html>
<html lang="${bp.getPageLanguageTag(cmpage!self)}" dir="${bp.getPageDirection(cmpage!self)!'ltr'}">

<#if cmpage?has_content>
  <@cm.include self=cmpage view="_head"/>
<#else>
  <head>
    <meta charset="UTF-8">
    <title>CoreMedia Studio Preview</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <@preview.previewScripts />
  </head>
</#if>

<body id="top" class="cm-preview"<@preview.metadata sliderMetadata!"" />>

  <#-- include fragmented preview -->
  <@cm.include self=self view="asPreview"/>

  <#if cmpage?has_content>
    <@cm.include self=cmpage view="_bodyEnd"/>
  </#if>

</body>
</html>
