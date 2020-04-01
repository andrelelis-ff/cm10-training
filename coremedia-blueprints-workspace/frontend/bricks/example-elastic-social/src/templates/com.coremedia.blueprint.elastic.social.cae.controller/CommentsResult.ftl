<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/><#-- could be used as fragment -->
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.elastic.social.cae.controller.CommentsResult" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<#if self.isEnabled()>
  <#assign commentsId=bp.generateId("cm-comments-") />
<div class="cm-comments" id="${commentsId}" data-cm-refreshable-fragment='{"url": "${cm.getLink(self)}"}'>
  <#assign numberOfContributions=(self.getNumberOfContributions())!0 />
  <#if (numberOfContributions != 0) || (!self.isReadOnly())>
      <h3 class="cm-comments__title">
        <#switch numberOfContributions>
          <#case 0>
          <@cm.message key="comments_no_comments" />
          <#break>
          <#case 1>
            <@cm.message key="comments_headline_singular" />
            <#break>
          <#default>
            <@cm.message key="comments_headline" args=[numberOfContributions] />
        </#switch>
      </h3>
  </#if>

  <#if self.isWritingContributionsAllowed()>
  <#-- output of dynamic, non-comment specific information -->
    <@elasticSocial.notification type="inactive" text="" additionalClasses=["cm-comments__notification"] attr={"data-cm-notification": '{"path": ""}'} />

      <div class="cm-comments__toolbar cm-toolbar cm-toolbar--comments">
        <@components.button text=cm.getMessage("comments_write") attr={"data-cm-button--comment": '{"replyTo": ""}'} />
      </div>
  <#elseif self.isWritingContributionsEnabled() && es.isAnonymousUser()>
    <@elasticSocial.notification type="info" text=cm.getMessage("commentForm_not_logged_in") additionalClasses=["cm-comments__notification"] attr={"data-cm-comments-notification-type": "LOGIN_REQUIRED"} />
      <div class="cm-comments__toolbar cm-toolbar cm-toolbar--comments">
        <@cm.include self=es.getLogin()!cm.UNDEFINED view="asButtonGroup" />
      </div>
  </#if>
  <@cm.include self=self view="commentForm" />

  <#assign comments=self.rootComments />
  <#if comments?has_content>
      <ul class="cm-collection cm-collection--comments cm-comments__list">
        <#list comments as wrapper>
            <@cm.include self=wrapper!cm.UNDEFINED view="asListItem" params={"commentsResult": self} />
        </#list>
      </ul>
  </#if>
</div>
</#if>
