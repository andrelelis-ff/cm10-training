<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPerson" -->

<#--
    Template Description:

    This template can be used to render additional properties in the "misc" struct of a CMPerson. Although
    structs can be complex this template just renders string properties as links
    with key as a label and value as URL similiar to the email of a person.
    If you need more complex rendering of the struct, please overwrite this template in your theme.

-->
<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#assign blockClass=cm.localParameters().blockClass!"cm-details" />

<#list self.furtherDetails!{}>
<ul class="${blockClass}__misc" <@preview.metadata "properties.misc"/>>
  <#items as key, value>
    <#-- only support string key-value pairs -->
    <#if (key?is_string && value?is_string)>
      <li class="${blockClass}__misc-item">
        <@utils.optionalLink href="${value}" openInNewTab=true>
          <#assign iconClass="cm-misc-icon cm-misc-icon--" + key?lower_case?replace(" ", "")?trim />
          <span class="${blockClass}__misc-icon ${iconClass}"></span>
          <span class="${blockClass}__misc-text">${key}</span>
        </@utils.optionalLink>
      </li>
    </#if>
  </#items>
</ul>
</#list>
