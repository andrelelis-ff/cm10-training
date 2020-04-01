<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.webflow.WebflowActionState" -->

<#assign action=self.action />

<li class="cm-navigation-item cm-navigation-item--depth-1 cm-navigation-item--special-depth-1"<@preview.metadata data=[action.content, "properties.id"] />>
  <#assign actionLink=cm.getLink(action, {"next": "$nextUrl$", "absolute": true, "scheme": "https"})/>
  <a href="#" data-href="${actionLink}" class="cm-navigation-item__title"<@preview.metadata data="properties.teaserTitle" />>
    ${action.teaserTitle!""}
  </a>
</li>

