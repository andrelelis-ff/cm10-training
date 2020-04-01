<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.webflow.WebflowActionState" -->
<#-- @ftlvariable name="flowRequestContext" type="org.springframework.webflow.execution.RequestContext" -->

<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<div class="cm-form cm-form--failure"<@preview.metadata data=[self.action.content!"", "properties.id"]/>>
  <#if flowRequestContext?has_content && flowRequestContext.messageContext?has_content>
    <#list flowRequestContext.messageContext.allMessages![] as message>
      <@elasticSocial.notification type="error" title=cm.getMessage("activateRegistration_failure_title") text=message.text!"" />
    </#list>
  </#if>
</div>
