<#-- @ftlvariable name="self" type="com.coremedia.blueprint.elastic.social.cae.controller.CommentWrapper" -->
<#-- @ftlvariable name="commentsResult" type="com.coremedia.blueprint.elastic.social.cae.controller.CommentsResult" -->

<li class="cm-collection__item">
  <@cm.include self=self.comment params={
    "commentingAllowed": commentsResult.isWritingContributionsAllowed()
  } />
  <#assign comments=self.subComments />
  <#if comments?has_content>
      <ul class="cm-collection cm-collection--comments">
        <#list comments as wrapper>
            <@cm.include self=wrapper!cm.UNDEFINED view="asListItem" params={"commentsResult": commentsResult} />
        </#list>
      </ul>
  </#if>
</li>