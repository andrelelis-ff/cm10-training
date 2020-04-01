<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#--
    Template Description:

    This template renders the property authors as list with the view "asBlurb".
-->

<#assign parentClass=cm.localParameters().parentClass!"cm-details"/>

<#list self.authors![]>
  <section class="${parentClass}__authors" <@preview.metadata "properties.authors"/>>
    <#-- headline -->
    <h3 class="${parentClass}__authors-headline"><@cm.message key="authors_label"/></h3>
    <#--authors -->
    <#items as author>
      <@cm.include self=author view="asBlurb" />
    </#items>
  </section>
</#list>
