<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.PageGridPlacement" -->

<#assign additionalClass=cm.localParameters().additionalClass!"" />

<div id="cm-placement-${self.name!""}" class="cm-placement cm-placement--${self.name!""} ${additionalClass}"<@preview.metadata data=[bp.getPlacementPropertyName(self), bp.getPlacementHighlightingMetaData(self)!""]/>>

  <#-- replace main section with the main content to render -->
  <#if self.name! == "main" && cmpage.detailView>
    <div class="cm-details-container">
      <@cm.include self=cmpage.content/>
    </div>
  <#-- render the placement items -->
  <#elseif self.name! == "header">
    <@cm.include self=self view="asHeader"/>
  <#elseif self.name! == "footer">
    <@cm.include self=self view="asFooter"/>
  <#elseif self.name! == "footer-navigation">
    <@cm.include self=self view="asFooterNavigation"/>
  <#-- default -->
  <#else>
    <@cm.include self=self view="asPlacement" />
  </#if>

</div>
