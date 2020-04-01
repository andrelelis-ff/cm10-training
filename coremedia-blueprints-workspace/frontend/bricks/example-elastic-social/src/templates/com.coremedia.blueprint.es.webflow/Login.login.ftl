<#-- @ftlvariable name="self" type="com.coremedia.blueprint.elastic.social.cae.action.AuthenticationState" -->
<#-- @ftlvariable name="flowExecutionKey" type="java.lang.String" -->
<#-- @ftlvariable name="nextUrl" type="java.lang.String" -->
<#-- @ftlvariable name="_CSRFToken" type="java.lang.String" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<#assign elasticSocialConfiguration=es.getElasticSocialConfiguration(cmpage) />
<#assign loginAction=self.loginAction!cm.UNDEFINED />
<#assign loginFlow=cm.substitute(loginAction.id!"", loginAction) />
<#assign classContainer=cm.localParameters().classContainer!"" />
<#assign forgotPasswordAction=self.passwordResetAction!cm.UNDEFINED />
<#assign forgotPasswordUrl=cm.getLink(forgotPasswordAction, {"next": nextUrl})/>
<#assign registrationAction=self.registrationAction!cm.UNDEFINED />
<#assign registrationFlow=cm.substitute(registrationAction.id!"", registrationAction) />
<#assign registerLink=cm.getLink(registrationFlow, {"next": nextUrl, "absolute": true, "scheme": "https"})/>

<#if elasticSocialConfiguration?has_content>
  <div class="cm-form cm-form--login"<@preview.metadata data=[loginAction.content!"", "properties.id"] />>
    <#-- login -->
    <h1 class="cm-form__headline"><@cm.message "login_sign_in" /></h1>
    <form method="post">
      <input type="hidden" name="_CSRFToken" value="${_CSRFToken!""}">
      <input type="hidden" name="execution" value="${flowExecutionKey!""}">
      <input type="hidden" name="nextUrl" value="${nextUrl!""}">
      <input type="hidden" name="_eventId_submit">

      <#-- notification -->
      <@elasticSocial.notificationFromSpring path="bpLoginForm" />

      <#-- Login Name -->
      <div class="cm-form-group">
        <@elasticSocial.labelFromSpring path="bpLoginForm.name" text='${cm.getMessage("login_name_label")}'/>
        <@spring.formInput path="bpLoginForm.name" attributes='class="cm-form-control" placeholder="${cm.getMessage("login_name_label")}" required'/>
      </div>

      <#-- Password -->
      <div class="cm-form-group">
        <@elasticSocial.labelFromSpring path="bpLoginForm.password" text='${cm.getMessage("login_password_label")}'/>
        <@spring.formInput path="bpLoginForm.password" fieldType="password" attributes='class="cm-form-control" placeholder="${cm.getMessage("login_password_label")}" required'/>
      </div>

      <#-- Forgot Password Link -->
      <#if forgotPasswordUrl?has_content>
        <div class="cm-form-group">
          <a href="${forgotPasswordUrl!""}"
             class="cm-form__link"><@cm.message "login_forgot_password" /></a>
        </div>
      </#if>

      <#-- Send Button -->
      <div class="cm-form-group">
        <@components.button text=cm.getMessage("login_sign_in") attr={"type": "submit"} />
      </div>
    </form>

    <div class="cm-form__container">
      <h3><@cm.message "login_create_account" /></h3>
      <a href="${registerLink}" class="cm-button cm-button--secondary">
        <@cm.message "login_sign_up_button" />
      </a>
    </div>
  </div>
</#if>
