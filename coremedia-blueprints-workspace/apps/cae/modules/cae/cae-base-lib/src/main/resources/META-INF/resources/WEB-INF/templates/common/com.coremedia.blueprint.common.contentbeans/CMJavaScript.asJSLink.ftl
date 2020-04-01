<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMJavaScript" -->

<#--
    Template Description:

    This template is used for JS files if
    - ieExpression (conditional comments for IE) is set
    - js includes an external css file
    - cae.use.local.resources or cae.developer.mode are set to true
    Otherwise MergableResources.asJSLink.ftl is used.
-->

<#assign jsLink=cm.getLink(self)/>
<#assign additionalAttributes="" />
<#list self.htmlAttributes?keys as key>
  <#assign additionalAttributes>${additionalAttributes?no_esc} ${key}="${self.htmlAttributes[key]?string}"</#assign>
</#list>

<#if self.ieExpression?has_content>
  <!--[if ${self.ieExpression}]><script src="${jsLink}"${additionalAttributes}<@preview.metadata self.content />></script><![endif]-->
<#else>
  <script src="${jsLink}"${additionalAttributes}<@preview.metadata self.content />></script>
</#if>
