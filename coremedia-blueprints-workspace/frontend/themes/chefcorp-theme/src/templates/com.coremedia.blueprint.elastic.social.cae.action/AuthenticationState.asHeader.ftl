<#-- @ftlvariable name="self" type="com.coremedia.blueprint.elastic.social.cae.action.AuthenticationState" -->
<#-- @ftlvariable name="profileAction" type="com.coremedia.blueprint.common.contentbeans.CMAction" -->
<#-- @ftlvariable name="loginLink" type="java.lang.String" -->
<#-- @ftlvariable name="logoutLink" type="java.lang.String" -->

<#assign elasticSocialConfiguration=es.getElasticSocialConfiguration(cmpage) />
<#assign loginAction=self.loginAction />
<#assign logoutAction=self.logoutAction />
<#assign profileAction=self.profileAction />

<#-- show login/logout link, if ES is enabled. this templates is included as fragment (DynamicInclude.ftl) -->
<#if elasticSocialConfiguration.isFeedbackEnabled()!false>
  <#if loginAction?has_content && logoutAction?has_content && profileAction?has_content>
  <#-- user profile / logout -->
    <#if self.authenticated>
      <#assign logoutLink=cm.getLink(logoutAction)/>
      <li class="cm-header__user-chooser cm-navigation-item cm-navigation-item--depth-1 cm-navigation-item--special-depth-1"<@preview.metadata data=[logoutAction.content, "properties.id"] />>
        <span class="cm-navigation-item__title" title="${self.user.givenName}">
          ${self.user.givenName!""} ${self.user.surName!""}
        </span>
        <button type="button" class="cm-navigation-item__toggle" aria-haspopup="true" disabled></button>
        <ul id="loginMenu" class="cm-navigation-item__menu">
          <li class="cm-navigation-item cm-navigation-item--depth-2 cm-navigation-item--special-depth-2">
            <a href="${cm.getLink(profileAction)}" class="cm-navigation-item__title"<@preview.metadata data=[profileAction.content, "properties.id"] />>
              <@cm.message "userDetails_title" />
            </a>
          </li>
          <li class="cm-navigation-item cm-navigation-item--depth-2 cm-navigation-item--special-depth-2">
            <a href="${logoutLink}" class="cm-navigation-item__title"<@preview.metadata data=[logoutAction.content, "properties.id"] />>
              <@cm.message "logout_title" />
            </a>
          </li>
        </ul>
      </li>
    <#-- login -->
    <#else>
      <li class="cm-header__login cm-navigation-item cm-navigation-item--depth-1 cm-navigation-item--special-depth-1"<@preview.metadata data=[loginAction.content, "properties.id"] />>
        <#assign loginLink=cm.getLink(self, {"next": "$nextUrl$", "absolute": true, "scheme": "https"})/>
        <a class="cm-navigation-item__title" href="#" data-href="${loginLink}"<@preview.metadata data="properties.teaserTitle" />>
          ${self.action.teaserTitle!""}
        </a>
      </li>
    </#if>
  </#if>
</#if>
