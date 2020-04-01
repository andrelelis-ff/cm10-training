<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.webflow.WebflowActionState" -->
<#-- @ftlvariable name="_CSRFToken" type="java.lang.String" -->
<#-- @ftlvariable name="flowExecutionKey" type="java.lang.String" -->
<#-- @ftlvariable name="nextUrl" type="java.lang.String" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<div class="cm-form cm-form--reset"<@preview.metadata data=[self.action.content!"", "properties.id"]/>>
  <h1 class="cm-form__headline"><@cm.message "passwordReset_title" /></h1>
  <form method="post" data-cm-form--forgot="">
    <input type="hidden" name="_CSRFToken" value="${_CSRFToken!""}">
    <input type="hidden" name="execution" value="${flowExecutionKey!""}">
    <input type="hidden" name="nextUrl" value="${nextUrl!""}">
    <input type="hidden" name="_eventId_submit">

    <#-- notification -->
    <@elasticSocial.notificationFromSpring path="bpPasswordReset" />

    <#-- email -->
    <div class="cm-form-group">
      <@elasticSocial.labelFromSpring path="bpPasswordReset.emailAddress" text='${cm.getMessage("passwordReset_email_label")}'/>
      <@spring.formInput path="bpPasswordReset.emailAddress" attributes='class="cm-form-control" placeholder="${cm.getMessage("passwordReset_email_label")}" required' fieldType="text"/>
    </div>

    <div class="cm-form-group cm-form-group--text-end">
      <@components.button text=cm.getMessage("passwordReset_button") attr={"type": "submit"} />
    </div>
  </form>
</div>
