<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPerson" -->

<#--
    Template Description:

    This template renders elements from the related property in a section with the given view "relatedView".
    This is by default "asRelatedForPerson".

    @since 1901
-->

<#assign additionalClass=cm.localParameters().additionalClass!""/>
<#assign relatedView=cm.localParameters().relatedView!"asRelatedForPerson" />

<#if self.related?has_content>
  <section class="cm-related ${additionalClass}" <@preview.metadata "properties.related"/>>
    <#--items in related property -->
    <#list self.related![] as item>
      <@cm.include self=item view=relatedView />
    </#list>
  </section>
</#if>
