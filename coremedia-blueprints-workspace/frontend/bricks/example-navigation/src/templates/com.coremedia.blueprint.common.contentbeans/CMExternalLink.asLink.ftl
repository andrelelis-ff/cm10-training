<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMExternalLink" -->
<#-- @ftlvariable name="cssClass" type="java.lang.String" -->

<#-- same as CMTeasable.asLink but with target=_blank as default -->
<#assign cssClass=cm.localParameters().cssClass!"" />
<#assign target=self.openInNewTab?then('target="_blank"', "") />
<#assign rel=self.openInNewTab?then('rel="noopener"', "") />

<#if self.url?has_content>
  <a class="${cssClass!""}" href="${self.url}" ${target?no_esc} ${rel?no_esc}<@preview.metadata data=[self.content, "properties.teaserTitle"] />>${self.teaserTitle!""}</a>
</#if>
