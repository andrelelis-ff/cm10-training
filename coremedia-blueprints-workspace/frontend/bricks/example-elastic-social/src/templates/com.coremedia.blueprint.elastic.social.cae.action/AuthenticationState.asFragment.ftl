<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/><#-- could be used as fragment -->
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.elastic.social.cae.action.AuthenticationState" -->
<#-- @ftlvariable name="fragmentParameter" type="com.coremedia.livecontext.fragment.FragmentParameters" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />

<#assign nextUrl="" />
<#if fragmentParameter?has_content>
  <#assign nextUrl=fragmentParameter.getParameter()!"" />
</#if>

<#-- Continue with authenticated and registered user-->
<#if self.authenticated>
  <a href="${nextUrl}"><@cm.message "checkout_continue" /></a>
<#else>
  <div class="cm-button-group cm-button-group--default">
    <#assign registrationAction=self.registrationAction!cm.UNDEFINED />
    <#assign registrationFlow=cm.substitute(registrationAction.id!"", registrationAction) />
    <#assign urlEncodedRedirect=nextUrl?url("utf-8")?url("utf-8") />
    <#assign link=cm.getLink(registrationFlow, cm.UNDEFINED, {"externalRedirect": "", "nextUrl": urlEncodedRedirect}) />
    <@components.button text=cm.getMessage("login_sign_up_button") href=link attr={"type": "submit", "classes": ["cm-button-group__button"]} />
  </div>
</#if>
