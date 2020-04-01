<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.webflow.WebflowActionState" -->
<#-- @ftlvariable name="flowExecutionKey" scope="request" type="java.lang.String" -->
<#-- @ftlvariable name="_CSRFToken" type="java.lang.String" -->
<#-- @ftlvariable name="nextUrl" type="java.lang.String" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<div class="cm-form cm-form--resetpassword"<@preview.metadata data=[self.action.content!"", "properties.id"]/>>
  <h1 class="cm-form__headline"><@cm.message "passwordReset_title" /></h1>
  <form method="post" data-cm-form--reset="">
    <input type="hidden" name="_CSRFToken" value="${_CSRFToken!""}">
    <input type="hidden" name="execution" value="${flowExecutionKey!""}">
    <input type="hidden" name="nextUrl" value="${nextUrl!""}">
    <input type="hidden" name="_eventId_submit">

    <@elasticSocial.notification type="info" title=cm.getMessage("confirmPasswordReset_title") />

    <#-- new password -->
    <@spring.bind path="bpPasswordReset.password"/>
    <div class="cm-form-group<#if spring.status.error> cm-form-group--has-error</#if>">
    <@elasticSocial.labelFromSpring path="bpPasswordReset.password" text='${cm.getMessage("confirmPasswordReset_password_label")}'/>
    <@spring.formInput path="bpPasswordReset.password" attributes='class="cm-form-control" placeholder="${cm.getMessage("confirmPasswordReset_password_label")}" required' fieldType="password"/>
    <#if spring.status.error><span class="cm-help-block">${spring.status.getErrorMessagesAsString("\n")}</span></#if>
    </div>

    <#-- repeat new password -->
    <@spring.bind path="bpPasswordReset.confirmPassword"/>
    <div class="cm-form-group<#if spring.status.error> cm-form-group--has-error</#if>">
    <@elasticSocial.labelFromSpring path="bpPasswordReset.confirmPassword" text='${cm.getMessage("confirmPasswordReset_confirmPassword_label")}'/>
    <@spring.formInput path="bpPasswordReset.confirmPassword" attributes='class="cm-form-control" placeholder="${cm.getMessage("confirmPasswordReset_confirmPassword_label")}" required' fieldType="password"/>
    <#if spring.status.error><span class="cm-help-block">${spring.status.getErrorMessagesAsString("\n")}</span></#if>
    </div>

    <div class="cm-form-group cm-form-group--text-end">
      <@components.button text="Reset" attr={"type": "submit"} />
    </div>
  </form>
</div>
