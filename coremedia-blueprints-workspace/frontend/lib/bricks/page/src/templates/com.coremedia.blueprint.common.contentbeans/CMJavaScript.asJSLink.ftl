<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMJavaScript" -->

<#--
    Template Description:

    This template is used for JS files if
    - ieExpression (conditional comments for IE) is set
    - js includes an external css file
    - cae.use.local.resources or cae.developer.mode are set to true
    Otherwise MergableResources.asJSLink.ftl is used.
-->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#assign jsLink=cm.getLink(self)/>
<#assign attr=self.htmlAttributes />
<#assign ignore=["src"] />

<#if self.ieExpression?has_content>
  <!--[if ${self.ieExpression}]><script src="${jsLink}"<@utils.renderAttr attr=attr ignore=ignore /><@preview.metadata self.content />></script><![endif]-->
<#else>
  <script src="${jsLink}"<@utils.renderAttr attr=attr ignore=ignore /><@preview.metadata self.content />></script>
</#if>
