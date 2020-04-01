<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/><#-- could be used as fragment -->
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.elastic.social.cae.action.AuthenticationState" -->
<#-- @ftlvariable name="profileFlow" type="com.coremedia.blueprint.common.contentbeans.CMAction" -->
<#-- @ftlvariable name="loginLink" type="java.lang.String" -->
<#-- @ftlvariable name="logoutLink" type="java.lang.String" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />

<#assign loginAction=self.loginAction />
<#assign logoutAction=self.logoutAction />
<#assign profileAction=self.profileAction />

<#if loginAction?has_content && logoutAction?has_content && profileAction?has_content>
  <#if self.authenticated>
    <div class="cm-button-group cm-button-group--default">
      <@components.button href="${cm.getLink(logoutAction!cm.UNDEFINED)}" text=cm.getMessage("logout_title") attr={"metadata": [logoutAction.content, "properties.id"]} />
      <@components.button href="${cm.getLink(profileAction!cm.UNDEFINED)}" text=cm.getMessage("userDetails_title") attr={"metadata": [profileAction.content, "properties.id"], "classes": ["cm-button-group__button"]} />
    </div>
  <#else>
    <div class="cm-button-group cm-button-group--default">
      <@components.button href="#" text=cm.getMessage("login_title") attr={"metadata": [loginAction.content, "properties.id"], "classes": ["cm-button-group__button"], "data-href": cm.getLink(self, {"next": "$nextUrl$", "absolute": true})} />
    </div>
  </#if>
</#if>
