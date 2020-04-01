<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMHTML" -->

<#--
    Template Description:

    This template renders the data of a CMHTML with the programed view "script". For CoreMedia Studio the preview CAE
    will render an additional surrounding span with preview metadata.
-->

<#if preview.isPreviewCae()><span<@preview.metadata self.content/>></#if>
<#if self.data?has_content>
  <@cm.include self=self.data view="script" />
</#if>
<#if preview.isPreviewCae()></span></#if>
