<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMCSS" -->

<#--
    Template Description:

    This template is used for CSS files if
    - ieExpression (conditional comments for IE) is set
    - css includes an external css file
    - cae.use.local.resources and/or cae.developer.mode are set to true
    Otherwise MergableResources.asCSSLink.ftl is used.
-->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#assign cssLink=cm.getLink(self)/>
<#assign attr={ "rel": "stylesheet" } + self.htmlAttributes />
<#assign ignore=["href"] />

<#if self.ieExpression?has_content>
  <!--[if ${self.ieExpression}]><link href="${cssLink}"<@utils.renderAttr attr=attr ignore=ignore /><@preview.metadata self.content />><![endif]-->
<#else>
  <link href="${cssLink}"<@utils.renderAttr attr=attr ignore=ignore /><@preview.metadata self.content />>
</#if>
