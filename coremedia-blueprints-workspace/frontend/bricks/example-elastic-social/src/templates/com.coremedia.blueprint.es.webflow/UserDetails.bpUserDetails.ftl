<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.webflow.WebflowActionState" -->
<#-- @ftlvariable name="userDetails" type="com.coremedia.blueprint.elastic.social.cae.flows.UserDetails" -->
<#-- @ftlvariable name="elasticSocialConfiguration" type="com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration" -->
<#-- @ftlvariable name="flowExecutionKey" type="java.lang.String" -->
<#-- @ftlvariable name="_CSRFToken" type="java.lang.String" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<#assign elasticSocialConfiguration=es.getElasticSocialConfiguration(cmpage) />

<div class="cm-form cm-form--userdetails"<@preview.metadata data=[self.action.content!"", "properties.id"]/>>
  <#-- show user detauils-->
  <#if userDetails?has_content>

    <#-- headline -->
    <#if userDetails.viewOwnProfile>
      <h1 class="cm-form__headline"><@cm.message "userDetails_personalDetails" /></h1>
    </#if>
    <#if !userDetails.viewOwnProfile>
      <h1 class="cm-form__headline"><@cm.message "userDetails_title" /></h1>
    </#if>

    <#-- show properties -->
    <div class="cm-form">

      <#-- premoderation notification -->
      <#if userDetails.viewOwnProfile && userDetails.preModerationChanged>
        <div><@cm.message "userDetails_changesForPreModeration" /></div>
      </#if>

      <#-- image -->
      <#if userDetails.profileImage?has_content>
        <div class="cm-form-group">
          <label><@cm.message "userDetails_profileImage" /></label>
          <#assign link=cm.getLink(userDetails.profileImage {"transform":true, "width":elasticSocialConfiguration.userImageWidth!100?int, "height": elasticSocialConfiguration.userImageHeight!100?int})/>
          <img class="cm-form__image" src="${link}" title="" alt="userimage">
        </div>
      </#if>

      <#-- username -->
      <div class="cm-form-group">
        <@elasticSocial.labelFromSpring path="userDetails.username" text='${cm.getMessage("userDetails_username")}'/>
        <@spring.formInput path="userDetails.username" attributes='class="cm-form-control" disabled="disabled"'/>
      </div>

      <#if userDetails.viewOwnProfile || userDetails.preview>
        <div class="cm-form-group">
          <@elasticSocial.labelFromSpring path="userDetails.givenname" text='${cm.getMessage("userDetails_givenname")}'/>
        <@spring.formInput path="userDetails.givenname" attributes='class="cm-form-control" disabled="disabled"'/>
        </div>

        <div class="cm-form-group">
          <@elasticSocial.labelFromSpring path="userDetails.surname" text='${cm.getMessage("userDetails_surname")}'/>
        <@spring.formInput path="userDetails.surname" attributes='class="cm-form-control" disabled="disabled"'/>
        </div>

        <div class="cm-form-group">
          <@elasticSocial.labelFromSpring path="userDetails.emailAddress" text='${cm.getMessage("userDetails_emailAddress")}'/>
        <@spring.formInput path="userDetails.emailAddress" attributes='class="cm-form-control" disabled="disabled"' fieldType="email"/>
        </div>
      </#if>

      <#if userDetails.viewOwnProfile>
        <form method="post" enctype="multipart/form-data">
          <input type="hidden" name="execution" value="${flowExecutionKey!""}">
          <input type="hidden" name="_CSRFToken" value="${_CSRFToken!""}">
          <input type="hidden" name="_eventId_editUser">

          <div class="cm-form-group cm-form-group--text-end">
            <@components.button text=cm.getMessage("userDetails_editProfile") attr={"type": "submit", "id": "saveUser"} />
          </div>

        </form>
      </#if>

    <#--
    <#if elasticSocialConfiguration?has_content && elasticSocialConfiguration.complainingEnabled && !userDetails.viewOwnProfile
          && (es.isAnonymousUser() || (!es.isAnonymousUser() && !userDetails.viewOwnProfile))>
      <#assign navigationId=bp.id((cmpage.navigation.content.id)!) />
      <@es.complaining id=userDetails.id collection="users"
      value=es.hasComplaintForCurrentUser(userDetails.id, "users")
      itemId=itemId navigationId=navigationId />
    </#if>
    -->
    </div>

        <#-- user activity -->
    <h2><@cm.message "userDetails_logging"/></h2>
    <div class="cm-form">
      <#if userDetails.lastLoginDate?has_content>
        <div class="cm-form-group">
          <label class="cm-form__label">${cm.getMessage("userDetails_lastLoginDate")}</label>
          <span class="cm-help-block">${userDetails.lastLoginDate!?datetime?string.medium}</span>
        </div>
      </#if>
      <#if userDetails.registrationDate?has_content>
        <div class="cm-form-group">
          <label class="cm-form__label">${cm.getMessage("userDetails_registrationDate")}</label>
          <span class="cm-help-block">${userDetails.registrationDate!?datetime?string.medium}</span>
        </div>
      </#if>
      <#if (userDetails.numberOfLogins?has_content && userDetails.numberOfLogins > 0)>
        <div class="cm-form-group">
          <label class="cm-form__label">${cm.getMessage("userDetails_numberOfLogins")}</label>
          <span class="cm-help-block">${userDetails.numberOfLogins}</span>
        </div>
      </#if>
      <#if (userDetails.numberOfReviews?has_content && userDetails.numberOfReviews > 0)>
      <div class="cm-form-group">
        <label class="cm-form__label">${cm.getMessage("userDetails_numberOfReviews")}</label>
          <span class="cm-help-block">${userDetails.numberOfReviews}</span>
      </div>
      </#if>
      <#if (userDetails.numberOfComments?has_content && userDetails.numberOfComments > 0)>
        <div class="cm-form-group">
          <label class="cm-form__label">${cm.getMessage("userDetails_numberOfComments")}</label>
          <span class="cm-help-block">${userDetails.numberOfComments}</span>
        </div>
      </#if>
      <#if (userDetails.numberOfRatings?has_content && userDetails.numberOfRatings > 0)>
        <div class="cm-form-group">
          <label class="cm-form__label">${cm.getMessage("userDetails_numberOfRatings")}</label>
          <span class="cm-help-block">${userDetails.numberOfRatings}</span>
        </div>
      </#if>
      <#if (userDetails.numberOfLikes?has_content && userDetails.numberOfLikes > 0)>
        <div class="cm-form-group">
          <label class="cm-form__label">${cm.getMessage("userDetails_numberOfLikes")}</label>
          <span class="cm-help-block">${userDetails.numberOfLikes}</span>
        </div>
      </#if>
    </div>

    <#-- personalization -->
    <#--#if userDetails.viewOwnProfile>
      <@cm.include self=self view="showPersonalizationForm"/>
    </#if-->

  <#else>
    <@elasticSocial.notification type="error" text=cm.getMessage("userDetails_noUserFound") />
  </#if>
</div>
