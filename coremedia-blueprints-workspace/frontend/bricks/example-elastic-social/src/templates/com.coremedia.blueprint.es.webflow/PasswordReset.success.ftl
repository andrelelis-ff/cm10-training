<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.webflow.WebflowActionState" -->
<#-- @ftlvariable name="emailAddress" type="java.lang.String" -->

<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<div class="cm-form cm-form--success"<@preview.metadata data=[self.action.content!"", "properties.id"]/>>
  <@elasticSocial.notification type="success" title=cm.getMessage("passwordReset_title") text=cm.getMessage("passwordReset_success", [emailAddress!""]) />
</div>
