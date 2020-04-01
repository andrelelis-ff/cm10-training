<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Container" -->

<#--
  Template Description:

  This template renders a container with a given view called "containerView".
  Please check Object.asContainerPreview.ftl for all other objects, wrapped in a container.

  @since 1907
-->

<@cm.include self=self view=cm.localParameters().containerView!cm.UNDEFINED />
