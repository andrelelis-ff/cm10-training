<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/><#-- could be used as fragment -->
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.elastic.social.cae.controller.ComplaintResult" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<#if self.isEnabled()>
  <#assign complaintId=bp.generateId("cm-complaint-") />
  <div class="cm-complaints" id="${complaintId}" data-cm-refreshable-fragment='{"url": "${cm.getLink(self)}"}'>
      <form method="post" enctype="multipart/form-data" class="cm-new-complaint__form cm-form" action="${cm.getLink(self)}" data-cm-es-ajax-form=''>
         <input type="hidden" name="_CSRFToken" value="${_CSRFToken!""}">
        <@elasticSocial.notification type="inactive" text="" attr={"data-cm-notification": '{"path": ""}'} />
        <#if self.hasAlreadyComplained()>
            <h3 class="cm-comments__title">has complained</h3>
            <input type="hidden" name="complain" value="false">
        <#else>
            <h3 class="cm-comments__title">has not complained</h3>
            <input type="hidden" name="complain" value="true">
        </#if>
        <@components.button text=cm.getMessage("complaintForm_label_submit") attr={"type": "submit", "class": "cm-button cm-button--small"} />
      </form>
  </div>
</#if>
