<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#if self.teaserTitle?has_content>
  <#assign link=cm.getLink(self.target!cm.UNDEFINED) />
  <#assign target=(self.target?has_content && self.target.openInNewTab)?then('target="_blank"', "") />
  <#assign rel=(self.target?has_content && self.target.openInNewTab)?then('rel="noopener"', "") />
  <h2 class="cm-footer-navigation-column__title">
    <a href="${link}" ${target?no_esc} ${rel?no_esc} class="cm-footer-navigation-column__link"<@preview.metadata "properties.teaserTitle" />>${self.teaserTitle!""}</a>
  </h2>
</#if>
