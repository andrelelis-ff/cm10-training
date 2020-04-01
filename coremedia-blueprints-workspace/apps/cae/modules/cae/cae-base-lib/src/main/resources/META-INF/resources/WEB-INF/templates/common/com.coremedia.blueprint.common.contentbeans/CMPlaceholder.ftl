<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPlaceholder" -->
<#-- @ftlvariable name="recursiveInclude" type="java.lang.Boolean" -->

<#--
    Template Description:

    This template delegates a placeholder to another view/layout or substitution.
    a) layout: a placeholder with layout "foo" will delegate to the template CMPlaceholder.[foo].ftl
    b) id: if no layout is given, the placeholder tries to substitute the object by the id
-->

<#assign recursiveInclude=cm.localParameter("recursiveInclude", false) />
<#assign layout=(self.viewtype.layout)!"" />

<#-- use layout as view -->
<#if layout?has_content && !recursiveInclude>
  <@cm.include self=self view="[${layout}]" params={"recursiveInclude": true} />

<#-- use id and substitute, if no layout is set -->
<#elseif self.id?has_content>
  <#assign substitution=cm.substitute(self.id!"", self) />
  <@cm.include self=substitution />
</#if>
