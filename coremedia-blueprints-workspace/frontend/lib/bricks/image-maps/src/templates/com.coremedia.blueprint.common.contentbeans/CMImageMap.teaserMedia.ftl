<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMImageMap" -->

<#assign teaserBlockClass=cm.localParameters().teaserBlockClass!cm.UNDEFINED />

<div class="${teaserBlockClass}__media cm-imagemap" <@cm.dataAttribute name="data-cm-imagemap" data={"coordsBaseWidth": bp.IMAGE_TRANSFORMATION_BASE_WIDTH} />>
  <@cm.include self=self view="_picture" params={
    "blockClass": cm.localParameter("teaserBlockClass", "cm-teasable"),
    "renderEmptyImage": cm.localParameter("renderEmptyImage", false)
  }
  />
</div>
