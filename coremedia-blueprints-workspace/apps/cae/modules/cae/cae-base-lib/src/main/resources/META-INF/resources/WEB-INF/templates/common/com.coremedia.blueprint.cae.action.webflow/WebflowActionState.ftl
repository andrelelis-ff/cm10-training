<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.webflow.WebflowActionState" -->

<#--
    Template Description:

    Dispatcher for rendering WebflowActionState objects. Simply delegate to the same object but using
    the flowViewId (this is not the flowId!) as a view name. Due to "WebflowActionState"'s special
    "HasCustomType" implementation, a template that is named like the "flowId" will be used. Example:
    Let's say the flowId is "com.mycompany.MyFlowId" and the flowViewId is "success",
    then the template "com.mycompany/MyFlowId.success.ftl" is used.
    The type hierachy used for the template lookup also includes WebflowActionState,
    so we make sure we do not include ourselves. This means that flows require a "flowViewId".
-->

<#assign flowViewId=self.flowViewId!""/>
<#if flowViewId?has_content>
  <@cm.include self=self view=flowViewId />
</#if>
