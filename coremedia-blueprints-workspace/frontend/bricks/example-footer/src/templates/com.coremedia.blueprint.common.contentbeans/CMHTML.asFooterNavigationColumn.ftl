<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMHTML" -->

<#if self.teaserTitle?has_content>
  <h2 class="cm-footer-navigation-column__title"<@preview.metadata "properties.teaserTitle" />>${self.teaserTitle!""}</h2>
</#if>

<div class="cm-footer-navigation-column">
  <#-- delegate to detail view (which renders the html data) -->
  <@cm.include self=self />
</div>
