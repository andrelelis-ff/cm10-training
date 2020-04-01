<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.webflow.WebflowActionState" -->
<#-- @ftlvariable name="flowRequestContext" type="org.springframework.webflow.execution.RequestContext" -->
<#-- @ftlvariable name="emailAddress" type="java.lang.String" -->

<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<div class="cm-form cm-form--success"<@preview.metadata data=[self.action.content!"", "properties.id"]/>>
  <@elasticSocial.notification type="success" title=cm.getMessage("registration_title") text=cm.getMessage("registration_success", [emailAddress!""]) />
  <#if flowRequestContext?has_content && flowRequestContext.messageContext?has_content>
    <#list flowRequestContext.messageContext.allMessages![] as message>
      <@elasticSocial.notification type="warning" text=message.text!"" />
    </#list>
  </#if>
</div>
