<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<#--
    Template Description:

    This is the start point for all pages. It includes the head and body as partial templates.
    Please check the brick "generic templates" for a more detailed version.
-->

<!DOCTYPE html>
<html lang="${bp.getPageLanguageTag(self)}" dir="${bp.getPageDirection(self)}" <@preview.metadata data=bp.getPageMetadata(self)!"" />>
  <@cm.include self=self view="_head"/>
  <@cm.include self=self view="_body"/>
</html>
