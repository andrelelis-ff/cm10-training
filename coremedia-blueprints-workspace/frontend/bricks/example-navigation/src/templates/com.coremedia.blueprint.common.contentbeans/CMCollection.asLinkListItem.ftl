<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMCollection" -->
<#-- @ftlvariable name="cssClass" type="java.lang.String" -->
<#-- @ftlvariable name="isTopLevel" type="java.lang.Boolean" -->
<#-- @ftlvariable name="showNavigationLabel" type="java.lang.Boolean" -->
<#-- @ftlvariable name="collectionProperty" type="java.util.List" -->

<#assign cssClass=cm.localParameters().cssClass!""/>
<#assign isTopLevel=cm.localParameters().isTopLevel!false/>
<#assign showNavigationLabel=cm.localParameters().showNavigationLabel!false/>
<#assign showPicturesInNavigation=cm.localParameters().showPicturesInNavigation!true/>
<#assign depth=(cm.localParameters().depth!0)+1/>
<#if depth <= 2>
<#-- single item collection are only rendered single navigation items  -->
  <#assign items=self.items![]>
  <#if (items?size == 1) || !(self.teaserTitle?has_content)>
    <#list items![] as child>
      <@cm.include self=child view="asLinkListItem"  params={
        "isTopLevel": isTopLevel,
        "depth" : depth-1,
        "showPicturesInNavigation": showPicturesInNavigation,
        "collectionProperty": (collectionProperty!["properties.children"]) + [self.content, "properties.items"]
      }/>
    </#list>

  <#elseif (items?size > 1)>
    <#-- add css class active, if this item is part of the active navigation -->
    <#if (items?seq_contains(cmpage.content))>
      <#assign cssClass= cssClass + " cm-navigation-item--active"/>
    </#if>

    <li class="${cssClass} cm-navigation-item cm-navigation-item--depth-${depth}<#if isTopLevel></#if>" <@preview.metadata (collectionProperty!["properties.children"]) + [self.content]/>>
      <#--link to this item in navigation and render children in dropdown list -->
      <span class="cm-navigation-item__title">${self.teaserTitle!""}</span>
      <#if isTopLevel>
        <button type="button" class="cm-navigation-item__toggle" aria-haspopup="true" disabled></button>
      </#if>

      <ul class="${cssClass} cm-navigation-item__menu">
        <#if (showNavigationLabel && depth < 2) >
          <li class="cm-navigation-item__menu-label">
            <span class="cm-navigation-item__title" <@preview.metadata ["properties.teaserTitle"] />>${self.teaserTitle!""}</span>
          </li>
        </#if>
        <#-- considering the flattenedItems call the following navigation items cannot be containers anymore -->
        <#list items![] as child>
          <@cm.include self=child view="asLinkListItem" params={
          "isTopLevel": isTopLevel,
          "depth" : depth,
          "showNavigationLabel" : showNavigationLabel,
          "showPicturesInNavigation": showPicturesInNavigation,
          "collectionProperty":["properties.items"]
          } />
        </#list>
      </ul>
    </li>
  <#elseif (self.teaserTitle?length > 0)><#-- render an text placeholder if the collection does not contain any items -->
    <li class="${cssClass} cm-navigation-item cm-navigation-item--depth-${depth}">
      <#-- link to this item in navigation -->
      <a>${self.teaserTitle!""}</a>
    </li>
  </#if>
</#if>
