<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/><#-- could be used as fragment -->
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.elastic.social.cae.controller.LikeResult" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<#if self.isEnabled()>
  <#assign likeId=bp.generateId("cm-like-") />
  <div class="cm-like" id="${likeId}" data-cm-refreshable-fragment='{"url": "${cm.getLink(self)}"}'>
      <form method="post" enctype="multipart/form-data" class="cm-new-like__form cm-form" action="${cm.getLink(self)}" data-cm-es-ajax-form=''>
         <input type="hidden" name="_CSRFToken" value="${_CSRFToken!""}">
        <@elasticSocial.notification type="inactive" text="" attr={"data-cm-notification": '{"path": ""}'} />
        <#if self.hasLiked()>
            <h3 class="cm-like__title">has liked of  likes ${self.numberOfLikes}</h3>
            <input type="hidden" name="like" value="false">
        <#else>
            <h3 class="cm-like__title">has not liked of likes ${self.numberOfLikes}</h3>
            <input type="hidden" name="like" value="true">
        </#if>
        <@components.button text="Like" attr={"type": "submit", "class": "cm-button cm-button--small"} />
      </form>
  </div>
</#if>
