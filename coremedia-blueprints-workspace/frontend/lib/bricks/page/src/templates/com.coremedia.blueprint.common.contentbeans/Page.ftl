<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<!DOCTYPE html>
<html class="no-js" lang="${bp.getPageLanguageTag(self)}" dir="${bp.getPageDirection(self)}" <@preview.metadata data=bp.getPageMetadata(self)!"" />>
  <@cm.include self=self view="_head"/>
  <@cm.include self=self view="_body"/>
</html>
