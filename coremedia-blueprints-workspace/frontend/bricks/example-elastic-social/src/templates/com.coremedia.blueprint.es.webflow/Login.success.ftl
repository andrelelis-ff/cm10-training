<#-- @ftlvariable name="self" type="com.coremedia.blueprint.elastic.social.cae.action.AuthenticationState" -->

<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<div class="cm-form cm-form--success"<@preview.metadata data=[self.action.content!"", "properties.id"]/>>
  <@elasticSocial.notification type="success" text=cm.getMessage("login_success") />
</div>
