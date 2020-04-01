<#-- @ftlvariable name="self" type="com.coremedia.blueprint.elastic.social.cae.controller.CommentsResult" -->
<#-- @ftlvariable name="_CSRFToken" type="java.lang.String" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<div class="cm-comments__new-comment cm-new-comment">
  <form method="post" enctype="multipart/form-data" class="cm-new-comment__form cm-form" action="${cm.getLink(self)}" data-cm-es-ajax-form=''>
    <@elasticSocial.notification type="inactive" text="" additionalClasses=["cm-form__notification"] attr={"data-cm-notification": '{"path": ""}'} />

    <input type="hidden" name="_CSRFToken" value="${_CSRFToken!""}">
    <input type="hidden" name="replyTo" value="">
    <fieldset class="cm-form__fieldset cm-fieldset">
      <div class="cm-fieldset__item cm-field cm-field--detail">
        <@elasticSocial.notification type="inactive" text="" additionalClasses=["cm-field__notification"] attr={"data-cm-notification": '{"path": "comment"}'} />
        <#assign idText=bp.generateId("cm-new-comment__textarea-") />
        <label for="${idText}" class="cm-field__name"><@cm.message "commentForm_label_text" /></label>
        <textarea name="comment" class="cm-field__value cm-textarea" id="${idText}" required="" placeholder="${cm.getMessage("commentForm_error_commentBlank")}"></textarea>
      </div>
      <div class="cm-fieldset__item cm-button-group cm-button-group--default">
        <@components.button text=cm.getMessage("commentForm_label_hide") attr={"type": "button", "classes": ["btn", "cm-button-group__button", "cm-button--secondary"], "data-cm-button--cancel": ""} />
        <@components.button text=cm.getMessage("commentForm_label_submit") attr={"type": "submit", "classes": ["btn", "cm-button-group__button"], "data-cm-button--submit": ""} />
      </div>
    </fieldset>
  </form>
</div>
