<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.navigation.Navigation" -->
<#-- @ftlvariable name="cssClass" type="java.lang.String" -->
<#-- @ftlvariable name="attr" type="java.lang.String" -->
<#-- @ftlvariable name="link" type="java.lang.String" -->

<#assign cssClass=cm.localParameters().cssClass!"" />
<#assign attr=cm.localParameters().attr!"" />
<#assign link=cm.localParameters().link!cm.getLink(self) />

<a class="${cssClass}" href="${link}" ${attr?no_esc}<@preview.metadata "properties.title"/>>${self.title!""}</a>
