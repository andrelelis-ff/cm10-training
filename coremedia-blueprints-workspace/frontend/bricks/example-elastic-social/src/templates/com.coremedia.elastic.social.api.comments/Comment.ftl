<#-- @ftlvariable name="self" type="com.coremedia.elastic.social.api.comments.Comment" -->
<#-- @ftlvariable name="commentView" type="java.lang.String" -->
<#-- @ftlvariable name="commentingAllowed" type="java.lang.Boolean" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<#assign commentView=es.getCommentView(self) />

<div class="cm-comment" data-cm-comment-id="${self.id}"<@preview.metadata self/>>

  <#assign strAuthorName=self.authorName!"" />
  <#assign strDate=self.creationDate?datetime?string.long_short />
  <#if !strAuthorName?has_content>
    <#if !self.author?has_content || es.isAnonymous(self.author)>
      <#assign strAuthorName=cm.getMessage("comment_author_anonymous") />
    <#else>
      <#assign strAuthorName=(self.author.name)!"" />
    </#if>
  </#if>
  <#if ["default", "undecided", "rejected"]?seq_contains(commentView)>
    <div class="cm-comment__header">
      <div class="cm-comment__author-date">
        <@cm.message key="comment_author_by" /> <span class="cm-comment__author">${strAuthorName!""}</span>
        <span class="cm-comment__date">${strDate}</span>
      </div>
    </div>
  </#if>
  <#-- output of comment specific information -->
  <#-- At least one dynamic notification is rendered -->
  <#if ["undecided"]?seq_contains(commentView)>
    <@elasticSocial.notification type="info" text=cm.getMessage("comment_approval_undecided") additionalClasses=["cm-comment__notification"] attr={"data-cm-notification": '{"path": ""}', "data-cm-contribution-notification-type": "UNDECIDED"} />
  <#elseif ["rejected"]?seq_contains(commentView)>
    <@elasticSocial.notification type="warning" text=cm.getMessage("comment_approval_rejected") additionalClasses=["cm-comment__notification"] attr={"data-cm-notification": '{"path": ""}', "data-cm-contribution-notification-type": "REJECTED"} />
  <#elseif ["deleted"]?seq_contains(commentView)>
    <div class="cm-comment__deleted">
      <@cm.message key="comment_deleted" />
    </div>
  <#else>
    <@elasticSocial.notification type="inactive" text="" additionalClasses=["cm-comment__notification"] attr={"data-cm-notification": '{"path": ""}'} />
  </#if>
  <#if ["default", "undecided", "rejected"]?seq_contains(commentView)>
    <div class="cm-comment__text cm-readmore" data-cm-readmore='{"lines": 5, "text": "${cm.getMessage("comment_more")}"}'>
      <div class="cm-readmore__wrapper">
        ${self.textAsHtml?no_esc}
      </div>
      <div class="cm-readmore__buttonbar">
        <@components.button baseClass="" text=cm.getMessage("comment_more") attr={"class": "cm-readmore__button-more"} />
        <@components.button baseClass="" text=cm.getMessage("comment_less") attr={"class": "cm-readmore__button-less"} />
      </div>
    </div>
  </#if>
  <#if ["default", "undecided"]?seq_contains(commentView) && commentingAllowed!false>
    <div class="cm-comment__toolbar cm-toolbar cm-toolbar--comments">
      <@components.button text=cm.getMessage("commentForm_label_reply") attr={"data-cm-button--comment": '{"replyTo": "${self.id}"}'} />
      <@components.button text=cm.getMessage("commentForm_label_quote") attr={"data-cm-button--comment": '{"replyTo": "${self.id}", "quote": {"author": "${(strAuthorName!"")?json_string}", "date": "${strDate?json_string}", "text": "${self.text?json_string}"}}'} />
    </div>
  </#if>

</div>
