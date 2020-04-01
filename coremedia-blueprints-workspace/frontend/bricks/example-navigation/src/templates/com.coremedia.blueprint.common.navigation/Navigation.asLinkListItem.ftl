<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.navigation.Navigation" -->
<#-- @ftlvariable name="isRoot" type="java.lang.Boolean" -->
<#-- @ftlvariable name="cssClass" type="java.lang.String" -->
<#-- @ftlvariable name="isTopLevel" type="java.lang.Boolean" -->
<#-- @ftlvariable name="showNavigationLabel" type="java.lang.Boolean" -->

<#assign cssClass=cm.localParameters().cssClass!""/>
<#assign isRoot=(isRoot!true)/>
<#assign isTopLevel=cm.localParameters().isTopLevel!false/>
<#assign showNavigationLabel=cm.localParameters().showNavigationLabel!false/>
<#assign contentData=self.content!{}/>
<#assign depth=(cm.localParameters().depth!0)+1/>
<#assign showPicturesInNavigation=cm.localParameters().showPicturesInNavigation!true/>

<#if isRoot || (!((self.hidden)!false))>

  <#assign children=self.visibleChildren/>
  <#if children?has_content>
    <#-- add css class active, if this item is part of the active navigation -->
    <#if (bp.isActiveNavigation(self, (cmpage.navigation.navigationPathList)![]) || (children?seq_contains(cmpage.content)))>
      <#assign cssClass= cssClass + " cm-navigation-item--active"/>
    </#if>

    <li class="${cssClass} cm-navigation-item cm-navigation-item--depth-${depth}<#if isTopLevel></#if>" <@preview.metadata ["properties.children", contentData]/>>
      <#--link to this item in navigation and render children in dropdown list -->
      <@cm.include self=self view="asLink" params={"cssClass" : "cm-navigation-item__title"}/>
      <#if isTopLevel>
        <#assign cssClass=cssClass />
        <button type="button" class="cm-navigation-item__toggle" aria-haspopup="true" disabled></button>
      </#if>
      <@cm.include self=self view="asLinkList" params={
        "isRoot": false,
        "depth": depth,
        "cssClass": cssClass,
        "showPicturesInNavigation": showPicturesInNavigation,
        "showNavigationLabel": showNavigationLabel,
        "collectionProperty" : ["properties.children"]
      }/>
    </li>
  <#else>
    <#-- add css class active, if this item is part of the active navigation -->
    <#if (bp.isActiveNavigation(self, (cmpage.navigation.navigationPathList)![]))>
      <#assign cssClass= cssClass + " cm-navigation-item--active"/>
    </#if>

    <li class="${cssClass} cm-navigation-item cm-navigation-item--depth-${depth}" <@preview.metadata ["properties.children", contentData]/>>
      <@cm.include self=self view="asLink" params={"cssClass" : "cm-navigation-item__title"}/>

      <#if showPicturesInNavigation && depth == 2 && self.picture?has_content>
        <a class="cm-navigation-item__picture-link" href="${cm.getLink(self.target!cm.UNDEFINED)}">
          <@cm.include self=self.picture!cm.UNDEFINED view="media" params={
            "classBox": "cm-navigation-item__picture-box",
            "classMedia": "cm-navigation-item__picture",
            "metadata": ["properties.picture"]
          }/>
        </a>
      </#if>
    </li>
  </#if>
</#if>
