<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->
<#-- @ftlvariable name="cssClass" type="java.lang.String" -->
<#-- @ftlvariable name="attr" type="java.lang.String" -->
<#-- @ftlvariable name="link" type="java.lang.String" -->

<#assign cssClass=cm.localParameters().cssClass!"" />
<#assign attr=cm.localParameters().attr!"" />
<#assign link=cm.localParameters().link!cm.getLink(self.target!cm.UNDEFINED) />

<a class="${cssClass}" href="${link}" ${attr?no_esc} <@preview.metadata data=[self.content, "properties.teaserTitle"] />>${self.teaserTitle!""}</a>
