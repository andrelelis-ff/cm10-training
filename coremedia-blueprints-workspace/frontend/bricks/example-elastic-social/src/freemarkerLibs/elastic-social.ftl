<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#--
  Renders a label associated to Spring forms.

  @param path The path name of the field to bind to.
  @param text The text of the label. By default, the localization for the bound field will be applied.
  @param bindPath Prevents the rebinding of the path, for example, if you already know that the path is bound.
  @param attr Additional attributes for the label tag.

  Example:
  <@labelFromSpring path="bpLoginForm.name" text=cm.getMessage("login_name_label") />
-->
<#macro labelFromSpring path text="" bindPath=true attr={}>
  <#if bindPath><@spring.bind path=path /></#if>
  <label for="${_getIdFromExpression(spring.status.expression)}" <@utils.renderAttr attr />>${text}</label>
</#macro>

<#--
  Renders a notification.

  @param type Defines the type of the notification. Must be one of these values: error, warn, info, success or inactive.
  @param baseClass Defines the base class to be used for the notification and all its child elements.
  @param additionalClasses Additional classes to be attached to the root element of the notification.
  @param title Defines the title of the notification.
  @param text Defines the text of the notification.
  @param iconClass Defines an the CSS class of an icon to be attached to the notification.
  @param attr Defines additional attributes to be attached to the root element of the notification.

  Example:
  <@notification type="inactive"
                 text=""
                 additionalClasses=["cm-comment__notification"]
                 attr={"data-cm-notification": '{"path": ""}'}/>
-->
<#macro notification type baseClass="cm-notification" additionalClasses=[] title="" text="" iconClass="" attr={}>
  <#local classes=[baseClass, baseClass + "--" + type] + additionalClasses />
  <#local attr=utils.extendSequenceInMap(attr, "classes", classes) />
  <div<@utils.renderAttr attr=attr />>
    <#if iconClass?has_content>
      <i class="${iconClass}" aria-hidden="true"></i>
    </#if>
    <#if title?has_content>
      <span class="${baseClass}__headline">${title}</span>
    </#if>
    <span class="${baseClass}__text">
    <#if text?has_content>${text}</#if>
    <#nested />
    </span>
  </div>
</#macro>

<#--
  Renders a notification associated to Spring forms. The text will be determined automatically.

  @param path The name of the field to bind to.
  @param baseClass Defines the base class to be used for the notification and all its child elements.
  @param additionalClasses Additional classes to be attached to the root element of the notification.
  @param ignoreIfEmpty Specifies if the notification will not be rendered if Spring error messages are empty.
  @param type Defines the type of the notification. Must be one of these values: error, warn, info, success or inactive.
  @param title Defines the title of the notification.
  @param bindPath If false it prevents the rebinding of the path, for example, if you already know that the path is bound.
  @param attr Defines additional attributes to be attached to the root element of the notification.

  Example:
  <@notificationFromSpring path="bpLoginForm" additionalClasses=["my-class"]/>
-->
<#outputformat "plainText">
  <#macro notificationFromSpring path baseClass="cm-notification" additionalClasses=[] ignoreIfEmpty=true type="error" title="" bindPath=true attr={}>
    <#if bindPath><@spring.bind path=path /></#if>
    <#local text="" />
    <#if spring.status.error>
      <#local text=spring.status.getErrorMessagesAsString("\n") />
    </#if>
    <#if !ignoreIfEmpty?is_boolean || !ignoreIfEmpty || text?has_content>
      <@notification type=type baseClass=baseClass additionalClasses=additionalClasses title=title attr=attr>${text?replace("\n", "<br>")}<#nested /></@notification>
    </#if>
  </#macro>
</#outputformat>

<#-- PRIVATE -->
<#function _getIdFromExpression expression>
  <#return expression?replace("[", "")?replace("]", "") />
</#function>
