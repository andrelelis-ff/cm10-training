<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Container" -->

<#--
    Template Description:

    This template renders a container (collection or placement) and all its items.
    Please check the brick "livecontext" or "generic templates" for a more detailed version.
-->

<div class="cm-container">
  <#list self.items![] as item>
    <@cm.include self=item />
  </#list>
</div>
